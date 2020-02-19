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
import com.takipi.api.client.util.regression.*;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;

public class ReportBuilder {
	private ApiClient apiClient;
	private RegressionInput input;
	private Config config;

	public ReportBuilder(ApiClient apiClient, RegressionInput input, Config config) {
		this.apiClient = apiClient;
		this.input = input;
		this.config = config;
	}

	private static class ReportVolume {
		protected List<OOReportEvent> topEvents;
		protected Collection<EventResult> filter;
	}

	private boolean allowEvent(EventResult event, Pattern pattern) {

		if (pattern == null ) {
			return true;
		}

		String json = new Gson().toJson(event);
		boolean result = !pattern.matcher(json).find();

		return result;
	}

	private List<EventResult> getSortedEventsByVolume(Collection<EventResult> events) {

		List<EventResult> result = new ArrayList<EventResult>(events);

		result.sort((o1, o2) -> {
			long v1;
			long v2;

			if (o1.stats != null) {
				v1 = o1.stats.hits;
			} else {
				v1 = 0;
			}

			if (o2.stats != null) {
				v2 = o2.stats.hits;
			} else {
				v2 = 0;
			}

			return (int)(v2 - v1);
		});

		return result;
	}

	private void addEvent(Set<EventResult> events, EventResult event,
		Pattern pattern, PrintStream output, boolean verbose) {

		if (allowEvent(event, pattern)) {
			events.add(event);
		} else if ((output != null) && (verbose)) {
			output.println(event + " did not match regexFilter and was skipped");
		}
	}

	private Collection<EventResult> filterEvents(RateRegression rateRegression,
			Pattern pattern, PrintStream output, boolean verbose) {

		Set<EventResult> result = new HashSet<>();

		if (pattern != null) {

			for (EventResult event : rateRegression.getNonRegressions()) {
				addEvent(result, event, pattern, output, verbose);
			}

			for (EventResult event : rateRegression.getAllNewEvents().values()) {
				addEvent(result, event, pattern, output, verbose);
			}

			for (RegressionResult regressionResult : rateRegression.getAllRegressions().values()) {
				addEvent(result, regressionResult.getEvent(), pattern, output, verbose);

			}

		} else {
			result.addAll(rateRegression.getNonRegressions());
			result.addAll(rateRegression.getAllNewEvents().values());

			for (RegressionResult regressionResult : rateRegression.getAllRegressions().values()) {
				result.add(regressionResult.getEvent());
			}
		}

		return result;
	}

	private ReportVolume getReportVolume(ApiClient apiClient,
			RegressionInput input, RateRegression rateRegression,
			int limit, String regexFilter,  PrintStream output, boolean verbose) {

		ReportVolume result = new ReportVolume();

		Pattern pattern;

		if ((regexFilter != null) && (regexFilter.length() > 0)) {
			pattern = Pattern.compile(regexFilter);
		} else {
			pattern = null;
		}

		Collection<EventResult> eventsSet = filterEvents(rateRegression, pattern, output, verbose);
		List<EventResult> events =  getSortedEventsByVolume(eventsSet);

		if (pattern != null) {
			result.filter = eventsSet;
		}

		result.topEvents = new ArrayList<>();

		for (EventResult event : events) {
			if (event.stats != null) {
				if (result.topEvents.size() < limit) {
					String arcLink = ProcessQualityGates.getArcLink(apiClient, event.id, input, rateRegression.getActiveWndowStart());
					result.topEvents.add(new OOReportEvent(event, arcLink));
				}
			}
		}

		return result;
	}

	public QualityReport build() {
		Integer maxEventVolume = config.getMaxErrorVolume();
		Integer maxUniqueErrors = config.getMaxUniqueErrors();
		int topEventLimit = config.getPrintTopIssues();
		String regexFilter =config.getRegexFilter();
		boolean newEvents= config.isNewEvents();
		boolean resurfacedEvents = config.isResurfacedErrors();
		boolean runRegressions = config.isRegressionPresent();
		boolean markedUnstable = config.isMarkUnstable();
		Context context = DependencyInjector.getImplementation(Context.class);
		PrintStream output = context.getOutputStream().getPrintStream();
		boolean verbose = config.isDebug();


		//check if total or unique gates are being tested
		boolean countGate = false;
		if (maxEventVolume != 0 || maxUniqueErrors != 0) {
			countGate = true;
		}
		boolean checkMaxEventGate = maxEventVolume != 0;
		boolean checkUniqueEventGate = maxUniqueErrors != 0;

		boolean failedAPI = false;

		//get the CICD quality report for all gates but Regressions
		//initialize the QualityGateReport so we don't get null pointers below
		QualityGateReport qualityGateReport = new QualityGateReport();
		if (countGate || newEvents || resurfacedEvents || regexFilter != null) {
			try {
				qualityGateReport = ProcessQualityGates.processCICDInputs(apiClient, input, newEvents, resurfacedEvents,
					regexFilter, topEventLimit, countGate, output, verbose);
			} catch (Exception e) {
				if (output != null) {
					output.println("Error processing CI CD inputs " + e.getMessage());
				}

				failedAPI = true;
			}
		}

		//run the regression gate
		ReportVolume reportVolume;
		RateRegression rateRegression = null;
		List<OOReportRegressedEvent> regressions = null;
		boolean hasRegressions = false;
		if (runRegressions) {
			rateRegression = RegressionUtil.calculateRateRegressions(apiClient, input, output, verbose);

			reportVolume = getReportVolume(apiClient, input,
					rateRegression, topEventLimit, regexFilter, output, verbose);

			regressions = getAllRegressions(rateRegression, reportVolume.filter);
			if (regressions != null && regressions.size() > 0) {
				hasRegressions = true;
				replaceSourceId(regressions);
			}
		}

		//max total error gate
		boolean maxVolumeExceeded = (maxEventVolume != 0) && (qualityGateReport.getTotalErrorCount() > maxEventVolume);

		//max unique error gate
		long uniqueEventCount;
		boolean maxUniqueErrorsExceeded;
		if (maxUniqueErrors != 0) {
			uniqueEventCount = qualityGateReport.getUniqueErrorCount();
			maxUniqueErrorsExceeded = uniqueEventCount > maxUniqueErrors;
		} else {
			uniqueEventCount = 0;
			maxUniqueErrorsExceeded = false;
		}

		boolean newErrors = processQualityGateErrors(qualityGateReport.getNewErrors());
		boolean resurfaced = processQualityGateErrors(qualityGateReport.getResurfacedErrors());
		boolean critical = processQualityGateErrors(qualityGateReport.getCriticalErrors());
		processQualityGateErrors(qualityGateReport.getTopErrors());

		boolean checkCritical = false;
		if (input.criticalExceptionTypes != null && input.criticalExceptionTypes.size() > 0) {
			checkCritical = true;
		}

		boolean unstable = (failedAPI)
				|| (hasRegressions)
				|| (maxVolumeExceeded)
				|| (maxUniqueErrorsExceeded)
				|| (newErrors)
				|| (resurfaced)
				|| (critical);

		return new QualityReport(input, rateRegression, regressions,
				qualityGateReport.getCriticalErrors(), qualityGateReport.getTopErrors(), qualityGateReport.getNewErrors(),
				qualityGateReport.getResurfacedErrors(), qualityGateReport.getTotalErrorCount(),
				qualityGateReport.getUniqueErrorCount(), unstable, newEvents, resurfacedEvents, checkCritical, checkMaxEventGate,
				checkUniqueEventGate, runRegressions, maxEventVolume, maxUniqueErrors, markedUnstable);
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
     * @param events
     */
	private void replaceSourceId (List<? extends OOReportEvent> events) {
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

		for (RegressionResult regressionResult : rateRegression.getCriticalRegressions().values()) {

			if ((filter != null) && (!filter.contains(regressionResult.getEvent()))) {
				continue;
			}

			String arcLink = ProcessQualityGates.getArcLink(apiClient, regressionResult.getEvent().id, input, rateRegression.getActiveWndowStart());

			OOReportRegressedEvent regressedEvent = new OOReportRegressedEvent(regressionResult.getEvent(),
					regressionResult.getBaselineHits(), regressionResult.getBaselineInvocations(), RegressionStringUtil.SEVERE_REGRESSION, arcLink);

			result.add(regressedEvent);
		}

		for (RegressionResult regressionResult : rateRegression.getAllRegressions().values()) {

			if (rateRegression.getCriticalRegressions().containsKey(regressionResult.getEvent().id)) {
				continue;
			}

			String arcLink = ProcessQualityGates.getArcLink(apiClient, regressionResult.getEvent().id, input, rateRegression.getActiveWndowStart());

			OOReportRegressedEvent regressedEvent = new OOReportRegressedEvent(regressionResult.getEvent(),
					regressionResult.getBaselineHits(), regressionResult.getBaselineInvocations(), RegressionStringUtil.REGRESSION, arcLink);

			result.add(regressedEvent);
		}

		return result;
	}
}
