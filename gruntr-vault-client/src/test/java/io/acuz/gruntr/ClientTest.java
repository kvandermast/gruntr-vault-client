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

package io.acuz.gruntr;

import io.acuz.gruntr.vault.model.VaultToken;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class ClientTest {
    @Test
    void test_createANewClientWithoutParameters() {
        assertThrows(NullPointerException.class, () -> Client.builder().build());
    }

    @Test
    void test_createANewClientWithParameters() {
        var client = Client.builder()
                .setToken(VaultToken.of("root"))
                .setPath(Path.of("/Users/Kris/fod_workspace/sops_playground", "application-encrypted.properties"))
                .build();

        assertNotNull(client);
        assertNotNull(client.getPath());
        assertNotNull(client.getEncryptedProperties());

        var properties = client.getEncryptedProperties();
        assertEquals(5, properties.size());
    }

    @Test
    @Disabled("Disabled until Mockserver is implemented")
    void test_createANewClientAndDecrypt() {
        var client = Client.builder()
                .setToken(VaultToken.of("root"))
                .setPath(Path.of("/Users/Kris/fod_workspace/sops_playground", "application-encrypted.properties"))
                .build();

        assertNotNull(client);

        var properties = client.getDecryptedProperties();
        assertEquals(2, properties.size());
    }
}