package com.overops.plugins;

import com.overops.plugins.model.Context;
import com.overops.plugins.model.Config;
import com.overops.plugins.service.impl.RenderService;
import com.overops.report.service.ReportService;
import com.overops.report.service.model.QualityReport;
import com.overops.report.service.model.QualityReportExceptionDetails;
import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.stream.Stream;

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
                        config.getReportParams(), ReportService.Requestor.CONCOURSE,
                        context.getOutputStream().getPrintStream(), config.isDebug());
    
    
                QualityReportExceptionDetails exceptionDetails = qualityReport.getExceptionDetails();
                if(exceptionDetails == null) {
                    new RenderService(qualityReport).render();
                } else {
                    context.getOutputStream().println("OverOps was unable to generate a Quality Report.", Ansi.Color.WHITE);
                    context.getOutputStream().printlnError(exceptionDetails.getExceptionMessage());
                    Stream.of(exceptionDetails.getStackTrace()).forEachOrdered(
                            trace -> context.getOutputStream().println(trace, Ansi.Color.WHITE)
                    );
                }

                // Check to make build stable or not
                status = qualityReport.getStatusCode() == QualityReport.ReportStatus.FAILED ? 1 : 0;
            } catch(Exception e) {
                // Report failed to be generated
                context.getOutputStream().println("OverOps was unable to generate a Quality Report.", Ansi.Color.WHITE);
                printError(e);
                status = config.getReportParams().isMarkUnstable() ? 1 : 0;
            }
        } catch(Exception e) {
            // Bad arguments
            context.getOutputStream().println("OverOps was unable to generate a Quality Report.", Ansi.Color.WHITE);
            printError(e);
        }

        System.exit(status);
    }

    private void printError(Exception e) {
        context.getOutputStream().printlnError("Exceptions: " + e.toString());
        context.getOutputStream().printlnError("Trace: " + Arrays.toString(e.getStackTrace()));
    }
}
