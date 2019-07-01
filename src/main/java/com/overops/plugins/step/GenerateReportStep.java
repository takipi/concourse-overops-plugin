package com.overops.plugins.step;

import com.overops.plugins.Context;
import com.overops.plugins.core.BasicStep;
import com.overops.plugins.service.impl.ReportBuilder;
import org.fusesource.jansi.Ansi;

public class GenerateReportStep extends BasicStep<ReportBuilder.QualityReport> {
    public GenerateReportStep(Context context) {
        super(context);
    }

    @Override
    public void run(ReportBuilder.QualityReport inputParams) {
        context.getOutputStream().println("OverOps [Step 3/3]: Generating report...", Ansi.Color.YELLOW);
        context.getOutputStream().print("Draw result", Ansi.Color.BLUE);
    }
}
