package com.overops.plugins;

import com.overops.plugins.model.Context;
import com.overops.plugins.model.Config;
import com.overops.plugins.service.impl.RenderService;
import com.overops.report.service.ReportService;
import com.overops.report.service.model.QualityReport;

import java.util.Arrays;

public class Plugin {

    private Context context;

    public Plugin() {
        context = DependencyInjector.getImplementation(Context.class);
    }

    public void run(String[] args) {
        // Assume build is failed unless told otherwise
        int status = 1;
        try {
            Config config = new Config(args);
            try {

                QualityReport qualityReport = new ReportService().runQualityReport(
                        config.getOverOpsURL(), config.getOverOpsAPIKey(),
                        config.getReportParams(), ReportService.Requestor.UNKNOWN);

                new RenderService(qualityReport).render();

                // Check to make build stable or not
                status = qualityReport.getStatusCode() == QualityReport.ReportStatus.FAILED ? 1 : 0;
            } catch(Exception e) {
                // Report failed to be generated
                printError(e);
                status = config.getReportParams().isMarkUnstable() ? 1 : 0;
            }
        } catch(Exception e) {
            // Bad arguments
            printError(e);
        }

        System.exit(status);
    }

    private void printError(Exception e) {
        context.getOutputStream().printlnError("Exceptions: " + e.toString());
        context.getOutputStream().printlnError("Trace: " + Arrays.toString(e.getStackTrace()));
    }
}
