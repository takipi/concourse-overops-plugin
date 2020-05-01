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

import static com.overops.report.service.model.QualityGateTestResults.TestType.TOTAL_EVENTS_TEST;
import static com.overops.report.service.model.QualityGateTestResults.TestType.UNIQUE_EVENTS_TEST;

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
    
    public OutputWriter getOutputStream() {
        return outputStream;
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
            List<QualityGateEvent> events = qualityGate.getEvents();
            if(UNIQUE_EVENTS_TEST.equals(qualityGate.getTestType()) || TOTAL_EVENTS_TEST.equals(qualityGate.getTestType())) {
                events = getTopEvents();
            }

            YamlObject eventYaml = new EventYaml(events, qualityGate.getTestType());
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
