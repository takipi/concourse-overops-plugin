package com.overops.plugins;

import com.overops.plugins.core.BasicStep;
import com.overops.plugins.core.Step;
import com.overops.plugins.model.QueryOverOps;
import com.overops.plugins.service.impl.AnsiWriter;
import com.overops.plugins.service.impl.OverOpsServiceImpl;
import com.overops.plugins.service.impl.ReportBuilder;
import com.overops.plugins.step.AnalyzingStep;
import com.overops.plugins.step.GenerateReportStep;
import com.overops.plugins.step.PreparationStep;
import org.fusesource.jansi.AnsiConsole;


public class ConcoursePlugin {
    private Context context;

    public static void run(String[] args) {
        new ConcoursePlugin(args);
    }

    private ConcoursePlugin(String[] args) {
        AnsiConsole.systemInstall();
        context = Context.getBuilder().setOutpitStream(new AnsiWriter(System.out)).build();
        try {
            Step<String[], QueryOverOps> prepStep = new PreparationStep(context);
            Step<QueryOverOps, ReportBuilder.QualityReport> analyzingStep = new AnalyzingStep(context, new OverOpsServiceImpl());
            BasicStep<ReportBuilder.QualityReport> generateReportStep = new GenerateReportStep(context);
            QueryOverOps query = prepStep.run(args);
            ReportBuilder.QualityReport report = analyzingStep.run(query);
            generateReportStep.run(report);
        } catch (Exception e) {
            context.getOutputStream().error("Exception: " + e.getMessage());
        }

    }

}
