package com.overops.plugins.service.impl;

import com.google.gson.Gson;
import com.overops.plugins.model.*;
import com.takipi.api.client.ApiClient;
import com.takipi.api.client.data.event.MainEventStats;
import com.takipi.api.client.functions.input.ReliabilityReportInput;
import com.takipi.api.client.functions.output.*;
import com.takipi.api.client.result.event.EventResult;
import com.takipi.api.client.util.cicd.OOReportEvent;
import com.takipi.api.client.util.regression.*;
import org.joda.time.DateTime;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.takipi.api.client.util.cicd.ProcessQualityGates.getArcLink;

public class ReportBuilder {
	private static long totalErrorCount = 0;
	private static int uniqueErrorCount = 0;
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

			this.eventVolume = eventVolume;
			this.uniqueEventsCount = uniqueEventCounts;
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

		public Version getMaxVersion() {
			Version version = null;
			if (Objects.nonNull(getNewIssues()) && getNewIssues().size() > 0) {
				version = getNewIssues().stream().map(OOReportEvent::getEvent).filter(Objects::nonNull).filter(e -> Objects.nonNull(e.id))
						.max(Comparator.comparingLong(e -> Long.parseLong(e.id))).map(this::createVersion).orElse(new Version());
			}

			if (Objects.isNull(version) && (Objects.nonNull(getAllIssues()) && getAllIssues().size() > 0)) {
				version = getAllIssues().stream().map(OOReportEvent::getEvent).filter(Objects::nonNull).filter(e -> Objects.nonNull(e.id))
						.max(Comparator.comparingLong(e -> Long.parseLong(e.id))).map(this::createVersion).orElse(new Version());
			}

			return Optional.ofNullable(version).orElse(new Version(new Event()));
		}

		private Version createVersion(EventResult e) {
			Version v = new Version(new Event(e.id));
			v.addMetadata(new Metadata("type", e.type));
			v.addMetadata(new Metadata("summary", e.summary));
			v.addMetadata(new Metadata("name", e.name));
			return v;
		}
	}

	public static ReportBuilder.QualityReport execute(ApiClient apiClient, QueryOverOps properties, ReliabilityReport reliabilityReport,
													  ReliabilityReportInput input, DateTime activeWindowStart, boolean runRegression, PrintStream output) {
		boolean countGate = false;
		if (properties.getMaxErrorVolume() != 0 || properties.getMaxUniqueErrors() != 0) {
			countGate = true;
		}
		boolean checkMaxEventGate = properties.getMaxErrorVolume() != 0;
		boolean checkUniqueEventGate = properties.getMaxUniqueErrors() != 0;

		ReliabilityReport.ReliabilityReportItem report = reliabilityReport.items.values().iterator().next();
		List<SeriesRow> rows = new ArrayList<>();
		if (report.errors != null) {
			rows = report.errors.readRows();
		}

		if (report.failures != null) {
			rows.addAll(report.failures.readRows());
		}
		List<EventRow> errors = filterEvents(rows, properties.getRegexFilter());
		errors.sort((o1, o2) -> (int) o2.hits - (int) o1.hits);

		List<OOReportEvent> topErrors = new ArrayList<>();
		List<OOReportEvent> resurfacedErrors = new ArrayList<>();
		List<OOReportEvent> criticalErrors = new ArrayList<>();

		for(EventRow row : errors) {
			if (countGate && topErrors.size() <= properties.getPrintTopIssues()) {
				String link = Optional.ofNullable(row.id).map(id -> id.split(",")[0])
						.map(id -> getArcLink(apiClient, id, input.regressionInput, activeWindowStart))
						.map(ReportBuilder::replaceSourceIdInArcLink).orElse("");
				topErrors.add(convertResult(row, link));
			}

			if (properties.isResurfacedErrors() && row.labels.contains(RegressionStringUtil.RESURFACED_ISSUE)) {
				String link = Optional.ofNullable(row.id).map(id -> id.split(",")[0])
						.map(id -> getArcLink(apiClient, id, input.regressionInput, activeWindowStart))
						.map(ReportBuilder::replaceSourceIdInArcLink).orElse("");
				resurfacedErrors.add(convertResult(row, link));
			}

			if (input.regressionInput.criticalExceptionTypes != null && input.regressionInput.criticalExceptionTypes.contains(row.name)) {
				String link = Optional.ofNullable(row.id).map(id -> id.split(",")[0])
						.map(id -> getArcLink(apiClient, id, input.regressionInput, activeWindowStart))
						.map(ReportBuilder::replaceSourceIdInArcLink).orElse("");
				criticalErrors.add(convertResult(row, link));
			}
		}

		List<OOReportEvent> newErrors = report.getNewErrors(properties.isNewEvents(), properties.isNewEvents()).stream().map(row -> {
			String link = Optional.ofNullable(row.id).map(id -> id.split(",")[0])
					.map(id -> getArcLink(apiClient, id, input.regressionInput, activeWindowStart))
					.map(ReportBuilder::replaceSourceIdInArcLink).orElse("");
			return convertResult(row, link);
		}).collect(Collectors.toList());

		List<OOReportRegressedEvent> regressionErrors = report.geIncErrors(runRegression, runRegression).stream().map(row -> {
			String link = Optional.ofNullable(row.id).map(id -> id.split(",")[0])
					.map(id -> getArcLink(apiClient, id, input.regressionInput, activeWindowStart))
					.map(ReportBuilder::replaceSourceIdInArcLink).orElse("");
			return convertResult(row, link);
		}).map(e -> new OOReportRegressedEvent(e.getEvent(), 0,0, e.getType(), e.getARCLink()))
				 .collect(Collectors.toList());
		boolean hasRegressions = false;

		if (!regressionErrors.isEmpty()) {
			hasRegressions = true;
		}

		boolean maxVolumeExceeded = (properties.getMaxErrorVolume() != 0) && (totalErrorCount > properties.getMaxErrorVolume());

		//max unique error gate
		long uniqueEventCount;
		boolean maxUniqueErrorsExceeded;
		if (properties.getMaxUniqueErrors() != 0) {
			uniqueEventCount = uniqueErrorCount;
			maxUniqueErrorsExceeded = uniqueEventCount > properties.getMaxUniqueErrors();
		} else {
			uniqueEventCount = 0;
			maxUniqueErrorsExceeded = false;
		}

		//new error gate
		boolean newError = false;
		if (newErrors.size() > 0) {
			newError = true;
		}

		//resurfaced error gate
		boolean resurfaced = false;
		if (resurfacedErrors.size() > 0) {
			resurfaced = true;
		}

		//critical error gate
		boolean critical = false;
		if (criticalErrors.size() > 0) {
			critical = true;
		}

		//top errors
		if (topErrors.size() > 0) {
		}

		boolean checkCritical = false;
		if (input.regressionInput.criticalExceptionTypes != null && input.regressionInput.criticalExceptionTypes.size() > 0) {
			checkCritical = true;
		}

		boolean unstable = (hasRegressions)
				|| (maxVolumeExceeded)
				|| (maxUniqueErrorsExceeded)
				|| (newError)
				|| (resurfaced)
				|| (critical);
		return new ReportBuilder.QualityReport(input.regressionInput, null, regressionErrors,
				criticalErrors, topErrors, newErrors,
				resurfacedErrors, totalErrorCount,
				uniqueErrorCount, unstable, properties.isNewEvents(), properties.isResurfacedErrors(), checkCritical, checkMaxEventGate,
				checkUniqueEventGate, runRegression, properties.getMaxErrorVolume(), properties.getMaxUniqueErrors(), properties.isMarkUnstable());
	}

	private static List<EventRow> filterEvents(List<SeriesRow> events, String regexFilter) {
		if (regexFilter == null) {
			return events.stream().map(e -> (EventRow) e).collect(Collectors.toList());
		}

		List<EventRow> returnEvents = new ArrayList<>();
		for (SeriesRow event : events) {
			if (evaluateEvent(event, getPattern(regexFilter))) {
				EventRow e = (EventRow) event;
				returnEvents.add(e);
				// now increment counters
				totalErrorCount += e.hits;
				uniqueErrorCount++;
			}
		}
		return returnEvents;
	}

	private static boolean evaluateEvent(SeriesRow event, Pattern pattern) {
		if (pattern == null) {
			return true;
		}

		String json = new Gson().toJson(event);

		return !pattern.matcher(json).find();
	}

	private static Pattern getPattern(String regexFilter) {
		Pattern pattern;

		if ((regexFilter != null) && (regexFilter.length() > 0)) {
			pattern = Pattern.compile(regexFilter);
		} else {
			pattern = null;
		}
		return pattern;
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

	private static OOReportEvent convertResult(BaseEventRow e, String link) {
		EventResult result = new EventResult();
		MainEventStats stats = new MainEventStats();
		stats.hits = e.hits;
		stats.invocations = e.invocations;
		result.id = e.id;
		Optional.ofNullable(e.labels).map(labels -> labels.split(",")).map(Arrays::asList)
				.ifPresent(labels -> result.labels = labels);
		Optional.ofNullable(e.similar_event_ids).map(events -> events.split(",")).map(Arrays::asList)
				.ifPresent(events -> result.similar_event_ids = events);
		result.name = e.name;
		result.summary = e.summary;
		result.type = e.type;
		result.message = e.message;
		result.first_seen = String.valueOf(e.first_seen);
		result.introduced_by = e.introduced_by;
		result.jira_issue_url = e.jira_issue_url;
		result.stats = stats;
		return new OOReportEvent(result, e.type, link);
	}
}
