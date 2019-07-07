package com.overops.plugins.model;

public class Event {

    public Event() {
    }

    public Event(String eventId) {
        this.eventId = eventId;
    }

    private String eventId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
