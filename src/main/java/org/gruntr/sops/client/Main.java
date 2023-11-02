package org.gruntr.sops.client;

import org.gruntr.sops.client.cli.CommandFactory;

public class Main {
    public static void main(String[] args) {
        CommandFactory.build(args).run();
    }
}
