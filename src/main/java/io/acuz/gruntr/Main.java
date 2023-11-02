package io.acuz.gruntr;

import io.acuz.gruntr.cli.CommandFactory;

public class Main {
    public static void main(String[] args) {
        CommandFactory.build(args).run();
    }
}
