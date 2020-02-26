package com.overops.plugins;

import com.overops.plugins.model.Context;
import com.overops.plugins.model.QualityReport;
import com.overops.plugins.model.Config;
import com.overops.plugins.step.CreateReportStep;
import com.overops.plugins.step.RenderReportStep;
import com.overops.plugins.step.CreateConfigStep;

import java.util.Arrays;

public class Plugin {

    private Context context;

    public Plugin() {
        context = DependencyInjector.getImplementation(Context.class);
    }

    public void run(String[] args) {
        boolean status = true;
        try {
            Config config = new CreateConfigStep().run(args);
            QualityReport report = new CreateReportStep().run(config);
            new RenderReportStep().run(report);
            status = report.isStable();
        } catch (Exception e) {
            status = false;
            printError(e);
        }
        System.exit(status ? 0 : 1);
    }

    private void printError(Exception e) {
        context.getOutputStream().printlnError("Exceptions: " + e.toString());
        context.getOutputStream().printlnError("Trace: " + Arrays.toString(e.getStackTrace()));
    }
}
