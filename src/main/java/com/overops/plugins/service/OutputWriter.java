package com.overops.plugins.service;

import com.takipi.api.client.util.cicd.OOReportEvent;
import org.fusesource.jansi.Ansi;

import java.util.List;

public interface OutputWriter {

    void error(String message);

    void success(String message);

    void debug(String message);

    void yellow(String message);

    void print(String s, Ansi.Color color);

    void println(String s, Ansi.Color color);

    void block(Ansi.Color color, String... s);

    void block(String s, Ansi.Color color, boolean closeLine);

    void table(List<String> headers, List<OOReportEvent> body);

    void debugMode(boolean debug);
}
