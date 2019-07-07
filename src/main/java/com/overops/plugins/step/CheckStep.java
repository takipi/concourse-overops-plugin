package com.overops.plugins.step;

import com.overops.plugins.Context;
import com.overops.plugins.core.Step;
import com.overops.plugins.model.QueryOverOps;
import com.overops.plugins.model.Versions;
import com.overops.plugins.service.OverOpsService;

import java.io.IOException;

public class CheckStep extends Step<QueryOverOps, Versions> {

    private OverOpsService overOpsService;

    public CheckStep(Context context, OverOpsService overOpsService) {
        super(context);
        this.overOpsService = overOpsService;
    }

    @Override
    public Versions run(QueryOverOps inputParams) throws IOException, InterruptedException {

        return overOpsService.check(inputParams);
    }
}
