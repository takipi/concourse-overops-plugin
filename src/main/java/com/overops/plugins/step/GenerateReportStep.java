package com.overops.plugins.step;

import com.overops.plugins.Context;
import com.overops.plugins.core.BasicStep;
import com.overops.plugins.service.Render;
import com.overops.plugins.service.impl.RenderService;
import com.overops.plugins.service.impl.ReportBuilder;
import org.fusesource.jansi.Ansi;

public class GenerateReportStep extends BasicStep<ReportBuilder.QualityReport> {
    public GenerateReportStep(Context context) {
        super(context);
    }

    @Override
    public void run(ReportBuilder.QualityReport inputParams) {
        context.getOutputStream().println("OverOps [Step 3/3]: Generating report...", Ansi.Color.YELLOW);
        Render render = new RenderService(context, inputParams);
        render.render();
        context.getOutputStream().print("Draw result", Ansi.Color.BLUE);
    }
}
