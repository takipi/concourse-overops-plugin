package com.overops.plugins.service;

import com.overops.plugins.model.QualityReport;
import com.overops.plugins.model.QueryOverConfig;

import java.io.IOException;

public interface OverOpsService {
    QualityReport perform(QueryOverConfig queryOverConfig) throws IOException, InterruptedException;
}
