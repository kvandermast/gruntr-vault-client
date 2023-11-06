package io.acuz.gruntr.cli;

import java.util.ArrayDeque;
import java.util.Arrays;

final class CommandFactory {
    public static Command build(String[] args) {
        var q = new ArrayDeque<>(Arrays.asList(args));

        if (q.isEmpty() || q.size() < 2) {
            throw new IllegalArgumentException("Insufficient parameters provided");
        }

        var action = q.removeFirst();

        switch (action.trim().toLowerCase()) {
            case "encrypt":
                return EncryptPropertiesFileCommand.builder()
                        .parameters(q)
                        .build();

            case "decrypt":
                return DecryptPropertiesFileCommand.builder()
                        .parameters(q)
                        .build();
            case "rewrap":
                return RewrapPropertiesFileCommand.builder()
                        .parameters(q)
                        .build();
            default:
                throw new IllegalArgumentException("unknown action: " + action);
        }
    }


}
