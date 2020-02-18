package com.overops.plugins;

import com.overops.plugins.model.QualityReport;
import com.overops.plugins.model.Config;
import com.overops.plugins.step.AnalyzingStep;
import com.overops.plugins.step.GenerateReportStep;
import com.overops.plugins.step.PreparationStep;

import java.util.Arrays;

public class ConcoursePlugin {

    public static void run(String[] args) {
        boolean status = true;
        try {
            Config config = new PreparationStep().run(args);
            QualityReport report = new AnalyzingStep().run(config);
            status = new GenerateReportStep().run(report);
        } catch (Exception e) {
            status = false;
            printError(e);
        }
        System.exit(status ? 0 : 1);
    }

    private static void printError(Exception e) {
        Context context = DependencyInjector.getImplementation(Context.class);
        context.getOutputStream().printlnError("Exceptions: " + e.toString());
        context.getOutputStream().printlnError("Trace: " + Arrays.toString(e.getStackTrace()));
    }
}
