package com.overops.plugins.step;

import com.overops.plugins.DependencyInjector;
import com.overops.plugins.model.Config;
import com.overops.plugins.model.QualityReport;
import com.overops.plugins.service.OverOpsService;

import java.io.IOException;

public class CreateReportStep extends Step<Config, QualityReport> {

    private OverOpsService overOpsService;

    public CreateReportStep() {
        this.overOpsService = DependencyInjector.getImplementation(OverOpsService.class);
    }

    @Override
    public QualityReport run(Config config) throws IOException, InterruptedException {
        println("OverOps [Step 2/3]: Analyzing data...");
        return overOpsService.produceReport(config);
    }
}
