package com.overops.plugins;


import org.apache.log4j.BasicConfigurator;

public class Main {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        new Plugin().run(args);
    }
}
