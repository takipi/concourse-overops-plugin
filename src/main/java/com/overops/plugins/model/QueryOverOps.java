package com.overops.plugins.model;

import java.util.Map;

public class QueryOverOps {
    private String overOpsURL;
    private String overOpsSID;
    private String overOpsAPIKey;

    private String applicationName;
    private String deploymentName;
    private String serviceId;
    private String regexFilter;
    private boolean markUnstable;
    private Integer printTopIssues;
    private boolean newEvents;
    private boolean resurfacedErrors;
    private Integer maxErrorVolume;
    private Integer maxUniqueErrors;
    private String criticalExceptionTypes;
    private boolean checkRegressionErrors;
    private String activeTimespan;
    private String baselineTimespan;
    private Integer minVolumeThreshold;
    private Double minErrorRateThreshold;
    private Double regressionDelta;
    private Double criticalRegressionDelta;
    private boolean applySeasonality;

    private boolean debug;

    public String getOverOpsURL() {
        return overOpsURL;
    }

    public void setOverOpsURL(String overOpsURL) {
        this.overOpsURL = overOpsURL;
    }

    public String getOverOpsSID() {
        return overOpsSID;
    }

    public void setOverOpsSID(String overOpsSID) {
        this.overOpsSID = overOpsSID;
    }

    public String getOverOpsAPIKey() {
        return overOpsAPIKey;
    }

    public void setOverOpsAPIKey(String overOpsAPIKey) {
        this.overOpsAPIKey = overOpsAPIKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getRegexFilter() {
        return regexFilter;
    }

    public void setRegexFilter(String regexFilter) {
        this.regexFilter = regexFilter;
    }

    public boolean isMarkUnstable() {
        return markUnstable;
    }

    public void setMarkUnstable(boolean markUnstable) {
        this.markUnstable = markUnstable;
    }

    public Integer getPrintTopIssues() {
        return printTopIssues;
    }

    public void setPrintTopIssues(Integer printTopIssues) {
        this.printTopIssues = printTopIssues;
    }

    public boolean isNewEvents() {
        return newEvents;
    }

    public void setNewEvents(boolean newEvents) {
        this.newEvents = newEvents;
    }

    public boolean isResurfacedErrors() {
        return resurfacedErrors;
    }

    public void setResurfacedErrors(boolean resurfacedErrors) {
        this.resurfacedErrors = resurfacedErrors;
    }

    public Integer getMaxErrorVolume() {
        return maxErrorVolume;
    }

    public void setMaxErrorVolume(Integer maxErrorVolume) {
        this.maxErrorVolume = maxErrorVolume;
    }

    public Integer getMaxUniqueErrors() {
        return maxUniqueErrors;
    }

    public void setMaxUniqueErrors(Integer maxUniqueErrors) {
        this.maxUniqueErrors = maxUniqueErrors;
    }

    public String getCriticalExceptionTypes() {
        return criticalExceptionTypes;
    }

    public void setCriticalExceptionTypes(String criticalExceptionTypes) {
        this.criticalExceptionTypes = criticalExceptionTypes;
    }

    public Boolean getCheckRegressionErrors() {
        return checkRegressionErrors;
    }

    public void setCheckRegressionErrors(Boolean checkRegressionErrors) {
        this.checkRegressionErrors = checkRegressionErrors;
    }

    public String getActiveTimespan() {
        return activeTimespan;
    }

    public void setActiveTimespan(String activeTimespan) {
        this.activeTimespan = activeTimespan;
    }

    public String getBaselineTimespan() {
        return baselineTimespan;
    }

    public void setBaselineTimespan(String baselineTimespan) {
        this.baselineTimespan = baselineTimespan;
    }

    public Integer getMinVolumeThreshold() {
        return minVolumeThreshold;
    }

    public void setMinVolumeThreshold(Integer minVolumeThreshold) {
        this.minVolumeThreshold = minVolumeThreshold;
    }

    public Double getMinErrorRateThreshold() {
        return minErrorRateThreshold;
    }

    public void setMinErrorRateThreshold(Double minErrorRateThreshold) {
        this.minErrorRateThreshold = minErrorRateThreshold;
    }

    public Double getRegressionDelta() {
        return regressionDelta;
    }

    public void setRegressionDelta(Double regressionDelta) {
        this.regressionDelta = regressionDelta;
    }

    public Double getCriticalRegressionDelta() {
        return criticalRegressionDelta;
    }

    public void setCriticalRegressionDelta(Double criticalRegressionDelta) {
        this.criticalRegressionDelta = criticalRegressionDelta;
    }

    public boolean isApplySeasonality() {
        return applySeasonality;
    }

    public void setApplySeasonality(boolean applySeasonality) {
        this.applySeasonality = applySeasonality;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public static QueryOverOps mapToObject(Map<String, String> params) {
        QueryOverOps queryOverOps = new QueryOverOps();
        queryOverOps.overOpsURL = params.get("overOpsURL");
        queryOverOps.overOpsSID = params.get("overOpsSID");
        queryOverOps.overOpsAPIKey = params.get("overOpsAPIKey");
        queryOverOps.applicationName = params.get("applicationName");
        queryOverOps.deploymentName = params.get("deploymentName");
        queryOverOps.serviceId = params.getOrDefault("serviceId", queryOverOps.overOpsSID);
        queryOverOps.regexFilter = params.getOrDefault("regexFilter", "");
        queryOverOps.markUnstable = Boolean.parseBoolean(params.getOrDefault("markUnstable", "false"));
        queryOverOps.printTopIssues = Integer.parseInt(params.getOrDefault("printTopIssues", "5"));
        if (Boolean.parseBoolean(params.getOrDefault("checkNewErrors", "false"))) {
            queryOverOps.newEvents = Boolean.parseBoolean(params.getOrDefault("newEvents", "false"));
        }
        if (Boolean.parseBoolean(params.getOrDefault("checkResurfacedErrors", "false"))) {
            queryOverOps.resurfacedErrors = Boolean.parseBoolean(params.getOrDefault("resurfacedErrors", "false"));
        }
        if (Boolean.parseBoolean(params.getOrDefault("checkVolumeErrors", "false"))) {
            queryOverOps.maxErrorVolume = Integer.parseInt(params.getOrDefault("maxErrorVolume", "0"));
        }
        if (Boolean.parseBoolean(params.getOrDefault("checkUniqueErrors", "false"))) {
            queryOverOps.maxUniqueErrors = Integer.parseInt(params.getOrDefault("maxUniqueErrors", "0"));
        }
        if (Boolean.parseBoolean(params.getOrDefault("checkCriticalErrors", "false"))) {
            queryOverOps.criticalExceptionTypes = params.getOrDefault("criticalExceptionTypes", "");
        }
        queryOverOps.setCheckRegressionErrors(Boolean.parseBoolean(params.getOrDefault("checkRegressionErrors", "false")));
        if (queryOverOps.getCheckRegressionErrors()) {
            queryOverOps.activeTimespan = params.getOrDefault("activeTimespan", "0");
            queryOverOps.baselineTimespan = params.getOrDefault("baselineTimespan", "0");
            queryOverOps.minVolumeThreshold = Integer.parseInt(params.getOrDefault("minVolumeThreshold", "0"));
            queryOverOps.minErrorRateThreshold = Double.parseDouble(params.getOrDefault("minErrorRateThreshold", "0"));
            queryOverOps.regressionDelta = Double.parseDouble(params.getOrDefault("regressionDelta", "0"));
            queryOverOps.criticalRegressionDelta = Double.parseDouble(params.getOrDefault("criticalRegressionDelta", "0"));
            queryOverOps.applySeasonality = Boolean.parseBoolean(params.getOrDefault("applySeasonality", "false"));
        }
        queryOverOps.debug = Boolean.parseBoolean(params.getOrDefault("debug", "false"));
        return queryOverOps;
    }

    @Override
    public String toString() {
        return "QueryOverOps{" +
                "applicationName='" + applicationName + '\'' +
                ", deploymentName='" + deploymentName + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", regexFilter='" + regexFilter + '\'' +
                ", markUnstable=" + markUnstable +
                ", printTopIssues=" + printTopIssues +
                ", newEvents=" + newEvents +
                ", resurfacedErrors=" + resurfacedErrors +
                ", maxErrorVolume=" + maxErrorVolume +
                ", maxUniqueErrors=" + maxUniqueErrors +
                ", criticalExceptionTypes='" + criticalExceptionTypes + '\'' +
                ", activeTimespan='" + activeTimespan + '\'' +
                ", baselineTimespan='" + baselineTimespan + '\'' +
                ", minVolumeThreshold=" + minVolumeThreshold +
                ", minErrorRateThreshold=" + minErrorRateThreshold +
                ", regressionDelta=" + regressionDelta +
                ", criticalRegressionDelta=" + criticalRegressionDelta +
                ", applySeasonality=" + applySeasonality +
                ", debug=" + debug +
                '}';
    }
}
