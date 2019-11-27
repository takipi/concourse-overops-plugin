package com.overops.plugins.service.impl;

import com.overops.plugins.Context;
import com.overops.plugins.model.QueryOverOps;
import com.overops.plugins.service.OverOpsService;
import com.overops.plugins.utils.StringUtils;
import com.takipi.api.client.ApiClient;
import com.takipi.api.client.RemoteApiClient;
import com.takipi.api.client.data.view.SummarizedView;
import com.takipi.api.client.observe.Observer;
import com.takipi.api.client.result.event.EventResult;
import com.takipi.api.client.util.regression.RegressionInput;
import com.takipi.api.client.util.regression.RegressionUtil;
import com.takipi.api.client.util.view.ViewUtil;
import com.takipi.common.util.CollectionUtil;
import org.fusesource.jansi.Ansi;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;

public class OverOpsServiceImpl implements OverOpsService {
    private static final String SEPERATOR = ",";
    private boolean runRegressions = false;
    private Context context;

    public OverOpsServiceImpl(Context context) {
        this.context = context;
    }

    @Override
    public ReportBuilder.QualityReport perform(QueryOverOps queryOverOps) throws IOException, InterruptedException {
        PrintStream printStream;
        if (convertToMinutes(queryOverOps.getBaselineTimespan()) > 0) {
            runRegressions = true;
        }
        if (queryOverOps.isDebug()) {
            printStream = System.err;
        } else {
            printStream = null;
        }

        if (Objects.nonNull(printStream)) {
            context.getOutputStream().println("OverOps [Step 2/3]: Starting OverOps Quality Gate...", Ansi.Color.MAGENTA);
        }

        validateInputs(queryOverOps, printStream);

        RemoteApiClient apiClient = (RemoteApiClient) RemoteApiClient.newBuilder().setHostname(queryOverOps.getOverOpsURL()).setApiKey(queryOverOps.getOverOpsAPIKey()).build();

        if (Objects.nonNull(printStream) && (queryOverOps.isDebug())) {
            apiClient.addObserver(new ApiClientObserver(printStream, queryOverOps.isDebug()));
        }

        SummarizedView allEventsView = ViewUtil.getServiceViewByName(apiClient, queryOverOps.getOverOpsSID().toUpperCase(), "All Events");

        if (Objects.isNull(allEventsView)) {
            if(Objects.nonNull(printStream)) {
                context.getOutputStream().error("Could not acquire ID for 'All Events'. Please check connection to " + queryOverOps.getOverOpsURL());
            }
            throw new IllegalStateException(
                    "Could not acquire ID for 'All Events'. Please check connection to " + queryOverOps.getOverOpsURL());
        }

        RegressionInput input = setupRegressionData(queryOverOps, allEventsView, printStream);
        return ReportBuilder.execute(apiClient, input, queryOverOps.getMaxErrorVolume(), queryOverOps.getMaxUniqueErrors(),
                queryOverOps.getPrintTopIssues(), queryOverOps.getRegexFilter(), queryOverOps.isNewEvents(), queryOverOps.isResurfacedErrors(),
                runRegressions, queryOverOps.isMarkUnstable(), printStream, queryOverOps.isDebug());

    }

    private void validateInputs (QueryOverOps queryOverOps, PrintStream printStream) {
        String apiHost = queryOverOps.getOverOpsURL();
        String apiKey = queryOverOps.getOverOpsAPIKey();

        if (StringUtils.isEmpty(apiHost)) {
            throw new IllegalArgumentException("Missing host name");
        }

        if (StringUtils.isEmpty(apiKey)) {
            throw new IllegalArgumentException("Missing api key");
        }

        //validate active and baseline time window
        if (!"0".equalsIgnoreCase(queryOverOps.getActiveTimespan())) {
            if (convertToMinutes(queryOverOps.getActiveTimespan()) == 0) {
                context.getOutputStream().error("For Increasing Error Gate, the active timewindow currently set to: " + queryOverOps.getActiveTimespan() +  " is not properly formated. See help for format instructions.");
                throw new IllegalArgumentException("For Increasing Error Gate, the active timewindow currently set to: " + queryOverOps.getActiveTimespan() +  " is not properly formated. See help for format instructions.");
            }
        }
        if (!"0".equalsIgnoreCase(queryOverOps.getBaselineTimespan())) {
            if (convertToMinutes(queryOverOps.getBaselineTimespan()) == 0) {
                context.getOutputStream().error("For Increasing Error Gate, the baseline timewindow currently set to: " + queryOverOps.getBaselineTimespan() + " cannot be zero or is improperly formated. See help for format instructions.");
                throw new IllegalArgumentException("For Increasing Error Gate, the baseline timewindow currently set to: " + queryOverOps.getBaselineTimespan() + " cannot be zero or is improperly formated. See help for format instructions.");
            }
        }


        if (StringUtils.isEmpty(queryOverOps.getOverOpsSID())) {
            throw new IllegalArgumentException("Missing environment Id");
        }

    }

    private RegressionInput setupRegressionData(QueryOverOps queryOverOps, SummarizedView allEventsView, PrintStream printStream)
            throws InterruptedException, IOException {

        RegressionInput input = new RegressionInput();
        input.serviceId = queryOverOps.getOverOpsSID();
        input.viewId = allEventsView.id;
        input.applictations = parseArrayString(queryOverOps.getApplicationName(), printStream, "Application Name");
        input.deployments = parseArrayString(queryOverOps.getDeploymentName(), printStream, "Deployment Name");
        input.criticalExceptionTypes = parseArrayString(queryOverOps.getCriticalExceptionTypes(), printStream,
                "Critical Exception Types");

        if (runRegressions) {
            input.activeTimespan = convertToMinutes(queryOverOps.getActiveTimespan());
            input.baselineTime = queryOverOps.getBaselineTimespan();
            input.baselineTimespan = convertToMinutes(queryOverOps.getBaselineTimespan());
            input.minVolumeThreshold = queryOverOps.getMinVolumeThreshold();
            input.minErrorRateThreshold = queryOverOps.getMinErrorRateThreshold();
            input.regressionDelta = queryOverOps.getRegressionDelta();
            input.criticalRegressionDelta = queryOverOps.getCriticalRegressionDelta();
            input.applySeasonality = queryOverOps.isApplySeasonality();
            input.validate();
        }

        printInputs(queryOverOps, printStream, input);

        return input;
    }

    private int convertToMinutes(String timeWindow) {

        if (StringUtils.isEmpty(timeWindow)) {
          return 0;
        }

        if (timeWindow.toLowerCase().contains("d")) {
            int days = Integer.parseInt(timeWindow.substring(0, timeWindow.indexOf("d")));
            return days * 24 * 60;
        } else if (timeWindow.toLowerCase().contains("h")) {
            int hours = Integer.parseInt(timeWindow.substring(0, timeWindow.indexOf("h")));
            return hours * 60;
        } else if (timeWindow.toLowerCase().contains("m")) {
            return Integer.parseInt(timeWindow.substring(0, timeWindow.indexOf("m")));
        }

        return 0;
    }

    private static Collection<String> parseArrayString(String value, PrintStream printStream, String name) {
        if (StringUtils.isEmpty(value)) {
            return Collections.emptySet();
        }

        return Arrays.asList(value.trim().split(Pattern.quote(SEPERATOR)));
    }

    private void printInputs(QueryOverOps queryOverOps, PrintStream printStream, RegressionInput input) {

        if (Objects.nonNull(printStream)) {
            printStream.println(input);

            printStream.println("Max unique errors  = " + queryOverOps.getMaxUniqueErrors());
            printStream.println("Max error volume  = " + queryOverOps.getMaxErrorVolume());
            printStream.println("Check new errors  = " + queryOverOps.isNewEvents());
            printStream.println("Check resurfaced errors  = " + queryOverOps.isResurfacedErrors());

            String regexPrint;

            if (Objects.nonNull(queryOverOps.getRegexFilter())) {
                regexPrint = queryOverOps.getRegexFilter();
            } else {
                regexPrint = "";
            }

            printStream.println("Regex filter  = " + regexPrint);
        }
    }

    protected static class ApiClientObserver implements Observer {

        private final PrintStream printStream;
        private final boolean verbose;

        public ApiClientObserver(PrintStream printStream, boolean verbose) {
            this.printStream = printStream;
            this.verbose = verbose;
        }

        @Override
        public void observe(Operation operation, String url, String request, String response, int responseCode, long time) {
            StringBuilder output = new StringBuilder();

            output.append(String.valueOf(operation));
            output.append(" took ");
            output.append(time / 1000);
            output.append("ms for ");
            output.append(url);

            if (verbose) {
                output.append(". Response: ");
                output.append(response);
            }

            printStream.println(output.toString());
        }
    }

    private Collection<EventResult> getEvents(ApiClient apiClient, RegressionInput input, DateTime deploymentStart, PrintStream printStream) {
        Collection<EventResult> events = RegressionUtil.getActiveEventVolume(apiClient, input, deploymentStart, printStream);
        if (!CollectionUtil.safeIsEmpty(events)) {
            return events;
        } else {
            events = RegressionUtil.getActiveEventVolume(apiClient, input, deploymentStart, printStream, true);
            return (Collection)(CollectionUtil.safeIsEmpty(events) ? events : filterByLabel(events, input));
        }
    }

    private List<EventResult> filterByLabel(Collection<EventResult> inputEvents, RegressionInput input) {
        List<String> list = (List)input.applictations;
        String appName = (String)list.get(0) + ".app";
        List<EventResult> result = new ArrayList();

        for (EventResult eventResult : inputEvents) {
            if (eventResult.labels != null && eventResult.labels.contains(appName)) {
                result.add(eventResult);
            }
        }

        return result;
    }
}