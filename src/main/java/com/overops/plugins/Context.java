package com.overops.plugins;

import com.overops.plugins.service.OutputWriter;

public class Context {
    private OutputWriter outputStream;

    public Context() {
        this.outputStream = DependencyInjector.getImplementation();
    }

    public OutputWriter getOutputStream() {
        return outputStream;
    }
}
