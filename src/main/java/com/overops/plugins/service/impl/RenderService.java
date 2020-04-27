package com.overops.plugins.service.impl;

import com.overops.plugins.model.yaml.EventYaml;
import com.overops.plugins.model.yaml.QualityGateSummaryYaml;
import com.overops.plugins.model.yaml.YamlObject;
import com.overops.plugins.service.Render;
import com.overops.report.service.model.QualityGateEvent;
import com.overops.report.service.model.QualityGateTestResults;
import com.overops.report.service.model.QualityReport;
import org.fusesource.jansi.Ansi;
import com.overops.plugins.service.OutputWriter;

import java.util.List;
import java.util.stream.Stream;

public class RenderService extends Render {

    private QualityReport qualityReport;
    private OutputWriter outputStream;

    public RenderService(QualityReport report) {
        context.getOutputStream().println("OverOps Quality Report", Ansi.Color.WHITE);
        outputStream = this.context.getOutputStream();
        this.qualityReport = report;
    }

    @Override
    public Render render() {
        printMainQualityGateStatusSection();
        printQualityGatesSummarySection();

        printQualityGateSection(qualityReport.getNewErrorsTestResults());
        printQualityGateSection(qualityReport.getResurfacedErrorsTestResults());
        printQualityGateSection(qualityReport.getTotalErrorsTestResults());
        printQualityGateSection(qualityReport.getUniqueErrorsTestResults());
        printQualityGateSection(qualityReport.getCriticalErrorsTestResults());
        printQualityGateSection(qualityReport.getRegressionErrorsTestResults());

        return this;
    }

    private void printMainQualityGateStatusSection() {
        QualityReport.ReportStatus reportStatus = qualityReport.getStatusCode();
        String message = qualityReport.getStatusMsg();

        if(reportStatus == QualityReport.ReportStatus.PASSED) {
            outputStream.printlnSuccess(message);
        } else if(reportStatus == QualityReport.ReportStatus.WARNING) {
            outputStream.yellowFgPrintln(message);
        } else {
            outputStream.printlnError(message);
        }

        printSeparator();
    }

    private void printQualityGateSection(QualityGateTestResults qualityGate) {
        if(qualityGate != null) {
            // TODO add type in the quality gate so check doesn't have to be done on error count and events == null
            // This means the gate is Unique or Total so use the Top Errors List
            List<QualityGateEvent> events = qualityGate.getEvents();
            if(events == null && qualityGate.getErrorCount() > 0) {
                events = getTopEvents();
            }

            YamlObject eventYaml = new EventYaml(events);
            outputStream.printStatementHeaders(qualityGate.getMessage());
            if(!qualityGate.isPassed()) {
                outputStream.printYamlObject(eventYaml);
            }

            printSeparator();
        }
    }

    private void printSeparator() {
        outputStream.yellowFgPrintln("");
    }

    private void printQualityGatesSummarySection() {
        outputStream.printStatementHeaders("Report Summary");
        outputStream.printYamlObject(getSummaryYamlObject(), (property, value) -> {
            if (property.equals("Status")) {
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
        QualityGateSummaryYaml qualityGateSummaryYaml = new QualityGateSummaryYaml("Gates");

        Stream.of(qualityReport.getNewErrorsTestResults(), qualityReport.getResurfacedErrorsTestResults(), qualityReport.getTotalErrorsTestResults(), qualityReport.getUniqueErrorsTestResults(), qualityReport.getCriticalErrorsTestResults(), qualityReport.getRegressionErrorsTestResults()).forEach( qualityGate -> {
            if(qualityGate != null) {
                qualityGateSummaryYaml.addSummaryRow(qualityGate);
            }
        });

        return qualityGateSummaryYaml;
    }

    private List<QualityGateEvent> getTopEvents() {
        return qualityReport.getTopEvents();
    }
}
