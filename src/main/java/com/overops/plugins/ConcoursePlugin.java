package com.overops.plugins;

import com.overops.plugins.core.Step;
import com.overops.plugins.model.QueryOverOps;
import com.overops.plugins.service.impl.AnsiWriter;
import com.overops.plugins.service.impl.OverOpsServiceImpl;
import com.overops.plugins.service.impl.ReportBuilder;
import com.overops.plugins.step.AnalyzingStep;
import com.overops.plugins.step.GenerateReportStep;
import com.overops.plugins.step.PreparationStep;

import java.util.Arrays;


public class ConcoursePlugin {
    private Context context;

    public static void run(String[] args) {
        new ConcoursePlugin(args);
    }

    private ConcoursePlugin(String[] args) {
        boolean status;
        context = Context.getBuilder().setOutpitStream(new AnsiWriter(System.err)).build();
        try {
            Step<String[], QueryOverOps> prepStep = new PreparationStep(context);
            Step<QueryOverOps, ReportBuilder.QualityReport> analyzingStep = new AnalyzingStep(context, new OverOpsServiceImpl(context));
            Step<ReportBuilder.QualityReport, Boolean> generateReportStep = new GenerateReportStep(context);
            QueryOverOps query = prepStep.run(args);
            ReportBuilder.QualityReport report = analyzingStep.run(query);
            status = generateReportStep.run(report);
        } catch (Exception e) {
            status = false;
            context.getOutputStream().error("Exceptions: " + e.toString());
            context.getOutputStream().error("Trace: " + Arrays.toString(e.getStackTrace()));
        }
        System.exit(status ? 0 : 1);
    }

}
