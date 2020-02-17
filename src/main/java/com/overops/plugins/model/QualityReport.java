package com.overops.plugins.model;

import com.takipi.api.client.util.cicd.OOReportEvent;
import com.takipi.api.client.util.regression.RateRegression;
import com.takipi.api.client.util.regression.RegressionInput;

import java.util.ArrayList;
import java.util.List;

public class QualityReport {

    private final List<OOReportEvent> newIssues;
    private final List<OOReportRegressedEvent> regressions;
    private final List<OOReportEvent> criticalErrors;
    private final List<OOReportEvent> topErrors;
    private final List<OOReportEvent> resurfacedErrors;
    private final List<OOReportEvent> allIssues;
    private final boolean unstable;
    private final RegressionInput input;
    private final RateRegression regression;
    private final long eventVolume;
    private final int uniqueEventsCount;
    private final boolean checkNewGate;
    private final boolean checkResurfacedGate;
    private final boolean checkCriticalGate;
    private final boolean checkVolumeGate;
    private final boolean checkUniqueGate;
    private final boolean checkRegressionGate;
    private final Integer maxEventVolume;
    private final Integer maxUniqueVolume;
    private final boolean markedUnstable;

    public QualityReport(RegressionInput input, RateRegression regression,
                         List<OOReportRegressedEvent> regressions, List<OOReportEvent> criticalErrors,
                         List<OOReportEvent> topErrors, List<OOReportEvent> newIssues,
                         List<OOReportEvent> resurfacedErrors, long eventVolume, int uniqueEventCounts, boolean unstable,
                         boolean checkNewGate, boolean checkResurfacedGate, boolean checkCriticalGate, boolean checkVolumeGate,
                         boolean checkUniqueGate, boolean checkRegressionGate, Integer maxEventVolume, Integer maxUniqueVolume, boolean markedUnstable) {

        this.input = input;
        this.regression = regression;

        this.regressions = regressions;
        this.allIssues = new ArrayList<OOReportEvent>();
        this.newIssues = newIssues;
        this.criticalErrors = criticalErrors;
        this.topErrors = topErrors;
        this.resurfacedErrors = resurfacedErrors;

        if (regressions != null) {
            allIssues.addAll(regressions);
        }

        this.eventVolume =  eventVolume;
        this.uniqueEventsCount =  uniqueEventCounts;
        this.unstable = unstable;
        this.checkNewGate = checkNewGate;
        this.checkResurfacedGate = checkResurfacedGate;
        this.checkCriticalGate = checkCriticalGate;
        this.checkVolumeGate = checkVolumeGate;
        this.checkUniqueGate = checkUniqueGate;
        this.checkRegressionGate = checkRegressionGate;
        this.maxEventVolume = maxEventVolume;
        this.maxUniqueVolume = maxUniqueVolume;
        this.markedUnstable = markedUnstable;
    }

    public RegressionInput getInput() {
        return input;
    }

    public RateRegression getRegression() {
        return regression;
    }

    public List<OOReportEvent> getResurfacedErrors() {
        return resurfacedErrors;
    }

    public List<OOReportEvent> getAllIssues() {
        return allIssues;
    }

    public List<OOReportEvent> getCriticalErrors() {
        return criticalErrors;
    }

    public List<OOReportEvent> getTopErrors() {
        return topErrors;
    }

    public List<OOReportEvent> getNewIssues() {
        return newIssues;
    }

    public List<OOReportRegressedEvent> getRegressions() {
        return regressions;
    }

    public long getUniqueEventsCount() {
        return uniqueEventsCount;
    }

    public boolean getUnstable() {
        return unstable;
    }

    public long getEventVolume() {
        return eventVolume;
    }

    public boolean isCheckNewGate() {
        return checkNewGate;
    }

    public boolean isCheckResurfacedGate() {
        return checkResurfacedGate;
    }

    public boolean isCheckCriticalGate() {
        return checkCriticalGate;
    }

    public boolean isCheckVolumeGate() {
        return checkVolumeGate;
    }

    public boolean isCheckUniqueGate() {
        return checkUniqueGate;
    }

    public boolean isCheckRegressionGate() {
        return checkRegressionGate;
    }

    public Integer getMaxEventVolume() {
        return maxEventVolume;
    }

    public Integer getMaxUniqueVolume() {
        return maxUniqueVolume;
    }

    public boolean isMarkedUnstable() {
        return markedUnstable;
    }
}
