package com.overops.plugins;

import com.overops.plugins.model.Context;
import com.overops.plugins.service.OutputWriter;
import com.overops.plugins.service.impl.AnsiWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DependencyInjector {

    private static Map<Class, Supplier<?>> map = new HashMap<>();
    private static Map<Class, Object> beansMap = new HashMap<>();

    static {
        map.put(OutputWriter.class, () -> new AnsiWriter(System.err));
        map.put(Context.class, Context::new);
    }

    public static <T> T getImplementation(Class clazz) {
        if (beansMap.get(clazz) == null) {
            beansMap.put(clazz, map.get(clazz).get());
        }
        return (T)beansMap.get(clazz);
    }
}
