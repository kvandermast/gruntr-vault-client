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

package io.acuz.gruntr.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultValidationUtilTest {
    @Test
    void whenVaultHostContainsGarbage() {
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("null"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("x//fake"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("http//localhost:8200"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("http:/localhost:8200"));
    }

    @Test
    void whenVaultHostIsNullOrEmpty() {
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultHost(null));
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultHost(""));
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultHost("    "));
    }

    @Test
    void whenVaultHostIsNotHttpCompliant() {
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("ftp://server"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("ssl://something"));
    }

    @Test
    void whenVaultHostHasQueryParameters() {
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("http://server?server=x"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("https://server/subcontext?redirectTo=http"));
    }

    @Test
    void whenVaultHostIsValid() {
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultHost("http://localhost"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultHost("http://localhost:8200"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultHost("https://localhost:8200"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultHost("HTTps://vault:8200/v1"));
    }

    @Test
    void whenVaultPathIsNotValid() {
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultPathComponent(null));
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultPathComponent(""));
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultPathComponent("   "));

        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultPathComponent("/"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultPathComponent("/ccc"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultPathComponent("ccc?redirect"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultPathComponent("ccc?redirect=true"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultPathComponent("garbage\\"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultPathComponent("gar^\\"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultPathComponent("gar "));

        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultPathComponent("transit"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultPathComponent("transit/project"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultPathComponent("transit/project_name"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultPathComponent("transit/project/name_"));

        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultPathComponent("_key"));
    }
}