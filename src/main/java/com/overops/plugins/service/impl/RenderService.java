package com.overops.plugins.service.impl;

import com.overops.plugins.Context;
import com.overops.plugins.model.SummaryRow;
import com.overops.plugins.service.Render;
import com.takipi.api.client.util.cicd.OOReportEvent;
import org.fusesource.jansi.Ansi;
import com.overops.plugins.service.OutputWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenderService extends Render {

    private ReportBuilder.QualityReport qualityReport;

    public RenderService(Context context, ReportBuilder.QualityReport report) {
        super(context);

        this.qualityReport = report;
    }

    @Override
    public String getDisplayName() {
        return "OverOps Quality Report";
    }

    @Override
    public boolean isStable() {
        boolean stable;
        if (getMarkedUnstable() && getUnstable()) {
            stable = false;
        } else {
            stable = true;
        }
        return stable;
    }

    @Override
    public void render() {
        OutputWriter outputStream = this.context.getOutputStream();

        if (getMarkedUnstable() && getUnstable()) {
            outputStream.error(getSummary());
        } else if (getMarkedUnstable() && !getUnstable()) {
            outputStream.success(getSummary());
        } else if (!getMarkedUnstable() && getUnstable()) {
            outputStream.yellowFgPrintln(getSummary());
        } else {
            outputStream.success(getSummary());
        }

        printSeparator();

        renderSummaryTable();

        printSeparator();

        if (getCheckNewEvents() && getPassedNewErrorGate()) {
            outputStream.printStatementHeaders( getNewErrorSummary(), "Nothing to report");
        } else if (getCheckNewEvents() && !getPassedNewErrorGate()) {
            outputStream.printStatementHeaders(getNewErrorSummary());
            outputStream.table(Arrays.asList("Event", "Application(s)", "Introduced by", "Volume"), getNewEvents());
        }

        printSeparator();

        if (getCheckResurfacedEvents() && getPassedResurfacedErrorGate()) {
            outputStream.printStatementHeaders( getResurfacedErrorSummary(), "Nothing to report");
        } else if (getCheckResurfacedEvents() && !getCheckResurfacedEvents()) {
            outputStream.printStatementHeaders(getResurfacedErrorSummary());
            outputStream.table(Arrays.asList("Event", "Application(s)", "Introduced by", "Volume"), getResurfacedEvents());
        }

        printSeparator();

        if (getCheckTotalErrors() || getCheckUniqueErrors()) {
            if (getCheckTotalErrors() && getPassedTotalErrorGate()) {
                outputStream.printStatementHeaders(getTotalErrorSummary());
            } else if (getCheckTotalErrors() && getPassedTotalErrorGate()) {
                outputStream.printStatementHeaders(getTotalErrorSummary());
            }

            if (getCheckUniqueErrors() && getPassedUniqueErrorGate()) {
                outputStream.printStatementHeaders(getUniqueErrorSummary());
            } else if (getCheckUniqueErrors() && getPassedUniqueErrorGate()) {
                outputStream.printStatementHeaders(getUniqueErrorSummary());
            }

            if (getHasTopErrors()) {
                outputStream.table(Arrays.asList("Top Events Affecting Unique/Total Error Gates", "Application(s)", "Introduced by", "Volume"), getTopEvents());
            } else {
                outputStream.printStatementHeaders("Nothing to report");
            }
        }

        printSeparator();

        if (getCheckCriticalErrors() && getPassedCriticalErrorGate()) {
            outputStream.printStatementHeaders( getCriticalErrorSummary(), "Nothing to report");
        } else if (getCheckCriticalErrors() && !getPassedCriticalErrorGate()) {
            outputStream.printStatementHeaders(getCriticalErrorSummary());
            outputStream.table(Arrays.asList("Event", "Application(s)", "Introduced by", "Volume"), getCriticalEvents());
        }

        printSeparator();

        if (getCheckRegressedErrors() && getPassedRegressedEvents()) {
            outputStream.printStatementHeaders( getRegressionSumarry(), "Nothing to report");
        } else if (getCheckRegressedErrors() && !getPassedRegressedEvents()) {
            outputStream.printStatementHeaders(getRegressionSumarry());
            outputStream.table(Arrays.asList("Event", "Application(s)", "Introduced by", "Volume"), getRegressedEvents());
        }
    }

    private void printSeparator() {
        this.context.getOutputStream().yellowFgPrintln("");
    }

    private void renderSummaryTable() {
        this.context.getOutputStream().println("", Ansi.Color.BLUE);
        this.context.getOutputStream().println("Report Summary", Ansi.Color.BLUE);
        this.context.getOutputStream().tableSummary(Arrays.asList("Gate", "Status", "Passed"), getSummaryCollection());
    }

    private List<SummaryRow> getSummaryCollection() {
        ArrayList<SummaryRow> summaryRows = new ArrayList<>();
        final String passedString =  "-";
        boolean passedNewErrorGate = getPassedNewErrorGate();
        boolean passedResurfacedErrorGate = getPassedResurfacedErrorGate();
        boolean passedCriticalErrorGate = getPassedCriticalErrorGate();
        boolean passedRegressedEvents = getPassedRegressedEvents();
        summaryRows.add(new SummaryRow("New", passedNewErrorGate, passedNewErrorGate ? passedString : String.valueOf(getNewEvents().size())));
        summaryRows.add(new SummaryRow("Resurfaced", passedResurfacedErrorGate, passedResurfacedErrorGate ? passedString : String.valueOf(getResurfacedEvents().size())));
        summaryRows.add(new SummaryRow("Critical", passedCriticalErrorGate, passedCriticalErrorGate ? passedString : String.valueOf(getCriticalEvents().size())));
        summaryRows.add(new SummaryRow("Increasing", passedRegressedEvents, passedRegressedEvents ? passedString : String.valueOf(getResurfacedEvents().size())));
        return summaryRows;
    }

    private boolean getUnstable() {
        return qualityReport.getUnstable();
    }

    private boolean getMarkedUnstable() {
        return qualityReport.isMarkedUnstable();
    }

    private String getSummary() {
        if (getUnstable() && getMarkedUnstable()) {
            //the build is unstable when marking the build as unstable
            return "OverOps has marked build "+ getDeploymentName() + " as unstable because the below quality gate(s) were not met.";
        } else if (!getMarkedUnstable() && getUnstable()) {
            //unstable build stable when NOT marking the build as unstable
            return "OverOps has detected issues with build "+ getDeploymentName() + " but did not mark the build as unstable.";
        } else {
            //stable build when marking the build as unstable
            return "Congratulations, build " + getDeploymentName() + " has passed all quality gates!";
        }
    }

    private String getDeploymentName() {
        String value = qualityReport.getInput().deployments.toString();
        value = value.replace("[", "");
        value = value.replace("]", "");
        return value;
    }

    private boolean getPassedNewErrorGate() {
        return getCheckNewEvents() && !getNewErrorsExist();

    }

    private boolean getCheckNewEvents() {
        return qualityReport.isCheckNewGate();
    }

    private String getNewErrorSummary() {
        if (getNewEvents() != null && getNewEvents().size() > 0) {
            return "New Error Gate: Failed, OverOps detected " + qualityReport.getNewIssues().size() + " new error(s) in your build.";
        } else if (qualityReport.isCheckNewGate()) {
            return "New Error Gate: Passed, OverOps did not detect any new errors in your build.";
        }

        return null;
    }

    private boolean getNewErrorsExist() {
        return getNewEvents() != null && getNewEvents().size() > 0;
    }

    private List<OOReportEvent> getNewEvents() {
        return qualityReport.getNewIssues();
    }

    private boolean getPassedResurfacedErrorGate() {
        return getCheckResurfacedEvents() && !getResurfacedErrorsExist();

    }

    private boolean getResurfacedErrorsExist() {
        return getResurfacedEvents() != null && getResurfacedEvents().size() > 0;
    }

    private boolean getCheckResurfacedEvents() {
        return qualityReport.isCheckResurfacedGate();
    }

    private String getResurfacedErrorSummary() {
        if (getResurfacedEvents() != null && getResurfacedEvents().size() > 0) {
            return "Resurfaced Error Gate: Failed, OverOps detected " + qualityReport.getResurfacedErrors().size() + " resurfaced errors in your build.";
        } else if (qualityReport.isCheckResurfacedGate()) {
            return "Resurfaced Error Gate: Passed, OverOps did not detect any resurfaced errors in your build.";
        }

        return null;
    }

    private List<OOReportEvent> getResurfacedEvents() {
        return qualityReport.getResurfacedErrors();
    }

    private boolean getCheckCriticalErrors() {
        return qualityReport.isCheckCriticalGate();
    }

    private boolean getPassedCriticalErrorGate() {
        return getCheckCriticalErrors() && !getCriticalErrorsExist();

    }

    private boolean getCriticalErrorsExist() {
        return getCriticalEvents() != null && getCriticalEvents().size() > 0;
    }

    private String getCriticalErrorSummary() {
        if (getCriticalEvents() != null && getCriticalEvents().size() > 0) {
            return "Critical Error Gate: Failed, OverOps detected " + qualityReport.getCriticalErrors().size() + " critical errors in your build.";
        } else if (qualityReport.isCheckCriticalGate()) {
            return "Critical Error Gate: Passed, OverOps did not detect any critical errors in your build.";
        }

        return null;
    }

    private List<OOReportEvent> getCriticalEvents() {
        return qualityReport.getCriticalErrors();
    }

    //this will serve as a check for either unique or total error gates
    private boolean getCountGates() {
        return getCheckUniqueErrors() || getCheckTotalErrors();
    }

    private boolean getCheckTotalErrors() {
        return qualityReport.isCheckVolumeGate();
    }

    private boolean getPassedTotalErrorGate() {
        return getCheckTotalErrors() && (qualityReport.getEventVolume() > 0 && qualityReport.getEventVolume() < qualityReport.getMaxEventVolume());

    }

    private String getTotalErrorSummary() {
        if (qualityReport.getEventVolume() > 0 && qualityReport.getEventVolume() >= qualityReport.getMaxEventVolume()) {
            return "Total Error Volume Gate: Failed, OverOps detected " + qualityReport.getEventVolume() + " total errors which is >= the max allowable of " + qualityReport.getMaxEventVolume();
        } else if (qualityReport.getEventVolume() > 0 && qualityReport.getEventVolume() < qualityReport.getMaxEventVolume()) {
            return "Total Error Volume Gate: Passed, OverOps detected " + qualityReport.getEventVolume() + " total errors which is < than max allowable of " + qualityReport.getMaxEventVolume();
        }

        return null;
    }

    private boolean getCheckUniqueErrors() {
        return qualityReport.isCheckUniqueGate();
    }

    private boolean getHasTopErrors() {
        return !getPassedTotalErrorGate() || !getPassedUniqueErrorGate();
    }

    private boolean getPassedUniqueErrorGate() {
        return getCheckUniqueErrors() && (qualityReport.getUniqueEventsCount() > 0 && qualityReport.getUniqueEventsCount() < qualityReport.getMaxUniqueVolume());

    }

    private String getUniqueErrorSummary() {
        if (qualityReport.getUniqueEventsCount() > 0 && qualityReport.getUniqueEventsCount() >= qualityReport.getMaxUniqueVolume()) {
            return "Unique Error Volume Gate: Failed, OverOps detected " + qualityReport.getUniqueEventsCount() + " unique errors which is >= the max allowable of " + qualityReport.getMaxUniqueVolume();
        } else if (qualityReport.getUniqueEventsCount() > 0 && qualityReport.getUniqueEventsCount() < qualityReport.getMaxUniqueVolume()) {
            return "Unique Error Volume Gate: Passed, OverOps detected " + qualityReport.getUniqueEventsCount() + " unique errors which is < than max allowable of " + qualityReport.getMaxUniqueVolume();
        }

        return null;
    }

    private List<OOReportEvent> getTopEvents() {
        return qualityReport.getTopErrors();
    }

    private String getRegressionSumarry() {
        if (!getPassedRegressedEvents()) {
            return "Increasing Quality Gate: Failed, OverOps detected incressing errors in the current build against the baseline of " + qualityReport.getInput().baselineTime;
        } else if (getPassedRegressedEvents()) {
            return "Increasing Quality Gate: Passed, OverOps did not detect any increasing errors in the current build against the baseline of " + qualityReport.getInput().baselineTime;
        }

        return null;
    }

    private boolean getCheckRegressedErrors() {
        return qualityReport.isCheckRegressionGate();
    }

    private boolean getPassedRegressedEvents() {
        return !getCheckRegressedErrors() || qualityReport.getRegressions() == null || qualityReport.getRegressions().size() <= 0;
    }

    private List<OOReportEvent> getRegressedEvents() {
        return qualityReport.getAllIssues();
    }
}
