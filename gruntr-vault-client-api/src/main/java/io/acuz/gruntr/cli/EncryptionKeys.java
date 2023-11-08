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
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class EncryptionKeys {
    public static final String ALL = ".*";
    public static final String SECRETS = "(secret|token|password)";
    private static final List<String> PATTERNS = Collections.synchronizedList(new ArrayList<>());

    private EncryptionKeys() {
        //no-op
    }

    /**
     * Register one or more new patterns. These will be used to evaluate if a key in the properties file requires
     * encryption or not.
     *
     * @param pattern  the string representation of the RegExp to which the key must adhere
     * @param patterns a vararg of string representing the RegExp to register
     * @throws PatternSyntaxException – If the expression's syntax is invalid
     */
    public static void register(String pattern, String... patterns) {
        PATTERNS.add(Pattern.compile(pattern).pattern());

        if (null != patterns) {
            for (String p : patterns) {
                PATTERNS.add(Pattern.compile(p).pattern());
            }
        }
    }

    /**
     * Clears the list of registered patterns
     */
    static void clear() {
        synchronized (PATTERNS) {
            PATTERNS.clear();
        }
    }

    /**
     * Compiles the registered regexp into a single RegExp instance.
     * For example, registering 'secret' and 'password' would generate a regexp '.*(secret|password).*' which means that
     * the key must either contain secret or password to match. The matching is performed case-insensitive.
     *
     * @return the compiled regexp.
     */
    public static Pattern compile() {
        var buffer = new StringBuilder().append(".*(");

        for (int i = 0; i < PATTERNS.size(); i++) {
            if (i > 0) {
                buffer.append('|');
            }

            buffer.append(PATTERNS.get(i));
        }

        buffer.append(").*");

        return Pattern.compile(buffer.toString(), Pattern.CASE_INSENSITIVE);
    }
}
