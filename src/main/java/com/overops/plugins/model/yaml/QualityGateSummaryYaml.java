package com.overops.plugins.model.yaml;

import com.overops.report.service.model.QualityGateTestResults;

import java.util.LinkedHashMap;

public class QualityGateSummaryYaml extends YamlObject {

    public QualityGateSummaryYaml(String name) {
        super(name);
    }

    public void addSummaryRow(QualityGateTestResults qualityGate) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        // TODO think about setting the name on QualityGateTestResults
        map.put("Name", qualityGate.getMessage());
        map.put("Status", qualityGate.isPassed() ? "Passed" : "Failed");
        map.put("Errors", Long.toString(qualityGate.getErrorCount()));
        simpleProperties.add(map);
    }
}
