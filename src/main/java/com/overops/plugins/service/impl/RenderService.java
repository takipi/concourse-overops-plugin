package com.overops.plugins.service.impl;

import com.overops.plugins.Context;
import com.overops.plugins.model.*;
import com.overops.plugins.model.yaml.OOReportYaml;
import com.overops.plugins.model.yaml.QualityGateSummaryYaml;
import com.overops.plugins.model.yaml.YamlObject;
import com.overops.plugins.service.Render;
import com.takipi.api.client.util.cicd.OOReportEvent;
import org.fusesource.jansi.Ansi;
import com.overops.plugins.service.OutputWriter;

import java.util.List;
import java.util.stream.Stream;

public class RenderService extends Render {

    private QualityReport qualityReport;
    private OutputWriter outputStream;
    private QualityGate criticalQualityGate;
    private QualityGate newQualityGate;
    private QualityGate resurfacedQualityGate;
    private QualityGate increasingQualityGate;

    public RenderService(QualityReport report) {
        context.getOutputStream().println("OverOps Quality Report", Ansi.Color.BLACK);
        outputStream = this.context.getOutputStream();
        this.qualityReport = report;
    }

    @Override
    public Render render() {
        initQualityGates();

        printMainQualityGateStatusSection();
        printQualityGatesSummarySection();
        printQualityGateSection(newQualityGate);
        printQualityGateSection(resurfacedQualityGate);
        printTotalUniqueErrorsSection();
        printQualityGateSection(criticalQualityGate);
        printQualityGateSection(increasingQualityGate);

        return this;
    }

    private void printTotalUniqueErrorsSection() {
        boolean includeTotalErrorsGate = qualityReport.isCheckVolumeGate();
        boolean includeUniqueErrorsGate = qualityReport.isCheckUniqueGate();
        boolean includeTotalOrUniqueErrorsGate = includeTotalErrorsGate || includeUniqueErrorsGate;
        if (includeTotalOrUniqueErrorsGate) {
            if (includeTotalErrorsGate) {
                outputStream.printStatementHeaders(getTotalErrorSummary());
            }

            if (includeUniqueErrorsGate) {
                outputStream.printStatementHeaders(getUniqueErrorSummary());
            }

            if (getHasTopErrors()) {
                outputStream.printYamlObject(new OOReportYaml(getTopEvents()));
            }

            printSeparator();
        }
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
        boolean toMarkUnstable = qualityReport.isMarkedUnstable();
        boolean isActuallyUnstable = qualityReport.getUnstable();
        String deploymentName = getDeploymentName();
        String passedSummary = "Congratulations, build " + deploymentName + " has passed all quality gates!";
        String unstableBuildWithToMarkUnstableSetSummary = "OverOps has marked build " + deploymentName + " as unstable because the below quality gate(s) were not met.";
        String unstableBuildWithToMarkUnstableNotSetSummary = "OverOps has detected issues with build " + deploymentName + " but did not mark the build as unstable.";

        if (toMarkUnstable) {
            if (isActuallyUnstable) {
                outputStream.printlnError(unstableBuildWithToMarkUnstableSetSummary);
            } else {
                outputStream.printlnSuccess(passedSummary);
            }
        } else {
            if (isActuallyUnstable) {
                outputStream.yellowFgPrintln(unstableBuildWithToMarkUnstableNotSetSummary);
            } else {
                outputStream.printlnSuccess(passedSummary);
            }
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

    private String getDeploymentName() {
        return qualityReport.getInput().deployments.toString()
                .replace("[", "")
                .replace("]", "");
    }

    private boolean getPassedTotalErrorGate() {
        return qualityReport.getEventVolume() > 0 && qualityReport.getEventVolume() < qualityReport.getMaxEventVolume();

    }

    private String getTotalErrorSummary() {
        if (qualityReport.getEventVolume() > 0 && qualityReport.getEventVolume() >= qualityReport.getMaxEventVolume()) {
            return "Total Error Volume Gate: Failed, OverOps detected " + qualityReport.getEventVolume() + " total errors which is >= the max allowable of " + qualityReport.getMaxEventVolume();
        } else if (qualityReport.getEventVolume() > 0 && qualityReport.getEventVolume() < qualityReport.getMaxEventVolume()) {
            return "Total Error Volume Gate: Passed, OverOps detected " + qualityReport.getEventVolume() + " total errors which is < than max allowable of " + qualityReport.getMaxEventVolume();
        }

        return null;
    }

    private boolean getPassedUniqueErrorGate() {
        return qualityReport.getUniqueEventsCount() > 0 && qualityReport.getUniqueEventsCount() < qualityReport.getMaxUniqueVolume();
    }

    private String getUniqueErrorSummary() {
        if (qualityReport.getUniqueEventsCount() > 0 && qualityReport.getUniqueEventsCount() >= qualityReport.getMaxUniqueVolume()) {
            return "Unique Error Volume Gate: Failed, OverOps detected " + qualityReport.getUniqueEventsCount() + " unique errors which is >= the max allowable of " + qualityReport.getMaxUniqueVolume();
        } else if (qualityReport.getUniqueEventsCount() > 0 && qualityReport.getUniqueEventsCount() < qualityReport.getMaxUniqueVolume()) {
            return "Unique Error Volume Gate: Passed, OverOps detected " + qualityReport.getUniqueEventsCount() + " unique errors which is < than max allowable of " + qualityReport.getMaxUniqueVolume();
        }

        return null;
    }

    private boolean getHasTopErrors() {
        return !getPassedTotalErrorGate() || !getPassedUniqueErrorGate();
    }

    private List<OOReportEvent> getTopEvents() {
        return qualityReport.getTopErrors();
    }
}
