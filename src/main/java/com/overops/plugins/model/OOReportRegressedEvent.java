package com.overops.plugins.model;

import com.takipi.api.client.util.cicd.OOReportEvent;
import com.takipi.api.client.util.regression.RegressionResult;
import com.takipi.api.client.util.regression.RegressionStringUtil;

public class OOReportRegressedEvent extends OOReportEvent {
    private final long baselineHits;
    private final long baselineInvocations;

    public OOReportRegressedEvent(RegressionResult regressionResult, String type, String arcLink){
        super(regressionResult.getEvent(), type, arcLink);
        this.baselineHits = regressionResult.getBaselineHits();
        this.baselineInvocations = regressionResult.getBaselineInvocations();
    };

    @Override
    public String getEventRate() {
        return RegressionStringUtil.getRegressedEventRate(getEvent(), baselineHits, baselineInvocations);
    }
}
