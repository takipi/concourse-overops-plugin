package com.overops.plugins;

import com.overops.plugins.service.OutputWriter;
import com.overops.plugins.service.OverOpsService;
import com.overops.plugins.service.impl.AnsiWriter;
import com.overops.plugins.service.impl.OverOpsServiceImpl;

public class DependencyInjector {
    public static OutputWriter getImplementation() {
        return new AnsiWriter(System.err);
    }

    public static OverOpsService getImplementation(Context context) {
        return new OverOpsServiceImpl(context);
    }
}
