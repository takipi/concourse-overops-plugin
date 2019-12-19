package com.overops.plugins.service.impl;

import com.overops.plugins.Context;
import com.overops.plugins.service.Render;
import com.takipi.api.client.util.cicd.OOReportEvent;
import org.fusesource.jansi.Ansi;

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
        if (getMarkedUnstable() && getUnstable()) {
            this.context.getOutputStream().error(getSummary());
        } else if (getMarkedUnstable() && !getUnstable()) {
            this.context.getOutputStream().success(getSummary());
        } else if (!getMarkedUnstable() && getUnstable()) {
            this.context.getOutputStream().yellow(getSummary());
        } else {
            this.context.getOutputStream().success(getSummary());
        }

        if (getCheckNewEvents() && getPassedNewErrorGate()) {
            this.context.getOutputStream().block(Ansi.Color.GREEN, getNewErrorSummary(), "Nothing to report");
        } else if (getCheckNewEvents() && !getPassedNewErrorGate()) {
            this.context.getOutputStream().block(getNewErrorSummary(), Ansi.Color.RED, false);
            this.context.getOutputStream().table(Arrays.asList("Event", "Application(s)", "Introduced by", "Volume"), getNewEvents());
        }

        if (getCheckResurfacedEvents() && getPassedResurfacedErrorGate()) {
            this.context.getOutputStream().block(Ansi.Color.GREEN, getResurfacedErrorSummary(), "Nothing to report");
        } else if (getCheckResurfacedEvents() && !getCheckResurfacedEvents()) {
            this.context.getOutputStream().block(getResurfacedErrorSummary(), Ansi.Color.RED, false);
            this.context.getOutputStream().table(Arrays.asList("Event", "Application(s)", "Introduced by", "Volume"), getResurfacedEvents());
        }

        if (getCheckTotalErrors() || getCheckUniqueErrors()) {
            if (getCheckTotalErrors() && getPassedTotalErrorGate()) {
                this.context.getOutputStream().block(getTotalErrorSummary(), Ansi.Color.GREEN, false);
            } else if (getCheckTotalErrors() && getPassedTotalErrorGate()) {
                this.context.getOutputStream().block(getTotalErrorSummary(), Ansi.Color.RED, false);
            }

            if (getCheckUniqueErrors() && getPassedUniqueErrorGate()) {
                this.context.getOutputStream().block(getUniqueErrorSummary(), Ansi.Color.GREEN, false);
            } else if (getCheckUniqueErrors() && getPassedUniqueErrorGate()) {
                this.context.getOutputStream().block(getUniqueErrorSummary(), Ansi.Color.RED, false);
            }

            if (getHasTopErrors()) {
                this.context.getOutputStream().table(Arrays.asList("Top Events Affecting Unique/Total Error Gates", "Application(s)", "Introduced by", "Volume"), getTopEvents());
            } else {
                this.context.getOutputStream().block("Nothing to report", Ansi.Color.BLUE, true);
            }
        }

        if (getCheckCriticalErrors() && getPassedCriticalErrorGate()) {
            this.context.getOutputStream().block(Ansi.Color.GREEN, getCriticalErrorSummary(), "Nothing to report");
        } else if (getCheckCriticalErrors() && !getPassedCriticalErrorGate()) {
            this.context.getOutputStream().block(getCriticalErrorSummary(), Ansi.Color.RED, false);
            this.context.getOutputStream().table(Arrays.asList("Event", "Application(s)", "Introduced by", "Volume"), getCriticalEvents());
        }

        if (getCheckRegressedErrors() && getPassedRegressedEvents()) {
            this.context.getOutputStream().block(Ansi.Color.GREEN, getRegressionSumarry(), "Nothing to report");
        } else if (getCheckRegressedErrors() && !getPassedRegressedEvents()) {
            this.context.getOutputStream().block(getRegressionSumarry(), Ansi.Color.RED, false);
            this.context.getOutputStream().table(Arrays.asList("Event", "Application(s)", "Introduced by", "Volume"), getRegressedEvents());
        }
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
