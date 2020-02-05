package com.overops.plugins.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class YamlObject {
    private String name;
    protected List<LinkedHashMap<String, String>> simpleProperties;

    public YamlObject(String name) {
        this.name = name;
        simpleProperties = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<LinkedHashMap<String, String>> getSimpleProperties() {
        return simpleProperties;
    }
}
