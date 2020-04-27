package com.overops.plugins.model;

import com.overops.report.service.QualityReportParams;
import com.takipi.common.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {
    private String overOpsURL;
    private String overOpsSID;
    private String overOpsAPIKey;
    private String serviceId;
    private QualityReportParams reportParams;

    public Config(String[] args) {
        Map<String, String> argsMap = argsToMap(args);
        QualityReportParams params = new QualityReportParams();

        params.setApplicationName(argsMap.get("applicationName"));
        params.setDeploymentName(argsMap.get("deploymentName"));
        params.setServiceId(argsMap.getOrDefault("serviceId", argsMap.get("overOpsSID")));
        params.setRegexFilter(argsMap.getOrDefault("regexFilter", ""));
        params.setMarkUnstable(Boolean.parseBoolean(argsMap.getOrDefault("markUnstable", "false")));
        params.setPrintTopIssues(Integer.parseInt(argsMap.getOrDefault("printTopIssues", "5")));
        params.setNewEvents(Boolean.parseBoolean(argsMap.getOrDefault("newEvents", "false")));
        params.setResurfacedErrors(Boolean.parseBoolean(argsMap.getOrDefault("resurfacedErrors", "false")));
        params.setMaxErrorVolume(Integer.parseInt(argsMap.getOrDefault("maxErrorVolume", "0")));
        params.setMaxUniqueErrors(Integer.parseInt(argsMap.getOrDefault("maxUniqueErrors", "0")));
        params.setCriticalExceptionTypes(argsMap.getOrDefault("criticalExceptionTypes", ""));
        params.setCriticalRegressionDelta(Double.parseDouble(argsMap.getOrDefault("criticalRegressionDelta", "0")));
        params.setActiveTimespan(argsMap.getOrDefault("activeTimespan", "0"));
        params.setBaselineTimespan(argsMap.getOrDefault("baselineTimespan", "0"));
        params.setMinVolumeThreshold(Integer.parseInt(argsMap.getOrDefault("minVolumeThreshold", "0")));
        params.setMinErrorRateThreshold(Double.parseDouble(argsMap.getOrDefault("minErrorRateThreshold", "0")));
        params.setRegressionDelta(Double.parseDouble(argsMap.getOrDefault("regressionDelta", "0")));
        params.setApplySeasonality(Boolean.parseBoolean(argsMap.getOrDefault("applySeasonality", "false")));
        params.setDebug(Boolean.parseBoolean(argsMap.getOrDefault("debug", "false")));

        reportParams = params;
        overOpsURL = argsMap.get("overOpsURL");
        overOpsSID = argsMap.get("overOpsSID");
        overOpsAPIKey = argsMap.get("overOpsAPIKey");
        serviceId = argsMap.getOrDefault("serviceId", overOpsSID);
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

    public String getServiceId() {
        return serviceId;
    }

    public QualityReportParams getReportParams() {
        return reportParams;
    }
}
