package com.overops.plugins.model;

import com.overops.plugins.DependencyInjector;
import com.overops.plugins.service.OutputWriter;

public class Context {
    private OutputWriter outputStream;

    public Context() {
        this.outputStream = DependencyInjector.getImplementation(OutputWriter.class);
    }

    public OutputWriter getOutputStream() {
        return outputStream;
    }
}
