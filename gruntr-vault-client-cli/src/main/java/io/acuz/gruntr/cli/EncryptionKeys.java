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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class EncryptionKeys {
    public static final String ALL = ".*";
    public static final String SECRETS = "(secret|token|password)";
    private static final List<String> PATTERNS = new ArrayList<>();

    private EncryptionKeys() {
        //no-op
    }

    public static void register(String pattern, String... patterns) {
        PATTERNS.add(Pattern.compile(pattern).pattern());

        if(null != patterns) {
            for (String p : patterns) {
                PATTERNS.add(Pattern.compile(p).pattern());
            }
        }
    }

    static void clear() {
        PATTERNS.clear();
    }

    public static Pattern compile() {
        var buffer = new StringBuilder().append('(');

        for (int i = 0; i < PATTERNS.size(); i++) {
            if (i > 0) {
                buffer.append('|');
            }

            buffer.append(PATTERNS.get(i));
        }

        buffer.append(')');

        return Pattern.compile(buffer.toString(), Pattern.CASE_INSENSITIVE);
    }
}
