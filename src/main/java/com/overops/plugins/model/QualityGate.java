package com.overops.plugins.model;

import com.takipi.api.client.util.cicd.OOReportEvent;

import java.util.List;

public class QualityGate<T extends OOReportEvent> {
    private boolean includeIntoReport;
    private List<T> reportCollection;
    private String gateName;
    private String summaryGateName;
    private String errorsName;
    private String passedSummary;
    private String notPassedSummary;

    public QualityGate(String gateName,
                       String errorsName,
                       String summaryGateName,
                       String passedSummary,
                       String notPassedSummary,
                       boolean includeIntoReport,
                       List<T> reportCollection) {
        this.gateName = gateName;
        this.errorsName = errorsName;
        this.summaryGateName = summaryGateName;
        this.passedSummary = passedSummary;
        this.notPassedSummary = notPassedSummary;
        this.includeIntoReport = includeIntoReport;
        this.reportCollection = reportCollection;
    }

    public String getSummary() {
        if (includeIntoReport) {
            if (haveDataToReport()){
                return getNotPassedSummary();
            } else {
                return getPassedSummary();
            }
        } else {
            return getNoteThatQualityGateIsNotIncluded();
        }
    }

    private String getNoteThatQualityGateIsNotIncluded() {
        return gateName + " Gate check was turn off.";
    }

    private String getPassedSummary() {
        return passedSummary != null ? passedSummary : gateName + " Gate: Passed, OverOps did not detect any " + errorsName +" in your build.";
    }

    private String getNotPassedSummary() {
        return notPassedSummary!= null ? notPassedSummary : gateName + " Gate: Failed, OverOps detected " + reportCollection.size() + " " + errorsName + " in your build.";
    }

    public boolean passed() {
        return !haveDataToReport();
    }

    public boolean haveDataToReport() {
        return reportCollection != null && reportCollection.size() > 0;
    }

    public boolean isIncludeIntoReport() {
        return includeIntoReport;
    }

    public List<T> getReportCollection() {
        return reportCollection;
    }

    public String getSummaryGateName() {
        return summaryGateName;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder<T extends OOReportEvent> {
        private String gateName;
        private String errorsName;
        private String summaryGateName;
        private String passedSummary;
        private String notPassedSummary;
        private boolean includeIntoReport;
        private List<T> reportCollection;

        public Builder setGateName(String gateName) {
            this.gateName = gateName;
            return this;
        }

        public Builder setErrorsName(String errorsName) {
            this.errorsName = errorsName;
            return this;
        }

        public Builder setIncludeIntoReport(boolean includeIntoReport) {
            this.includeIntoReport = includeIntoReport;
            return this;
        }

        public Builder setReportCollection(List<T> reportCollection) {
            this.reportCollection = reportCollection;
            return this;
        }

        public Builder setPassedSummary(String passedSummary) {
            this.passedSummary = passedSummary;
            return this;
        }

        public Builder setNotPassedSummary(String notPassedSummary) {
            this.notPassedSummary = notPassedSummary;
            return this;
        }

        public Builder setSummaryGateName(String summaryGateName) {
            this.summaryGateName = summaryGateName;
            return this;
        }

        public QualityGate build() {
            return new QualityGate(gateName, errorsName, summaryGateName, passedSummary,
                    notPassedSummary, includeIntoReport, reportCollection);
        }
    }
}
