package com.overops.plugins;

import com.overops.plugins.model.Context;
import com.overops.plugins.model.Config;
import com.overops.plugins.service.impl.RenderLinkService;
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
                ReportService reportService = new ReportService();
                if (config.isUseLink())
                {
                    String reportLink = reportService.generateReportLink(config.getOverOpsAppURL(), config.getReportParams());
                    new RenderLinkService(reportLink).render();
                    status = 0;
                } else
                {
                    reportService.pauseForTheCause(context.getOutputStream().getPrintStream());
                    QualityReport qualityReport = reportService.runQualityReport(
                        config.getOverOpsURL(), config.getOverOpsAPIKey(),
                        config.getReportParams(), ReportService.Requestor.CONCOURSE,
                        context.getOutputStream().getPrintStream(), config.isDebug());


                    QualityReportExceptionDetails exceptionDetails = qualityReport.getExceptionDetails();
                    if (exceptionDetails == null)
                    {
                        new RenderService(qualityReport, config.isShowEventsForPassedGates()).render();
                    }
                    else
                    {
                        context.getOutputStream().println("OverOps was unable to generate a Quality Report.", Ansi.Color.WHITE);
                        context.getOutputStream().printlnError(exceptionDetails.getExceptionMessage());
                        Stream.of(exceptionDetails.getStackTrace()).forEachOrdered(
                            trace -> context.getOutputStream().println(trace, Ansi.Color.WHITE)
                        );
                    }

                    // Check to make build stable or not
                    boolean hasExceptions = exceptionDetails != null;
                    status = calculateStatus(qualityReport.getStatusCode(), hasExceptions, config.isPassBuildOnException());
                }
            } catch(Exception e) {
                // Report failed to be generated
                context.getOutputStream().println("OverOps was unable to generate a Quality Report.", Ansi.Color.WHITE);
                printError(e);
                status = calculateStatus(QualityReport.ReportStatus.FAILED, true, config.isPassBuildOnException());
            }
        } catch(Exception e) {
            // Bad arguments. Very unlikely to happen and in this case the
            // build will fail because we couldn't determine the users entered
            // settings. Failure because status = 1 initially
            context.getOutputStream().println("OverOps was unable to generate a Quality Report.", Ansi.Color.WHITE);
            printError(e);
        }

        System.exit(status);
    }
    
    /**
     * Determine whether to report the status of the build as pass or failed
     *
     * @param reportStatus
     * @param passBuildOnException
     * @return 1 for report failed or 0 if passed
     */
    private int calculateStatus(QualityReport.ReportStatus reportStatus, boolean hasExceptions, boolean passBuildOnException) {
        int status = 1;
        if (reportStatus == QualityReport.ReportStatus.FAILED) {
            if (hasExceptions && passBuildOnException) {
                status = 0;
            }
        } else {
            status = 0;
        }

        return status;
    }
    
    private void printError(Exception e) {
        context.getOutputStream().printlnError("Exceptions: " + e.toString());
        context.getOutputStream().printlnError("Trace: " + Arrays.toString(e.getStackTrace()));
    }
}
