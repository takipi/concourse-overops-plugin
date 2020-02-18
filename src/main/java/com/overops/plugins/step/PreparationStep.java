package com.overops.plugins.step;

import com.overops.plugins.core.Step;
import com.overops.plugins.model.Config;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;


public class PreparationStep extends Step<String[], Config> {
    @Override
    public Config run(String[] args) {
        context.getOutputStream().println("OverOps [Step 1/3]: Preparation data...", Ansi.Color.MAGENTA);
        Config config = new Config(args);
        validation(config);
        return config;
    }

    private void validation(Config config) {
        if (StringUtils.isEmpty(config.getOverOpsURL())) {
            throw new IllegalArgumentException("OverOps url can not be empty.");
        }

        if (StringUtils.isEmpty(config.getOverOpsSID())) {
            throw new IllegalArgumentException("Environment id can not be empty.");
        }

        if (StringUtils.isEmpty(config.getOverOpsAPIKey())) {
            throw new IllegalArgumentException("Token can not be empty.");
        }

        if (StringUtils.isEmpty(config.getOverOpsSID())) {
            throw new IllegalArgumentException("Missing environment Id");
        }

        if (StringUtils.isEmpty(config.getOverOpsURL())) {
            throw new IllegalArgumentException("Missing host name");
        }

        if (StringUtils.isEmpty(config.getOverOpsAPIKey())) {
            throw new IllegalArgumentException("Missing api key");
        }

        if (!"0".equalsIgnoreCase(config.getActiveTimespan())) {
            if (config.getActiveTimespanMinutes() == 0) {
                String errorMessage = "For Increasing Error Gate, the active time window currently set to: " + config.getActiveTimespan() + " is not properly formatted. See help for format instructions.";
                context.getOutputStream().printlnError(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }

        if (!"0".equalsIgnoreCase(config.getBaselineTimespan())) {
            if (config.getBaselineTimespanMinutes() == 0) {
                String errorMessage = "For Increasing Error Gate, the baseline time window currently set to: " + config.getBaselineTimespan() + " cannot be zero or is improperly formatted. See help for format instructions.";
                context.getOutputStream().printlnError(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

}
