package com.overops.plugins.step;

import com.overops.plugins.Context;
import com.overops.plugins.DependencyInjector;
import com.overops.plugins.core.Step;
import com.overops.plugins.model.QualityReport;
import com.overops.plugins.model.QueryOverConfig;
import com.overops.plugins.service.OverOpsService;
import org.fusesource.jansi.Ansi;

import java.io.IOException;

public class AnalyzingStep extends Step<QueryOverConfig, QualityReport> {

    private OverOpsService overOpsService;

    public AnalyzingStep(Context context) {
        super(context);
        this.overOpsService = DependencyInjector.getImplementation(context);

    }

    @Override
    public QualityReport run(QueryOverConfig inputParams) throws IOException, InterruptedException {
        context.getOutputStream().println("OverOps [Step 2/3]: Analyzing data...", Ansi.Color.MAGENTA);
        return overOpsService.perform(inputParams);
    }
}
