package com.overops.plugins.model.yaml;

import com.overops.report.service.model.QualityGateTestResults;
import com.overops.report.service.model.QualityGateTestResults.TestType;

import java.util.LinkedHashMap;

public class QualityGateSummaryYaml extends YamlObject {

    public QualityGateSummaryYaml(String name) {
        super(name);
    }

    public void addSummaryRow(QualityGateTestResults qualityGate) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Name", getGateName(qualityGate.getTestType()));
        map.put("Status", qualityGate.isPassed() ? "Passed" : "Failed");
        map.put("Errors", Long.toString(qualityGate.getErrorCount()));
        simpleProperties.add(map);
    }
    
    private String getGateName(TestType type) {
        switch (type) {
            case CRITICAL_EVENTS_TEST:
                return "Critical";
            case NEW_EVENTS_TEST:
                return "New";
            case REGRESSION_EVENTS_TEST:
                return "Regression";
            case RESURFACED_EVENTS_TEST:
                return "Resurfaced";
            case TOTAL_EVENTS_TEST:
                return "Total";
            case UNIQUE_EVENTS_TEST:
                return "Unique";
            default:
                return "";
        }
    }
}
