package com.overops.plugins.model.yaml;

import com.overops.report.service.model.QualityGateEvent;
import com.overops.report.service.model.QualityGateTestResults.TestType;

import java.util.LinkedHashMap;
import java.util.List;

public class EventYaml extends YamlObject {
    public EventYaml(String name) {
        super(name);
    }

    public EventYaml(List<QualityGateEvent> list, TestType type) {
        super("Events");
        addList(list, type);
    }

    public void addList(List<QualityGateEvent> list, TestType type) {
        if (list == null) {
            return;
        }

        list.forEach(event -> {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("Summary", event.getEventSummary());
            map.put("Applications", event.getApplications());
            map.put("Introduced by", event.getIntroducedBy());
            map.put("ARC Link", event.getArcLink());
            map.put("Volume", event.getEventRate());
            
            simpleProperties.add(map);
        });
    }
}
