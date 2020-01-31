package com.overops.plugins.service.impl;

import com.overops.plugins.model.SummaryRow;
import com.overops.plugins.service.OutputWriter;
import com.takipi.api.client.util.cicd.OOReportEvent;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_FixedWidth;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class AnsiWriter implements OutputWriter {
    private PrintStream printStream;

    public AnsiWriter(PrintStream printStream) {
        AnsiConsole.systemInstall();
        this.printStream = printStream;
    }

    @Override
    public void error(String message) {
        printStream.println(ansi().fgBrightRed().a(message).reset().toString());
    }

    @Override
    public void success(String message) {
        printStream.println(ansi().fgBrightGreen().a(message).reset().toString());
    }

    @Override
    public void yellow(String message) {
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
    public void block(Ansi.Color color, String... s) {
        AsciiTable at = new AsciiTable();
        Arrays.stream(s).forEach(item -> {
            at.addRule();
            at.addRow(item);
        });
        at.addRule();
        printStream.println(at.render(130));
        printStream.println("\n");
    }

    @Override
    public void block(String s, Ansi.Color color, boolean closeLine) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow(s);
        if (closeLine)  {
            at.addRule();
        }
        printStream.println(at.render(130));
    }

    @Override
    public void table(List<String> headers, List<OOReportEvent> body) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow(headers);
        at.addRule();
        at.getRenderer().setCWC(new CWC_FixedWidth().add(85).add(15).add(15).add(10));
        printStream.println(at.render(125));
        body.forEach(item -> {
            AsciiTable tbody = new AsciiTable();
            tbody.addRow(item.getEventSummary(), item.getApplications(), item.getIntroducedBy(), item.getHits());
            tbody.addRule();
            tbody.getRenderer().setCWC(new CWC_FixedWidth().add(85).add(15).add(15).add(10));
            printStream.println(tbody.render(125));
            AsciiTable tbodyLink = new AsciiTable();
            tbodyLink.addRow(item.getARCLink());
            tbodyLink.addRule();
            printStream.println(tbodyLink.render(130));
        });
        printStream.println("\n");

    }

    @Override
    public void tableSummary(List<String> headers, List<SummaryRow> body) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow(headers);
        at.addRule();
        body.forEach(item -> {
            at.addRow(item.getGateName(), item.getGateStatus(), item.getTotal());
            at.addRule();
        });
        at.setTextAlignment(TextAlignment.CENTER);
        at.getRenderer().setCWC(new CWC_FixedWidth().add(20).add(20).add(20));
        printStream.println(at.render(60));
        printStream.println("\n");
    }
}
