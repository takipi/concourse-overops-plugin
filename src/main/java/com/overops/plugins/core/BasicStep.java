package com.overops.plugins.core;

import com.overops.plugins.Context;

public abstract class BasicStep<I> {
    protected Context context;

    public BasicStep(Context context) {
        this.context = context;
    }

    public abstract void run(I inputParams);
}
