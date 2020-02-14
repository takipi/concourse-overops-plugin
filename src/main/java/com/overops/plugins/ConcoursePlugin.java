package com.overops.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overops.plugins.model.QualityReport;
import com.overops.plugins.model.QueryOverConfig;
import com.overops.plugins.step.AnalyzingStep;
import com.overops.plugins.step.GenerateReportStep;
import com.overops.plugins.step.PreparationStep;

import java.util.Arrays;

public class ConcoursePlugin {

    public static void run(String[] args) {
        boolean status = true;
        Context context = new Context();
        try {
            QueryOverConfig config = new PreparationStep(context).run(args);
            QualityReport report = new AnalyzingStep(context).run(config);
            status = new GenerateReportStep(context).run(report);

            System.out.println(new ObjectMapper().writeValueAsString(report.getMetadata()));
        } catch (Exception e) {
            status = false;
            context.getOutputStream().error("Exceptions: " + e.toString());
            context.getOutputStream().error("Trace: " + Arrays.toString(e.getStackTrace()));
        }
        System.exit(status ? 0 : 1);
    }
}
