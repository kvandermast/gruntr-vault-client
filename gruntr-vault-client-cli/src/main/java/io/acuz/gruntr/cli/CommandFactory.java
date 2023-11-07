/*
 * Copyright (c) 2023. Gruntr/ACUZIO BV
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * https://www.gnu.org/licenses/gpl-3.0.html
 *
 */

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
