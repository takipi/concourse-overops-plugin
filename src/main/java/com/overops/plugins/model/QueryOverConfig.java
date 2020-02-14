package com.overops.plugins.model;

import java.util.Map;

public class QueryOverConfig {
    private String overOpsURL;
    private String overOpsSID;
    private String overOpsAPIKey;

    private String applicationName;
    private String deploymentName;
    private String serviceId;
    private String regexFilter;
    private boolean markUnstable = false;
    private Integer printTopIssues = 5;
    private boolean newEvents = false;
    private boolean resurfacedErrors = false;
    private Integer maxErrorVolume = 0;
    private Integer maxUniqueErrors = 0;
    private String criticalExceptionTypes;
    private String activeTimespan = "0";
    private String baselineTimespan = "0";
    private Integer minVolumeThreshold = 0;
    private Double minErrorRateThreshold = 0d;
    private Double regressionDelta = 0d;
    private Double criticalRegressionDelta = 0d;
    private boolean applySeasonality = false;

    private boolean debug = false;

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

    public static QueryOverConfig mapToObject(Map<String, String> params) {
        QueryOverConfig queryOverConfig = new QueryOverConfig();
        queryOverConfig.overOpsSID = params.get("overOpsSID");
        queryOverConfig.overOpsAPIKey = params.get("overOpsAPIKey");
        queryOverConfig.applicationName = params.get("applicationName");
        queryOverConfig.deploymentName = params.get("deploymentName");
        queryOverConfig.serviceId = params.getOrDefault("serviceId", queryOverConfig.overOpsSID);
        queryOverConfig.regexFilter = params.getOrDefault("regexFilter", "");
        queryOverConfig.markUnstable = Boolean.parseBoolean(params.getOrDefault("markUnstable", "false"));
        queryOverConfig.printTopIssues = Integer.parseInt(params.getOrDefault("printTopIssues", "5"));
        queryOverConfig.newEvents = Boolean.parseBoolean(params.getOrDefault("newEvents", "false"));
        queryOverConfig.resurfacedErrors = Boolean.parseBoolean(params.getOrDefault("resurfacedErrors", "false"));
        queryOverConfig.maxErrorVolume = Integer.parseInt(params.getOrDefault("maxErrorVolume", "0"));
        queryOverConfig.maxUniqueErrors = Integer.parseInt(params.getOrDefault("maxUniqueErrors", "0"));
        queryOverConfig.criticalExceptionTypes = params.getOrDefault("criticalExceptionTypes", "");
        queryOverConfig.activeTimespan = params.getOrDefault("activeTimespan", "0");
        queryOverConfig.baselineTimespan = params.getOrDefault("baselineTimespan", "0");
        queryOverConfig.minVolumeThreshold = Integer.parseInt(params.getOrDefault("minVolumeThreshold", "0"));
        queryOverConfig.minErrorRateThreshold = Double.parseDouble(params.getOrDefault("minErrorRateThreshold", "0"));
        queryOverConfig.regressionDelta = Double.parseDouble(params.getOrDefault("regressionDelta", "0"));
        queryOverConfig.criticalRegressionDelta = Double.parseDouble(params.getOrDefault("criticalRegressionDelta", "0"));
        queryOverConfig.applySeasonality = Boolean.parseBoolean(params.getOrDefault("applySeasonality", "false"));
        queryOverConfig.debug = Boolean.parseBoolean(params.getOrDefault("debug", "false"));
        return queryOverConfig;
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
