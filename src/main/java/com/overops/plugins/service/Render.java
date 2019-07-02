package com.overops.plugins.service;

import com.overops.plugins.Context;
import org.fusesource.jansi.Ansi;

public abstract class Render {
    protected Context context;

    public Render(Context context) {
        context.getOutputStream().println(getDisplayName(), Ansi.Color.BLACK);
    }

    public abstract String getDisplayName();

    public abstract void render();
}
