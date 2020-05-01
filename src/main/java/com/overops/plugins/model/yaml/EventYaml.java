package com.overops.plugins.model.yaml;

import com.overops.report.service.model.QualityGateEvent;
import com.overops.report.service.model.QualityGateTestResults.TestType;

import java.util.LinkedHashMap;
import java.util.List;

import static com.overops.report.service.model.QualityGateTestResults.TestType.REGRESSION_EVENTS_TEST;

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
            if(REGRESSION_EVENTS_TEST.equals(type)) {
                map.put("Volume / Rate", event.getEventRate());
                map.put("Type", event.getType());
            } else {
                map.put("Volume", event.getEventRate());
            }
            map.put("ARC Link", event.getArcLink());
            
            simpleProperties.add(map);
        });
    }
}
