package com.overops.plugins.service.impl;

import com.overops.plugins.Context;
import com.overops.plugins.DependencyInjector;
import com.overops.plugins.model.Config;
import com.overops.plugins.model.QualityReport;
import com.overops.plugins.service.OutputWriter;
import com.overops.plugins.service.OverOpsService;
import com.takipi.api.client.RemoteApiClient;
import com.takipi.api.client.data.view.SummarizedView;
import com.takipi.api.client.util.regression.RegressionInput;
import com.takipi.api.client.util.view.ViewUtil;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

public class OverOpsServiceImpl implements OverOpsService {
    private boolean runRegressions = false;
    private Context context;
    private Config config;

    public OverOpsServiceImpl() {
        this.context = DependencyInjector.getImplementation(Context.class);
    }

    @Override
    public QualityReport perform(Config config) throws IOException, InterruptedException {
        this.config = config;
        runRegressions = config.getBaselineTimespanMinutes() > 0;

        PrintStream printStream;
        if (config.isDebug()) {
            printStream = System.err;
        } else {
            printStream = null;
        }

        context.getOutputStream().println("OverOps [Step 2/3]: Starting OverOps Quality Gate...", Ansi.Color.MAGENTA);

        RemoteApiClient apiClient = (RemoteApiClient) RemoteApiClient.newBuilder()
                .setHostname(config.getOverOpsURL())
                .setApiKey(config.getOverOpsAPIKey())
                .build();
        printApiClientLogs(apiClient);

        SummarizedView allEventsView = ViewUtil.getServiceViewByName(apiClient, config.getOverOpsSID().toUpperCase(), "All Events");
        validateAllEventsView(allEventsView);

        RegressionInput input = setupRegressionData(allEventsView);

        return ReportBuilder.execute(apiClient, input, config.getMaxErrorVolume(), config.getMaxUniqueErrors(),
                config.getPrintTopIssues(), config.getRegexFilter(), config.isNewEvents(), config.isResurfacedErrors(),
                runRegressions, config.isMarkUnstable(), printStream, config.isDebug());

    }

    private void printApiClientLogs(RemoteApiClient apiClient) {
        if (config.isDebug()) {
            apiClient.addObserver((operation, url, request, response, responseCode, time) -> {
                String message = new StringBuilder()
                        .append(operation).append(" took ").append(time / 1000).append("ms for ").append(url)
                        .append(". Response: ").append(response).toString();

                context.getOutputStream().printlnDebug(message);
            });
        }
    }

    private void validateAllEventsView(SummarizedView allEventsView) {
        if (Objects.isNull(allEventsView)) {
            if (config.isDebug()) {
                context.getOutputStream().printlnError("Could not acquire ID for 'All Events'. Please check connection to " + config.getOverOpsURL());
            }
            throw new IllegalStateException(
                    "Could not acquire ID for 'All Events'. Please check connection to " + config.getOverOpsURL());
        }
    }

    private RegressionInput setupRegressionData(SummarizedView allEventsView) {
        RegressionInput input = new RegressionInput();
        input.serviceId = config.getOverOpsSID();
        input.viewId = allEventsView.id;
        input.applictations = config.getApplicationCollection();
        input.deployments = config.getDeploymentCollection();
        input.criticalExceptionTypes = config.getCriticalExceptionTypesCollection();

        if (runRegressions) {
            input.activeTimespan = config.getActiveTimespanMinutes();
            input.baselineTime = config.getBaselineTimespan();
            input.baselineTimespan = config.getBaselineTimespanMinutes();
            input.minVolumeThreshold = config.getMinVolumeThreshold();
            input.minErrorRateThreshold = config.getMinErrorRateThreshold();
            input.regressionDelta = config.getRegressionDelta();
            input.criticalRegressionDelta = config.getCriticalRegressionDelta();
            input.applySeasonality = config.isApplySeasonality();
            input.validate();
        }

        printInputs(input);

        return input;
    }

    private void printInputs(RegressionInput input) {
        if (config.isDebug()) {
            OutputWriter printStream = context.getOutputStream();
            printStream.printlnDebug(input.toString());
            printStream.printlnDebug("Max unique errors  = " + config.getMaxUniqueErrors());
            printStream.printlnDebug("Max error volume  = " + config.getMaxErrorVolume());
            printStream.printlnDebug("Check new errors  = " + config.isNewEvents());
            printStream.printlnDebug("Check resurfaced errors  = " + config.isResurfacedErrors());
            printStream.printlnDebug("Regex filter  = " + config.getRegexFilter());
        }
    }
}
