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

package io.acuz.gruntr.vault;

import io.acuz.gruntr.vault.exception.VaultException;
import io.acuz.gruntr.vault.model.VaultToken;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
class VaultRestClientTest {
    final static MockWebServer MOCK_SERVER = new MockWebServer();

    @BeforeAll
    static void startMockServer() throws IOException {
        MOCK_SERVER.start(8200);
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        MOCK_SERVER.shutdown();
    }

    @Test
    void testVaultClientDecryption() throws MalformedURLException, InterruptedException, VaultException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var encryptedValue = "vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==".toCharArray();
        var mockResponseBody = "{ \"data\": {\"plaintext\": \"c29tZXRoaW5nIHZlcnkgc2VjcmV0\" } }";


        MockResponse mockResponse = new MockResponse().setBody(mockResponseBody);
        MOCK_SERVER.enqueue(mockResponse);

        var client = createClient(url);
        var result = client.decrypt(encryptedValue);

        var mockrequest = MOCK_SERVER.takeRequest();

        assertEquals("/v1/transit/project_name/decrypt/appkey", mockrequest.getPath());
        assertEquals("root", mockrequest.getHeaders().get("X-Vault-Token"));
        assertEquals("POST", mockrequest.getMethod());
        assertEquals(
                new String(Base64.getDecoder().decode("c29tZXRoaW5nIHZlcnkgc2VjcmV0")),
                String.copyValueOf(result));
    }

    @Test
    void testVaultClientEncryption() throws MalformedURLException, InterruptedException, VaultException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var mockBody = "{ \"data\": {\"ciphertext\": \"vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==\" } }";

        MockResponse mockResponse = new MockResponse().setBody(mockBody);
        MOCK_SERVER.enqueue(mockResponse);

        var client = createClient(url);
        var result = client.encrypt("some test".getBytes());
        var mockrequest = MOCK_SERVER.takeRequest();

        assertEquals("/v1/transit/project_name/encrypt/appkey", mockrequest.getPath());
        assertEquals("root", mockrequest.getHeaders().get("X-Vault-Token"));
        assertEquals("POST", mockrequest.getMethod());
        assertEquals("vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==",
                String.copyValueOf(result));
    }

    @Test
    void testVaultClientRewrap() throws MalformedURLException, InterruptedException, VaultException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var encryptedValue = "vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==".toCharArray();
        var mockBody = "{ \"data\": {\"ciphertext\": \"vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==\" } }";

        MockResponse mockResponse = new MockResponse().setBody(mockBody);
        MOCK_SERVER.enqueue(mockResponse);

        var client = createClient(url);
        var result = client.rewrap(encryptedValue);
        var mockrequest = MOCK_SERVER.takeRequest();

        assertEquals("/v1/transit/project_name/rewrap/appkey", mockrequest.getPath());
        assertEquals("root", mockrequest.getHeaders().get("X-Vault-Token"));
        assertEquals("POST", mockrequest.getMethod());
        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==",
                String.copyValueOf(result));
    }

    private VaultTransitRestClient createClient(URL url) {
        return VaultTransitRestClient
                .builder()
                .host(url)
                .transitPath("transit/project_name")
                .transitKeyName("appkey")
                .token(VaultToken.of("root"))
                .build();
    }
}