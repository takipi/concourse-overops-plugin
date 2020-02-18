package com.overops.plugins.service.impl;

import com.overops.plugins.model.YamlObject;
import com.overops.plugins.service.OutputWriter;
import com.takipi.api.client.util.cicd.OOReportEvent;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_FixedWidth;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import static org.fusesource.jansi.Ansi.ansi;

public class AnsiWriter implements OutputWriter {
    private PrintStream printStream;
    private Ansi.Color propertyColor;
    private Ansi.Color propertyValueColor;

    public AnsiWriter(PrintStream printStream) {
        AnsiConsole.systemInstall();
        this.printStream = printStream;

        propertyColor = Ansi.Color.BLUE;
        propertyValueColor = Ansi.Color.CYAN;
    }

    @Override
    public void printlnError(String message) {
        printStream.println(ansi().fgBrightRed().a(message).reset().toString());
    }

    @Override
    public void printlnSuccess(String message) {
        printStream.println(ansi().fgBrightGreen().a(message).reset().toString());
    }

    @Override
    public void printlnDebug(String message) {
        printStream.println(ansi().fgBrightDefault().a(message).reset().toString());
    }

    @Override
    public void yellowFgPrintln(String message) {
        printStream.println(ansi().fgBrightYellow().a(message).reset().toString());
    }

    @Override
    public void print(String s, Ansi.Color color) {
        printStream.print(ansi().fg(color).a(s).reset().toString());
    }

    @Override
    public void println(String s, Ansi.Color color) {
        printStream.println(ansi().fg(color).a(s).reset().toString());
    }

    @Override
    public void printStatementHeaders(String... s) {
        Arrays.stream(s).forEach(item -> {
            yellowFgPrintln(item);
        });
    }

    @Override
    public void printYamlObject(YamlObject yamlObject) {
        printYamlObject(yamlObject, null);
    }
    @Override
    public void printYamlObject(YamlObject yamlObject, BiFunction<String, String, Ansi.Color> getPropertyValueFgColor) {
        String column = ":";
        println(yamlObject.getName() + column, propertyColor);
        yamlObject.getSimpleProperties().forEach(propertyValueMap -> {
            boolean isFirstPrinted = false;
            Set<String> propertyNames = propertyValueMap.keySet();
            Iterator<String> itr = propertyNames.iterator();
            while (itr.hasNext()){
                String property = itr.next();
                String propertyValue = propertyValueMap.get(property);
                if (isFirstPrinted == false) {
                    print("  - ", propertyColor);
                    isFirstPrinted = true;
                } else {
                    print("  | ", Ansi.Color.MAGENTA);
                }

                print(property + column, propertyColor);
                Ansi.Color finalPropertyValueColor = (getPropertyValueFgColor != null) &&
                        (getPropertyValueFgColor.apply(property, propertyValue) != null) ?
                        getPropertyValueFgColor.apply(property, propertyValue) : propertyValueColor;
                println(" " + propertyValue, finalPropertyValueColor);
            }
        });
    }
}
