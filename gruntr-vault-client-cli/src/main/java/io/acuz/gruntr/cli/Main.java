package io.acuz.gruntr.cli;

public class Main {
    public static void main(String[] args) {
        CommandFactory.build(args).run();
    }
}
