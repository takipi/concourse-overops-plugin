package com.overops.plugins.service;

import com.overops.plugins.model.QualityReport;
import com.overops.plugins.model.Config;

import java.io.IOException;

public interface OverOpsService {
    QualityReport produceReport(Config config);
}
