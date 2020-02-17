package com.overops.plugins.model;

import com.takipi.common.util.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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

    public QueryOverConfig(String[] args) {
        Map<String, String> map = argsToMap(args);

        overOpsURL = map.get("overOpsURL");
        overOpsSID = map.get("overOpsSID");
        overOpsAPIKey = map.get("overOpsAPIKey");
        applicationName = map.get("applicationName");
        deploymentName = map.get("deploymentName");
        serviceId = map.getOrDefault("serviceId", overOpsSID);
        regexFilter = map.getOrDefault("regexFilter", "");
        markUnstable = Boolean.parseBoolean(map.getOrDefault("markUnstable", "false"));
        printTopIssues = Integer.parseInt(map.getOrDefault("printTopIssues", "5"));
        newEvents = Boolean.parseBoolean(map.getOrDefault("newEvents", "false"));
        resurfacedErrors = Boolean.parseBoolean(map.getOrDefault("resurfacedErrors", "false"));
        maxErrorVolume = Integer.parseInt(map.getOrDefault("maxErrorVolume", "0"));
        maxUniqueErrors = Integer.parseInt(map.getOrDefault("maxUniqueErrors", "0"));
        criticalExceptionTypes = map.getOrDefault("criticalExceptionTypes", "");
        activeTimespan = map.getOrDefault("activeTimespan", "0");
        baselineTimespan = map.getOrDefault("baselineTimespan", "0");
        minVolumeThreshold = Integer.parseInt(map.getOrDefault("minVolumeThreshold", "0"));
        minErrorRateThreshold = Double.parseDouble(map.getOrDefault("minErrorRateThreshold", "0"));
        regressionDelta = Double.parseDouble(map.getOrDefault("regressionDelta", "0"));
        criticalRegressionDelta = Double.parseDouble(map.getOrDefault("criticalRegressionDelta", "0"));
        applySeasonality = Boolean.parseBoolean(map.getOrDefault("applySeasonality", "false"));
        debug = Boolean.parseBoolean(map.getOrDefault("debug", "false"));
    }

    private Map<String, String> argsToMap(String[] args) {
        final String parameterDeclaration = "--";
        return Arrays.stream(args).filter(e -> e.startsWith(parameterDeclaration) && e.contains("="))
                .map(e -> e.substring(parameterDeclaration.length()))
                .map(e -> {
                    int i = e.indexOf("=");
                    return Pair.of(e.substring(0, i), e.substring(++i));
                }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    public String getOverOpsURL() {
        return overOpsURL;
    }

    public String getOverOpsSID() {
        return overOpsSID;
    }

    public String getOverOpsAPIKey() {
        return overOpsAPIKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getRegexFilter() {
        return regexFilter;
    }

    public boolean isMarkUnstable() {
        return markUnstable;
    }

    public Integer getPrintTopIssues() {
        return printTopIssues;
    }

    public boolean isNewEvents() {
        return newEvents;
    }

    public boolean isResurfacedErrors() {
        return resurfacedErrors;
    }

    public Integer getMaxErrorVolume() {
        return maxErrorVolume;
    }

    public Integer getMaxUniqueErrors() {
        return maxUniqueErrors;
    }

    public String getCriticalExceptionTypes() {
        return criticalExceptionTypes;
    }

    public String getActiveTimespan() {
        return activeTimespan;
    }

    public String getBaselineTimespan() {
        return baselineTimespan;
    }

    public Integer getMinVolumeThreshold() {
        return minVolumeThreshold;
    }

    public Double getMinErrorRateThreshold() {
        return minErrorRateThreshold;
    }

    public Double getRegressionDelta() {
        return regressionDelta;
    }

    public Double getCriticalRegressionDelta() {
        return criticalRegressionDelta;
    }

    public boolean isApplySeasonality() {
        return applySeasonality;
    }

    public boolean isDebug() {
        return debug;
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
