package com.overops.plugins.model;

import java.util.ArrayList;
import java.util.List;

public class Versions {
    private List<Event> version = new ArrayList<>();

    public List<Event> getVersion() {
        return version;
    }

    public void setVersion(List<Event> version) {
        this.version = version;
    }

    public void addToVersions(String version) {
        this.version.add(new Event(version));
    }
}
