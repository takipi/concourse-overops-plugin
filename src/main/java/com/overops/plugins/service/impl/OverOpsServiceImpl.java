package com.overops.plugins.service.impl;

import com.overops.plugins.Context;
import com.overops.plugins.DependencyInjector;
import com.overops.plugins.model.QualityReport;
import com.overops.plugins.model.QueryOverConfig;
import com.overops.plugins.service.OverOpsService;
import com.takipi.api.client.ApiClient;
import com.takipi.api.client.RemoteApiClient;
import com.takipi.api.client.data.view.SummarizedView;
import com.takipi.api.client.observe.Observer;
import com.takipi.api.client.result.event.EventResult;
import com.takipi.api.client.util.regression.RegressionInput;
import com.takipi.api.client.util.regression.RegressionUtil;
import com.takipi.api.client.util.view.ViewUtil;
import com.takipi.common.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;

public class OverOpsServiceImpl implements OverOpsService {
    private static final String SEPARATOR = ",";
    private boolean runRegressions = false;
    private Context context;

    public OverOpsServiceImpl() {
        this.context = DependencyInjector.getImplementation(Context.class);
    }

    @Override
    public QualityReport perform(QueryOverConfig queryOverConfig) throws IOException, InterruptedException {
        PrintStream printStream;

        if (convertToMinutes(queryOverConfig.getBaselineTimespan()) > 0) {
            runRegressions = true;
        }

        if (queryOverConfig.isDebug()) {
            printStream = System.err;
        } else {
            printStream = null;
        }

        if (Objects.nonNull(printStream)) {
            context.getOutputStream().println("OverOps [Step 2/3]: Starting OverOps Quality Gate...", Ansi.Color.MAGENTA);
        }

        validateInputs(queryOverConfig, printStream);

        RemoteApiClient apiClient = (RemoteApiClient) RemoteApiClient.newBuilder().setHostname(queryOverConfig.getOverOpsURL()).setApiKey(queryOverConfig.getOverOpsAPIKey()).build();

        if (Objects.nonNull(printStream) && (queryOverConfig.isDebug())) {
            apiClient.addObserver(new ApiClientObserver(printStream, queryOverConfig.isDebug()));
        }

        SummarizedView allEventsView = ViewUtil.getServiceViewByName(apiClient, queryOverConfig.getOverOpsSID().toUpperCase(), "All Events");

        if (Objects.isNull(allEventsView)) {
            if (Objects.nonNull(printStream)) {
                context.getOutputStream().printlnError("Could not acquire ID for 'All Events'. Please check connection to " + queryOverConfig.getOverOpsURL());
            }
            throw new IllegalStateException(
                    "Could not acquire ID for 'All Events'. Please check connection to " + queryOverConfig.getOverOpsURL());
        }

        RegressionInput input = setupRegressionData(queryOverConfig, allEventsView, printStream);

        return ReportBuilder.execute(apiClient, input, queryOverConfig.getMaxErrorVolume(), queryOverConfig.getMaxUniqueErrors(),
                queryOverConfig.getPrintTopIssues(), queryOverConfig.getRegexFilter(), queryOverConfig.isNewEvents(), queryOverConfig.isResurfacedErrors(),
                runRegressions, queryOverConfig.isMarkUnstable(), printStream, queryOverConfig.isDebug());

    }

    private void validateInputs (QueryOverConfig queryOverConfig, PrintStream printStream) {
        String apiHost = queryOverConfig.getOverOpsURL();
        String apiKey = queryOverConfig.getOverOpsAPIKey();

        if (StringUtils.isEmpty(apiHost)) {
            throw new IllegalArgumentException("Missing host name");
        }

        if (StringUtils.isEmpty(apiKey)) {
            throw new IllegalArgumentException("Missing api key");
        }

        //validate active and baseline time window
        if (!"0".equalsIgnoreCase(queryOverConfig.getActiveTimespan())) {
            if (convertToMinutes(queryOverConfig.getActiveTimespan()) == 0) {
                context.getOutputStream().printlnError("For Increasing Error Gate, the active timewindow currently set to: " + queryOverConfig.getActiveTimespan() +  " is not properly formated. See help for format instructions.");
                throw new IllegalArgumentException("For Increasing Error Gate, the active timewindow currently set to: " + queryOverConfig.getActiveTimespan() +  " is not properly formated. See help for format instructions.");
            }
        }

        if (!"0".equalsIgnoreCase(queryOverConfig.getBaselineTimespan())) {
            if (convertToMinutes(queryOverConfig.getBaselineTimespan()) == 0) {
                context.getOutputStream().printlnError("For Increasing Error Gate, the baseline timewindow currently set to: " + queryOverConfig.getBaselineTimespan() + " cannot be zero or is improperly formated. See help for format instructions.");
                throw new IllegalArgumentException("For Increasing Error Gate, the baseline timewindow currently set to: " + queryOverConfig.getBaselineTimespan() + " cannot be zero or is improperly formated. See help for format instructions.");
            }
        }


        if (StringUtils.isEmpty(queryOverConfig.getOverOpsSID())) {
            throw new IllegalArgumentException("Missing environment Id");
        }
    }

    private RegressionInput setupRegressionData(QueryOverConfig config, SummarizedView allEventsView, PrintStream printStream) {

        RegressionInput input = new RegressionInput();
        input.serviceId = config.getOverOpsSID();
        input.viewId = allEventsView.id;
        input.applictations = parseArrayString(config.getApplicationName(), printStream, "Application Name");
        input.deployments = parseArrayString(config.getDeploymentName(), printStream, "Deployment Name");
        input.criticalExceptionTypes = parseArrayString(config.getCriticalExceptionTypes(), printStream,
                "Critical Exception Types");

        if (runRegressions) {
            input.activeTimespan = convertToMinutes(config.getActiveTimespan());
            input.baselineTime = config.getBaselineTimespan();
            input.baselineTimespan = convertToMinutes(config.getBaselineTimespan());
            input.minVolumeThreshold = config.getMinVolumeThreshold();
            input.minErrorRateThreshold = config.getMinErrorRateThreshold();
            input.regressionDelta = config.getRegressionDelta();
            input.criticalRegressionDelta = config.getCriticalRegressionDelta();
            input.applySeasonality = config.isApplySeasonality();
            input.validate();
        }

        printInputs(config, printStream, input);

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

        return Arrays.asList(value.trim().split(Pattern.quote(SEPARATOR)));
    }

    private void printInputs(QueryOverConfig queryOverConfig, PrintStream printStream, RegressionInput input) {

        if (Objects.nonNull(printStream)) {
            printStream.println(input);

            printStream.println("Max unique errors  = " + queryOverConfig.getMaxUniqueErrors());
            printStream.println("Max error volume  = " + queryOverConfig.getMaxErrorVolume());
            printStream.println("Check new errors  = " + queryOverConfig.isNewEvents());
            printStream.println("Check resurfaced errors  = " + queryOverConfig.isResurfacedErrors());

            String regexPrint;

            if (Objects.nonNull(queryOverConfig.getRegexFilter())) {
                regexPrint = queryOverConfig.getRegexFilter();
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
