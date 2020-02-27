package com.overops.plugins.model;

import com.takipi.common.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config {
    private static final String SEPARATOR = ",";

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
    private String activeTimespan;
    private String baselineTimespan;
    private Integer minVolumeThreshold;
    private Double minErrorRateThreshold;
    private Double regressionDelta;
    private Double criticalRegressionDelta;
    private boolean applySeasonality;

    private boolean debug = false;

    public Config(String[] args) {
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

    public Collection<String> getApplicationCollection() {
        return parseArrayString(applicationName);
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public Collection<String> getDeploymentCollection() {
        return parseArrayString(deploymentName);
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

    public boolean isMaxErrorVolume() {
        return getMaxErrorVolume() != 0;
    }

    public boolean checkIfMaxVolumeExceeded(long totalErrorsCount) {
        return isMaxErrorVolume() && totalErrorsCount > getMaxErrorVolume();
    }

    public Integer getMaxUniqueErrors() {
        return maxUniqueErrors;
    }

    public boolean isMaxUniqueErrors() {
        return getMaxUniqueErrors() != 0;
    }

    public boolean checkIfMaxUniqueErrorsExceeded(long uniqueErrorCount) {
        return isMaxUniqueErrors() && uniqueErrorCount > getMaxUniqueErrors();
    }

    public boolean isCountGatePresent() {
        return getMaxErrorVolume() != 0 || getMaxUniqueErrors() != 0;
    }

    public boolean isSomeGateBesideRegressionToProcess() {
        return isCountGatePresent() || isNewEvents() || isResurfacedErrors() || getRegexFilter() != null;
    }

    public String getCriticalExceptionTypes() {
        return criticalExceptionTypes;
    }

    public Collection<String> getCriticalExceptionTypesCollection() {
        return parseArrayString(criticalExceptionTypes);
    }

    public String getActiveTimespan() {
        return activeTimespan;
    }

    public int getActiveTimespanMinutes() {
        return convertToMinutes(activeTimespan);
    }

    public String getBaselineTimespan() {
        return baselineTimespan;
    }

    public int getBaselineTimespanMinutes() {
        return convertToMinutes(baselineTimespan);
    }

    public boolean isRegressionPresent() {
        return getBaselineTimespanMinutes() > 0;
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

    private int convertToMinutes(String timeWindow) {

        if (StringUtils.isEmpty(timeWindow)) {
            return 0;
        }

        if (timeWindow.toLowerCase().contains("d")) {
            int days = Integer.parseInt(timeWindow.substring(0, timeWindow.indexOf("d")));
            return days * 24 * 60;
        } else if (timeWindow.toLowerCase().contains("h")) {
            int hours = Integer.parseInt(timeWindow.substring(0, timeWindow.indexOf("h")));
            return hours * 60;
        } else if (timeWindow.toLowerCase().contains("m")) {
            return Integer.parseInt(timeWindow.substring(0, timeWindow.indexOf("m")));
        }

        return 0;
    }

    private static Collection<String> parseArrayString(String value) {
        if (StringUtils.isEmpty(value)) {
            return Collections.emptySet();
        }

        return Arrays.asList(value.trim().split(Pattern.quote(SEPARATOR)));
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
