package com.overops.plugins.service.impl;

import com.overops.plugins.Context;
import com.overops.plugins.model.*;
import com.overops.plugins.service.Render;
import com.takipi.api.client.util.cicd.OOReportEvent;
import org.fusesource.jansi.Ansi;
import com.overops.plugins.service.OutputWriter;

import java.util.List;
import java.util.stream.Stream;

public class RenderService extends Render {

    private ReportBuilder.QualityReport qualityReport;
    private OutputWriter outputStream;
    private QualityGate criticalQualityGate;
    private QualityGate newQualityGate;
    private QualityGate resurfacedQualityGate;
    private QualityGate increasingQualityGate;

    public RenderService(Context context, ReportBuilder.QualityReport report) {
        super(context);
        outputStream = this.context.getOutputStream();
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
        initQualityGates();

        printMainQualityGateStatusSection();
        printQualityGatesSummarySection();
        printQualityGateSection(newQualityGate);
        printQualityGateSection(resurfacedQualityGate);
        printTotalUniqueErrorsSection();
        printQualityGateSection(criticalQualityGate);
        printQualityGateSection(increasingQualityGate);
    }

    private void printTotalUniqueErrorsSection() {
        if (getCheckTotalErrors() || getCheckUniqueErrors()) {
            if (getCheckTotalErrors() && getPassedTotalErrorGate()) {
                outputStream.printStatementHeaders(getTotalErrorSummary());
            } else if (getCheckTotalErrors() && !getPassedTotalErrorGate()) {
                outputStream.printStatementHeaders(getTotalErrorSummary());
            }

            if (getCheckUniqueErrors() && getPassedUniqueErrorGate()) {
                outputStream.printStatementHeaders(getUniqueErrorSummary());
            } else if (getCheckUniqueErrors() && !getPassedUniqueErrorGate()) {
                outputStream.printStatementHeaders(getUniqueErrorSummary());
            }

            if (getHasTopErrors()) {
                outputStream.printYamlObject(new OOReportYaml(getTopEvents()));
            }
        }
        printSeparator();
    }

    private void initQualityGates() {
        newQualityGate = new QualityGate.Builder()
                .setGateName("New Error")
                .setErrorsName("new errors")
                .setSummaryGateName("New")
                .setIncludeIntoReport(qualityReport.isCheckNewGate())
                .setReportCollection(qualityReport.getNewIssues())
                .build();

        resurfacedQualityGate = new QualityGate.Builder()
                .setGateName("Resurfaced Error")
                .setErrorsName("resurfaced errors")
                .setSummaryGateName("Resurfaced")
                .setIncludeIntoReport(qualityReport.isCheckResurfacedGate())
                .setReportCollection(qualityReport.getResurfacedErrors())
                .build();

        criticalQualityGate = new QualityGate.Builder()
                .setGateName("Critical Error")
                .setErrorsName("critical errors")
                .setSummaryGateName("Critical")
                .setIncludeIntoReport(qualityReport.isCheckCriticalGate())
                .setReportCollection(qualityReport.getCriticalErrors())
                .build();

        increasingQualityGate = new QualityGate.Builder()
                .setGateName("Increasing Error")
                .setPassedSummary("Increasing Quality Gate: Passed, OverOps did not detect any increasing errors in the current build against the baseline of " + qualityReport.getInput().baselineTime + ".")
                .setNotPassedSummary("Increasing Quality Gate: Failed, OverOps detected increasing errors in the current build against the baseline of " + qualityReport.getInput().baselineTime + ".")
                .setSummaryGateName("Increasing")
                .setIncludeIntoReport(qualityReport.isCheckRegressionGate())
                .setReportCollection(qualityReport.getRegressions())
                .build();
    }

    private void printMainQualityGateStatusSection() {
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
    }

    private void printQualityGateSection(QualityGate qualityGate) {
        YamlObject qualityGateYamlRepresentation = new OOReportYaml(qualityGate.getReportCollection());
        if (qualityGate.isIncludeIntoReport()) {
            if (qualityGate.passed()) {
                outputStream.printStatementHeaders(qualityGate.getSummary());
            } else {
                outputStream.printStatementHeaders(qualityGate.getSummary());
                outputStream.printYamlObject(qualityGateYamlRepresentation);
            }
        }

        printSeparator();
    }

    private void printSeparator() {
        outputStream.yellowFgPrintln("");
    }

    private void printQualityGatesSummarySection() {
        outputStream.printStatementHeaders("Report Summary");
        outputStream.printYamlObject(getSummaryYamlObject(), (property, value) -> {
            if (property.equals("status")) {
                if (value.equals("Passed")) {
                    return Ansi.Color.GREEN;
                } else {
                    return Ansi.Color.RED;
                }
            }

            return null;
        });
        printSeparator();
    }

    private YamlObject getSummaryYamlObject() {
        QualityGateSummaryYaml qualityGateSummaryYaml = new QualityGateSummaryYaml("gates");

        Stream.of(newQualityGate, resurfacedQualityGate, criticalQualityGate, increasingQualityGate).forEach( qualityGate -> {
            if (qualityGate.isIncludeIntoReport()) {
                qualityGateSummaryYaml.addSummaryRow(new SummaryRow(qualityGate));
            }
        });

        return qualityGateSummaryYaml;
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
            return "OverOps has marked build " + getDeploymentName() + " as unstable because the below quality gate(s) were not met.";
        } else if (!getMarkedUnstable() && getUnstable()) {
            //unstable build stable when NOT marking the build as unstable
            return "OverOps has detected issues with build " + getDeploymentName() + " but did not mark the build as unstable.";
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
}
