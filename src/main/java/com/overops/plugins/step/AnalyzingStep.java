package com.overops.plugins.step;

import com.overops.plugins.Context;
import com.overops.plugins.core.Step;
import com.overops.plugins.model.QueryOverOps;
import com.overops.plugins.service.OverOpsService;
import com.overops.plugins.service.impl.ReportBuilder;
import org.fusesource.jansi.Ansi;

import java.io.IOException;

public class AnalyzingStep extends Step<QueryOverOps, ReportBuilder.QualityReport> {

    private OverOpsService overOpsService;

    public AnalyzingStep(Context context, OverOpsService overOpsService) {
        super(context);
        this.overOpsService = overOpsService;
    }

    @Override
    public ReportBuilder.QualityReport run(QueryOverOps inputParams) throws IOException, InterruptedException {
        context.getOutputStream().println("OverOps [Step 2/3]: Analyzing data...", Ansi.Color.MAGENTA);
        return overOpsService.perform(inputParams);
    }
}
