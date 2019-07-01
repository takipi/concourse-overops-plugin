package com.overops.plugins.service;

import org.fusesource.jansi.Ansi;

public interface OutputWriter {

    void error(String message);

    void success(String message);

    void print(String s, Ansi.Color color);

    void println(String s, Ansi.Color color);
}
