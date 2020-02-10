package com.overops.plugins.service.impl;

import com.google.gson.Gson;
import com.overops.plugins.model.Metadata;
import com.overops.plugins.model.OOReportRegressedEvent;
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

	public static class QualityReport {

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

		protected QualityReport(RegressionInput input, RateRegression regression,
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

		public String getDeploymentName() {
			String value = getInput().deployments.toString();
			value = value.replace("[", "");
			value = value.replace("]", "");
			return value;
		}

		public List<Metadata> getMetadata() {
			List<Metadata> metadata = null;
			if (Objects.nonNull(getNewIssues()) && getNewIssues().size() > 0) {
				metadata = getNewIssues().stream().map(OOReportEvent::getEvent).filter(Objects::nonNull).filter(e -> Objects.nonNull(e.id))
						.max(Comparator.comparingLong(e -> Long.parseLong(e.id))).map(this::createMeta).orElse(new ArrayList<>());
			}

			if (Objects.isNull(metadata) && (Objects.nonNull(getAllIssues()) && getAllIssues().size() > 0)) {
				metadata = getAllIssues().stream().map(OOReportEvent::getEvent).filter(Objects::nonNull).filter(e -> Objects.nonNull(e.id))
						.max(Comparator.comparingLong(e -> Long.parseLong(e.id))).map(this::createMeta).orElse(new ArrayList<>());
			}

			return Optional.ofNullable(metadata).orElse(new ArrayList<>());
		}

		private List<Metadata> createMeta(EventResult e) {
			return Arrays.asList(new Metadata("type", e.type), new Metadata("summary", e.summary),new Metadata("name", e.name));
		}
	}

	private static class ReportVolume {
		protected List<OOReportEvent> topEvents;
		protected Collection<EventResult> filter;

	}

	private static boolean allowEvent(EventResult event, Pattern pattern) {

		if (pattern == null ) {
			return true;
		}

		String json = new Gson().toJson(event);
		boolean result = !pattern.matcher(json).find();

		return result;
	}

	private static List<EventResult> getSortedEventsByVolume(Collection<EventResult> events) {

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

	private static void addEvent(Set<EventResult> events, EventResult event,
		Pattern pattern, PrintStream output, boolean verbose) {

		if (allowEvent(event, pattern)) {
			events.add(event);
		} else if ((output != null) && (verbose)) {
			output.println(event + " did not match regexFilter and was skipped");
		}
	}

	private static Collection<EventResult> filterEvents(RateRegression rateRegression,
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

	private static ReportVolume getReportVolume(ApiClient apiClient,
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

	/*
	 * Entry point into report engine
	 */
	public static QualityReport execute(ApiClient apiClient, RegressionInput input,
			Integer maxEventVolume, Integer maxUniqueErrors, int topEventLimit, String regexFilter,
			boolean newEvents, boolean resurfacedEvents, boolean runRegressions, boolean markedUnstable, PrintStream output, boolean verbose) {

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

			regressions = getAllRegressions(apiClient, input, rateRegression, reportVolume.filter);
			if (regressions != null && regressions.size() > 0) {
				hasRegressions = true;
				replaceSourceId2(regressions);
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

		//new error gate
		boolean newErrors = false;
		if (qualityGateReport.getNewErrors() != null && qualityGateReport.getNewErrors().size() > 0) {
			newErrors = true;
			replaceSourceId(qualityGateReport.getNewErrors());
		}

		//resurfaced error gate
		boolean resurfaced = false;
		if (qualityGateReport.getResurfacedErrors() != null && qualityGateReport.getResurfacedErrors().size() > 0) {
			resurfaced = true;
			replaceSourceId(qualityGateReport.getResurfacedErrors());
		}

		//critical error gate
		boolean critical = false;
		if (qualityGateReport.getCriticalErrors() != null  && qualityGateReport.getCriticalErrors().size() > 0) {
			critical = true;
			replaceSourceId(qualityGateReport.getCriticalErrors());
		}

		//top errors
		if (qualityGateReport.getTopErrors() != null  && qualityGateReport.getTopErrors().size() > 0) {
			replaceSourceId(qualityGateReport.getTopErrors());
		}

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

	//for each event, replace the source ID in the ARC link with the number 4 (which means Jenkins)
	private static void replaceSourceId (List<OOReportEvent> events) {
		for (OOReportEvent ooReportEvent : events) {
			String arcLink = replaceSourceIdInArcLink(ooReportEvent.getARCLink());
			ooReportEvent.setArcLink(arcLink);
		}
	}

	//for each event, replace the source ID in the ARC link with the number 4 (which means Jenkins)
	private static void replaceSourceId2 (List<OOReportRegressedEvent> events) {
		for (OOReportEvent ooReportEvent : events) {
			String arcLink = replaceSourceIdInArcLink(ooReportEvent.getARCLink());
			ooReportEvent.setArcLink(arcLink);
		}
	}

	private static String replaceSourceIdInArcLink(String arcLink) {
		if (arcLink == null) {
			return arcLink;
		}
		String returnString;
		CharSequence target = "source=43";
		CharSequence replacement = "source=4";

		returnString = arcLink.replace(target, replacement);

		return returnString;
	}

	private static List<OOReportRegressedEvent> getAllRegressions(ApiClient apiClient,
			RegressionInput input, RateRegression rateRegression, Collection<EventResult> filter) {

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
