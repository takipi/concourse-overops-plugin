package com.overops.plugins.step;

import com.overops.plugins.core.Step;
import com.overops.plugins.model.QueryOverConfig;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

public class PreparationStep extends Step<String[], QueryOverConfig> {
    @Override
    public QueryOverConfig run(String[] args) {
        context.getOutputStream().println("OverOps [Step 1/3]: Preparation data...", Ansi.Color.MAGENTA);
        QueryOverConfig config = new QueryOverConfig(args);
        validation(config);
        return config;
    }

    private void validation(QueryOverConfig config) {
        if (StringUtils.isEmpty(config.getOverOpsURL())) {
            throw new IllegalArgumentException("OverOps url can not be empty.");
        }
        if (StringUtils.isEmpty(config.getOverOpsSID())) {
            throw new IllegalArgumentException("Environment id can not be empty.");
        }
        if (StringUtils.isEmpty(config.getOverOpsAPIKey())) {
            throw new IllegalArgumentException("Token can not be empty.");
        }
    }
}
