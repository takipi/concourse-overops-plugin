package com.overops.plugins.model;

import java.util.LinkedHashMap;

public class QualityGateSummaryYaml extends YamlObject {

    public QualityGateSummaryYaml(String name) {
        super(name);
    }

    public void addSummaryRow(SummaryRow summaryRow) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("name", summaryRow.getGateName());
        map.put("status", summaryRow.getGateStatus());
        map.put("volume", summaryRow.getTotal());
        simpleProperties.add(map);
    }
}
