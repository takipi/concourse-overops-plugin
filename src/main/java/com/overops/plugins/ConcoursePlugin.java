package com.overops.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overops.plugins.core.Step;
import com.overops.plugins.model.QueryOverOps;
import com.overops.plugins.model.Versions;
import com.overops.plugins.service.OverOpsService;
import com.overops.plugins.service.impl.AnsiWriter;
import com.overops.plugins.service.impl.OverOpsServiceImpl;
import com.overops.plugins.service.impl.ReportBuilder;
import com.overops.plugins.step.AnalyzingStep;
import com.overops.plugins.step.CheckStep;
import com.overops.plugins.step.GenerateReportStep;
import com.overops.plugins.step.PreparationStep;

import java.util.Arrays;

public class ConcoursePlugin {
    private Context context;

    private ConcoursePlugin(String[] args) {
        boolean status = true;
        context = Context.getBuilder().setOutputStream(new AnsiWriter(System.err)).setObjectMapper(new ObjectMapper()).build();
        OverOpsService overOpsService = new OverOpsServiceImpl(context);
        try {
            Step<String[], QueryOverOps> prepStep = new PreparationStep(context);
            QueryOverOps query = prepStep.run(args);
            context.getOutputStream().debugMode(query.isDebug());
            if (query.isCheckVersion()) {
                Step<QueryOverOps, Versions> checkVersionStep = new CheckStep(context, overOpsService);
                Versions versions = checkVersionStep.run(query);
                System.out.println(context.getObjectMapper().writeValueAsString(versions.getVersion()));
            } else {
                Step<QueryOverOps, ReportBuilder.QualityReport> analyzingStep = new AnalyzingStep(context, overOpsService);
                Step<ReportBuilder.QualityReport, Boolean> generateReportStep = new GenerateReportStep(context);
                ReportBuilder.QualityReport report = analyzingStep.run(query);
                status = generateReportStep.run(report);
                System.out.println(context.getObjectMapper().writeValueAsString(report.getMaxVersion()));
            }
        } catch (Exception e) {
            status = false;
            context.getOutputStream().error("Exceptions: " + e.toString());
            context.getOutputStream().error("Trace: " + Arrays.toString(e.getStackTrace()));
        }
        System.exit(status ? 0 : 1);
    }

    public static void run(String[] args) {
        new ConcoursePlugin(args);
    }

}
