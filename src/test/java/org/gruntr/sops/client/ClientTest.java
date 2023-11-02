package org.gruntr.sops.client;

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
                .setPath(Path.of("/Users/Kris/Documents/fod_workspace/sops_playground", "application-encrypted.env"))
                .build();

        assertNotNull(client);
        assertNotNull(client.getPath());
        assertNotNull(client.getEncryptedProperties());

        var properties = client.getEncryptedProperties();
        assertEquals(2, properties.size());

        properties.keySet().forEach(it -> {
            assertFalse(((String) it).startsWith("sops_"), "Properties should not contains sops entries, found " + it);
        });
    }

    @Test
    void test_createANewClientAndDecrypt() {
        var client = Client.builder()
                .setPath(Path.of("/Users/Kris/Documents/fod_workspace/sops_playground", "application-encrypted.env"))
                .build();

        assertNotNull(client);

        var properties = client.getDecryptedProperties();
        assertEquals(2, properties.size());
    }
}