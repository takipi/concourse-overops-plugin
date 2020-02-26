package com.overops.plugins.core;

import com.overops.plugins.Context;
import com.overops.plugins.DependencyInjector;
import org.fusesource.jansi.Ansi;

import java.io.IOException;

public abstract class Step<I, O> {
    protected Context context;

    public Step() {
        this.context = DependencyInjector.getImplementation(Context.class);
    }

    public abstract O run(I inputParams) throws IOException, InterruptedException;

    public void println(String message) {
        context.getOutputStream().println(message, Ansi.Color.MAGENTA);
    }
}
