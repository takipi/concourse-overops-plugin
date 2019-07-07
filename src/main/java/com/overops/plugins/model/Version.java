package com.overops.plugins.model;

import java.util.ArrayList;
import java.util.List;

public class Version {

    public Version() {
    }

    public Version(Event version) {
        this.version = version;
    }

    private Event version;

    private List<Metadata> metadata = new ArrayList<>();

    public Event getVersion() {
        return version;
    }

    public void setVersion(Event version) {
        this.version = version;
    }

    public void setVersion(String version) {
        this.version = new Event(version);
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(Metadata metadata) {
        this.metadata.add(metadata);
    }
}
