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

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;

import static io.acuz.gruntr.cli.CliParameterName.*;
import static org.junit.jupiter.api.Assertions.*;

class CliParameterNameTest {
    @Test
    void test_CliParameterInputValidity() {
        assertThrows(NullPointerException.class, () -> INPUT_FILE.matches(null));

        assertFalse(INPUT_FILE.matches("test"));
        assertTrue(INPUT_FILE.matches("-i"));
        assertTrue(INPUT_FILE.matches("-I"));
        assertTrue(INPUT_FILE.matches("--inPut"));
        assertTrue(INPUT_FILE.matches("--input"));
    }

    @Test
    void test_CliParameterValidAll() {
        var parameters = new EnumMap<CliParameterName, List<String>>(CliParameterName.class);

        parameters.put(INPUT_FILE, List.of("--input", "-i"));
        parameters.put(OUTPUT_FILE, List.of("--output", "-o"));
        parameters.put(HC_VAULT_TOKEN, List.of("--hc-token", "-t", "--token"));
        parameters.put(HC_VAULT_HOST, List.of("--hc-vault-server", "-h"));
        parameters.put(HC_VAULT_TRANSIT_PATH, List.of("--hc-transit-path"));
        parameters.put(HC_VAULT_TRANSIT_KEY, List.of("--hc-transit-key"));

        parameters.forEach((key, value) -> value.forEach(s -> assertTrue(key.matches(s), "Could not match '" + key.name() + "' for value '" + s + "'")));
    }

    @Test
    void test_CliParameterResolution() {
        assertEquals(INPUT_FILE, CliParameterName.get("-i"));
        assertEquals(INPUT_FILE, CliParameterName.get("--input"));
        assertEquals(OUTPUT_FILE, CliParameterName.get("--output"));
        assertEquals(OUTPUT_FILE, CliParameterName.get("-o"));
        assertEquals(HC_VAULT_TOKEN, CliParameterName.get("-t"));
        assertEquals(HC_VAULT_TOKEN, CliParameterName.get("--token"));
        assertEquals(HC_VAULT_TOKEN, CliParameterName.get("--hc-token"));
        assertEquals(HC_VAULT_HOST, CliParameterName.get("-h"));
        assertEquals(HC_VAULT_HOST, CliParameterName.get("--hc-vault-server"));
        assertEquals(HC_VAULT_TRANSIT_PATH, CliParameterName.get("--hc-transit-path"));
        assertEquals(HC_VAULT_TRANSIT_KEY, CliParameterName.get("--hc-transit-key"));

        assertNull(CliParameterName.get("test"));
    }
}