package com.overops.plugins.step;

import com.overops.plugins.core.Step;
import com.overops.plugins.model.QualityReport;
import com.overops.plugins.service.impl.RenderService;

public class RenderReportStep extends Step<QualityReport, Void> {
    @Override
    public Void run(QualityReport report) {
        println("OverOps [Step 3/3]: Generating report...");
        new RenderService(report).render();
        return null;
    }

}
