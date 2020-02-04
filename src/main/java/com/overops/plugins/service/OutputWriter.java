package com.overops.plugins.service;

import com.takipi.api.client.util.cicd.OOReportEvent;
import org.fusesource.jansi.Ansi;

import java.util.List;

public interface OutputWriter {

    void error(String message);

    void success(String message);

    void yellowFgPrintln(String message);

    void print(String s, Ansi.Color color);

    void println(String s, Ansi.Color color);

    void printStatementHeaders(String... s);

    void table(List<String> headers, List<OOReportEvent> body);
}
