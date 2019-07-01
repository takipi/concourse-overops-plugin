package com.overops.plugins.service.impl;

import com.overops.plugins.service.OutputWriter;
import org.fusesource.jansi.Ansi;

import java.io.PrintStream;

public class AnsiWriter implements OutputWriter {
    PrintStream printStream;

    public AnsiWriter(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void error(String message) {

    }

    @Override
    public void success(String message) {

    }

    @Override
    public void print(String s, Ansi.Color color) {

    }

    @Override
    public void println(String s, Ansi.Color color) {

    }

}