package com.overops.plugins.service;

import com.overops.plugins.model.QueryOverOps;
import com.overops.plugins.service.impl.ReportBuilder;

import java.io.IOException;

public interface OverOpsService {
    ReportBuilder.QualityReport perform(QueryOverOps queryOverOps) throws IOException, InterruptedException;
}
