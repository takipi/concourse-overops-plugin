package com.overops.plugins.service;

import com.overops.plugins.model.Context;
import com.overops.plugins.DependencyInjector;

public abstract class Render {
    protected Context context;

    public Render() {
        this.context = DependencyInjector.getImplementation(Context.class);
    }

    public abstract Render render();
}
