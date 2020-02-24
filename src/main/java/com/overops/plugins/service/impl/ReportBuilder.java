package com.overops.plugins.service.impl;

import com.google.gson.Gson;
import com.overops.plugins.Context;
import com.overops.plugins.DependencyInjector;
import com.overops.plugins.model.Config;
import com.overops.plugins.model.OOReportRegressedEvent;
import com.overops.plugins.model.QualityReport;
import com.takipi.api.client.ApiClient;
import com.takipi.api.client.result.event.EventResult;
import com.takipi.api.client.util.cicd.OOReportEvent;
import com.takipi.api.client.util.cicd.ProcessQualityGates;
import com.takipi.api.client.util.cicd.QualityGateReport;
import com.takipi.api.client.util.regression.RateRegression;
import com.takipi.api.client.util.regression.RegressionInput;
import com.takipi.api.client.util.regression.RegressionResult;
import com.takipi.api.client.util.regression.RegressionUtil;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.takipi.api.client.util.regression.RegressionStringUtil.REGRESSION;
import static com.takipi.api.client.util.regression.RegressionStringUtil.SEVERE_REGRESSION;

public class ReportBuilder {
    private ApiClient apiClient;
    private RegressionInput input;
    private Config config;
    private Context context;

    public ReportBuilder(ApiClient apiClient, RegressionInput input, Config config) {
        this.apiClient = apiClient;
        this.input = input;
        this.config = config;
        context = DependencyInjector.getImplementation(Context.class);
    }

    private boolean isPatternMatched(EventResult event, Pattern pattern) {
        String json = new Gson().toJson(event);
        boolean result = !pattern.matcher(json).find();

        return result;
    }

    private void addIfMatchPattern(Set<EventResult> events, EventResult event, Pattern pattern) {
        if (isPatternMatched(event, pattern)) {
            events.add(event);
        } else {
            printlnForDebug(event + " did not match regexFilter and was skipped");
        }
    }

    private void printlnForDebug(String message) {
        if (config.isDebug()) {
            context.getOutputStream().printlnDebug(message);
        }
    }

    private Collection<EventResult> filterEvents(RateRegression rateRegression, Pattern pattern) {
        Set<EventResult> result = new HashSet<>();

        if (pattern != null) {
            rateRegression.getNonRegressions().stream()
                    .forEach(eventResult -> addIfMatchPattern(result, eventResult, pattern));
            rateRegression.getAllNewEvents().values().stream()
                    .forEach(eventResult -> addIfMatchPattern(result, eventResult, pattern));
            rateRegression.getAllRegressions().values().stream()
                    .forEach(regressionResult -> addIfMatchPattern(result, regressionResult.getEvent(), pattern));
        } else {
            result.addAll(rateRegression.getNonRegressions());
            result.addAll(rateRegression.getAllNewEvents().values());
            rateRegression.getAllRegressions().values().stream()
                    .forEach(regressionResult -> result.add(regressionResult.getEvent()));
        }

        return result;
    }

    private ReportVolume getReportVolume(RateRegression rateRegression) {
        ReportVolume result = new ReportVolume();

        Pattern pattern = getPattern(config.getRegexFilter());
        Collection<EventResult> eventsSet = filterEvents(rateRegression, pattern);

        if (pattern != null) {
            result.filter = eventsSet;
        }

        eventsSet.stream()
                .filter(eventResult -> eventResult.stats != null)
                .sorted((eventResult1, eventResult2) -> (int)(eventResult2.stats.hits - eventResult1.stats.hits))
                .limit(config.getPrintTopIssues())
                .forEach(eventResult -> {
                    String arcLink = ProcessQualityGates.getArcLink(apiClient, eventResult.id, input, rateRegression.getActiveWndowStart());
                    result.topEvents.add(new OOReportEvent(eventResult, arcLink));
                });

        return result;
    }

    private Pattern getPattern(String regexFilter) {
        Pattern pattern;

        if ((regexFilter != null) && (regexFilter.length() > 0)) {
            pattern = Pattern.compile(regexFilter);
        } else {
            pattern = null;
        }
        return pattern;
    }

    public QualityReport build() {
        boolean failedAPI = false;
        QualityGateReport qualityGateReport = new QualityGateReport();
        if (config.isSomeGateBesideRegressionToProcess()) {
            try {
                qualityGateReport = ProcessQualityGates.processCICDInputs(apiClient, input, config.isNewEvents(),
                        config.isResurfacedErrors(), config.getRegexFilter(), config.getPrintTopIssues(),
                        config.isCountGatePresent(), context.getOutputStream().getPrintStream(), config.isDebug());
            } catch (Exception e) {
                printlnForDebug("Error processing CI CD inputs " + e.getMessage());
                failedAPI = true;
            }
        }

        boolean newErrors = processQualityGateErrors(qualityGateReport.getNewErrors());
        boolean resurfaced = processQualityGateErrors(qualityGateReport.getResurfacedErrors());
        boolean critical = processQualityGateErrors(qualityGateReport.getCriticalErrors());
        processQualityGateErrors(qualityGateReport.getTopErrors());

        //run the regression gate
        RateRegression rateRegression = null;
        List<OOReportRegressedEvent> regressions = null;
        boolean hasRegressions = false;
        if (config.isRegressionPresent()) {
            rateRegression = RegressionUtil.calculateRateRegressions(apiClient, input, context.getOutputStream().getPrintStream(), config.isDebug());
            ReportVolume reportVolume = getReportVolume(rateRegression);
            regressions = getAllRegressions(rateRegression, reportVolume.filter);

            if (regressions != null && regressions.size() > 0) {
                hasRegressions = true;
                replaceSourceId(regressions);
            }
        }

        boolean unstable = (failedAPI)
                || (hasRegressions)
                || config.checkIfMaxVolumeExceeded(qualityGateReport.getTotalErrorCount())
                || (config.checkIfMaxUniqueErrorsExceeded(qualityGateReport.getUniqueErrorCount()))
                || (newErrors)
                || (resurfaced)
                || (critical);

        return new QualityReport(input, rateRegression, regressions,
                qualityGateReport, unstable, config);
    }

    private boolean processQualityGateErrors(List<OOReportEvent> events) {
        boolean errorsExist = false;
        if (events != null && events.size() > 0) {
            errorsExist = true;
            replaceSourceId(events);
        }
        return errorsExist;
    }

    /**
     * for each event, replace the source ID in the ARC link with the number 4 (which means Jenkins)
     * see: https://overopshq.atlassian.net/wiki/spaces/PP/pages/1529872385/Hit+Sources
     *
     * @param events
     */
    private void replaceSourceId(List<? extends OOReportEvent> events) {
        String match = "&source=(\\d)+"; // matches at least one digit
        String replace = "&source=47";    // replace with 4

        for (OOReportEvent ooReportEvent : events) {
            String arcLink = ooReportEvent.getARCLink();
            if (arcLink != null) {
                ooReportEvent.setArcLink(arcLink.replaceAll(match, replace));
            }
        }
    }

    private List<OOReportRegressedEvent> getAllRegressions(RateRegression rateRegression, Collection<EventResult> filter) {
        List<OOReportRegressedEvent> result = new ArrayList<OOReportRegressedEvent>();

        BiConsumer<RegressionResult, String> addToResult = (regressionResult, type) -> {
            String arcLink = ProcessQualityGates.getArcLink(apiClient, regressionResult.getEvent().id, input, rateRegression.getActiveWndowStart());
            result.add(new OOReportRegressedEvent(regressionResult, type, arcLink));
        };

        if (filter != null) {
            rateRegression.getCriticalRegressions().values().stream()
                    .filter(regressionResult -> filter.contains(regressionResult.getEvent()))
                    .forEach(regressionResult -> {
                        addToResult.accept(regressionResult, SEVERE_REGRESSION);
                    });
        }

        rateRegression.getAllRegressions().values().stream()
                .filter(regressionResult -> !rateRegression.getCriticalRegressions().containsKey(regressionResult.getEvent().id))
                .forEach(regressionResult -> {
                    addToResult.accept(regressionResult, REGRESSION);
                });

        return result;
    }

    private static class ReportVolume {
        protected List<OOReportEvent> topEvents;
        protected Collection<EventResult> filter;

        public ReportVolume() {
            this.topEvents = new ArrayList<>();;
        }
    }
}
