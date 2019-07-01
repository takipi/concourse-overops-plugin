package com.overops.plugins.core;

import com.overops.plugins.Context;

import java.io.IOException;

public abstract class Step<I, O> {
    protected Context context;

    public Step(Context context) {
        this.context = context;
    }

    public abstract O run(I inputParams) throws IOException, InterruptedException;
}
