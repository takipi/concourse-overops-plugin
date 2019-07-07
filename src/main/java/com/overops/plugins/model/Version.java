package com.overops.plugins.model;

import java.util.ArrayList;
import java.util.List;

public class Version {

    public Version(Event version) {
        this.version = version;
    }

    private Event version;

    public Event getVersion() {
        return version;
    }

    public void setVersion(Event version) {
        this.version = version;
    }

    public void setVersion(String version) {
        this.version = new Event(version);
    }

}
