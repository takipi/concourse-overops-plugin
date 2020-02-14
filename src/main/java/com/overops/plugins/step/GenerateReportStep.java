package com.overops.plugins.step;

import com.overops.plugins.Context;
import com.overops.plugins.core.Step;
import com.overops.plugins.model.QualityReport;
import com.overops.plugins.service.Render;
import com.overops.plugins.service.impl.RenderService;
import org.fusesource.jansi.Ansi;

public class GenerateReportStep extends Step<QualityReport, Boolean> {
    public GenerateReportStep(Context context) {
        super(context);
    }

    @Override
    public Boolean run(QualityReport inputParams) {
        context.getOutputStream().println("OverOps [Step 3/3]: Generating report...", Ansi.Color.MAGENTA);
        Render render = new RenderService(context, inputParams);
        render.render();
        return render.isStable();
    }
}
