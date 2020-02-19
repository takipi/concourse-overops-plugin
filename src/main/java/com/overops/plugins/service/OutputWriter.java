package com.overops.plugins.service;

import com.overops.plugins.model.yaml.YamlObject;
import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.function.BiFunction;

public interface OutputWriter {

    PrintStream getPrintStream();

    void printlnError(String message);

    void printlnSuccess(String message);

    void printlnDebug(String message);

    void yellowFgPrintln(String message);

    void print(String s, Ansi.Color color);

    void println(String s, Ansi.Color color);

    void printStatementHeaders(String... s);

    void printYamlObject(YamlObject yamlObject);

    void printYamlObject(YamlObject yamlObject, BiFunction<String, String, Ansi.Color> getPropertyValueFgColor);
}
