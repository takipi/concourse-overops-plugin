package com.overops.plugins.model.yaml;

import com.overops.plugins.model.yaml.YamlObject;
import com.takipi.api.client.util.cicd.OOReportEvent;

import java.util.LinkedHashMap;
import java.util.List;

public class OOReportYaml extends YamlObject {
    public OOReportYaml(String name) {
        super(name);
    }

    public OOReportYaml(List<OOReportEvent> list) {
        super("events");
        addList(list);
    }

    public void addList(List<OOReportEvent> list) {
        if (list == null) {
            return;
        }

        list.forEach(ooReportEvent -> {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("summary", ooReportEvent.getEventSummary());
            map.put("applications", ooReportEvent.getApplications());
            map.put("introduced_by", ooReportEvent.getIntroducedBy());
            map.put("arc_link", ooReportEvent.getARCLink());
            simpleProperties.add(map);
        });
    }
}
