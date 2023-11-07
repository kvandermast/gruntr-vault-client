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

import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

enum CliParameterName {
    INPUT_FILE("--input", "-i"),
    OUTPUT_FILE("--output", "-o"),
    HC_VAULT_TOKEN("--hc-token", "-t", "--token"),
    HC_VAULT_HOST("--hc-vault-server", "-h"),
    HC_VAULT_TRANSIT_PATH("--hc-transit-path"),
    HC_VAULT_TRANSIT_KEY("--hc-transit-key"),
    ;

    private static final Set<CliParameterName> ALL = Set.of(CliParameterName.values());
    private final Pattern regexp;

    CliParameterName(String... patterns) {
        if (null == patterns || patterns.length == 0) {
            throw new IllegalArgumentException("CliParameter must container at least one parameter");
        }

        this.regexp = Pattern.compile("^(" + String.join("|", patterns) + ")$");
    }

    static CliParameterName get(String value) {
        return ALL.stream().filter(it -> it.matches(value)).findAny().orElse(null);
    }

    public boolean matches(String value) {
        requireNonNull(value);

        return this.regexp.matcher(value.toLowerCase()).matches();
    }
}
