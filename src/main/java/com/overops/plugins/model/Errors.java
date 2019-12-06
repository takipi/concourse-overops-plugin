package com.overops.plugins.model;

import com.takipi.api.client.util.cicd.OOReportEvent;

import java.util.List;

public class Errors {
    private List<OOReportEvent> topErrors;
    private List<OOReportEvent> resurfacedErrors;
    private List<OOReportEvent> criticalErrors;
    private List<OOReportEvent> newErrors;
    private List<OOReportRegressedEvent> regressionErrors;

    public Errors() {
    }

    public Errors(List<OOReportEvent> topErrors, List<OOReportEvent> resurfacedErrors, List<OOReportEvent> criticalErrors,
                  List<OOReportEvent> newErrors, List<OOReportRegressedEvent> regressionErrors) {
        this.topErrors = topErrors;
        this.resurfacedErrors = resurfacedErrors;
        this.criticalErrors = criticalErrors;
        this.newErrors = newErrors;
        this.regressionErrors = regressionErrors;
    }

    public List<OOReportEvent> getTopErrors() {
        return topErrors;
    }

    public void setTopErrors(List<OOReportEvent> topErrors) {
        this.topErrors = topErrors;
    }

    public List<OOReportEvent> getResurfacedErrors() {
        return resurfacedErrors;
    }

    public void setResurfacedErrors(List<OOReportEvent> resurfacedErrors) {
        this.resurfacedErrors = resurfacedErrors;
    }

    public List<OOReportEvent> getCriticalErrors() {
        return criticalErrors;
    }

    public void setCriticalErrors(List<OOReportEvent> criticalErrors) {
        this.criticalErrors = criticalErrors;
    }

    public List<OOReportEvent> getNewErrors() {
        return newErrors;
    }

    public void setNewErrors(List<OOReportEvent> newErrors) {
        this.newErrors = newErrors;
    }

    public List<OOReportRegressedEvent> getRegressionErrors() {
        return regressionErrors;
    }

    public void setRegressionErrors(List<OOReportRegressedEvent> regressionErrors) {
        this.regressionErrors = regressionErrors;
    }
}
