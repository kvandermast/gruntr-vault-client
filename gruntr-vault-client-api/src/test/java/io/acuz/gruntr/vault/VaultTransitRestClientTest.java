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
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("SpellCheckingInspection")
class VaultTransitRestClientTest {
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

    @Test
    void testVaultClientExceptionForInvalidStatusCode() throws MalformedURLException, InterruptedException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var encryptedValue = "vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==".toCharArray();

        MockResponse mockResponse = new MockResponse().setResponseCode(403).setBody("");
        MOCK_SERVER.enqueue(mockResponse);

        var client = createClient(url);
        assertThrows(VaultException.class, () -> client.rewrap(encryptedValue));
        var mockRequest = MOCK_SERVER.takeRequest();

        assertEquals("/v1/transit/project_name/rewrap/appkey", mockRequest.getPath());
        assertEquals("root", mockRequest.getHeaders().get("X-Vault-Token"));
        assertEquals("POST", mockRequest.getMethod());
    }

    @Test
    void testVaultClientExceptionForInvalidDataStructure() throws MalformedURLException, InterruptedException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var encryptedValue = "vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==".toCharArray();
        var mockBody = "{ \"atad\": {\"txetrepihc\": \"vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==\" } }";
        var mockBody2 = "{ \"data\": {\"txetrepihc\": \"vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==\" } }";

        MOCK_SERVER.enqueue(new MockResponse().setBody(mockBody));
        MOCK_SERVER.enqueue(new MockResponse().setBody(mockBody2));

        var client = createClient(url);

        // no "data" element found
        assertThrows(VaultException.class, () -> client.rewrap(encryptedValue));
        MOCK_SERVER.takeRequest(); // remove from the queue

        // no "ciphertext" element found
        assertThrows(VaultException.class, () -> client.rewrap(encryptedValue));
        MOCK_SERVER.takeRequest(); // remove from the queue
    }

    @Test
    void testVaultClientDecryptPropertiesFailsOnShaVerification() throws MalformedURLException, InterruptedException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var mockBody = "{ \"data\": {\"plaintext\": \"NDEyZTU1NWZjYmZjNDNmMmUxYWIyZWQzZDNiM2JmNWNjMDIwZGM0YWQwYmVkYjRlYzg4OTIzYzcxNGUwODg4Yg==\" } }";

        var client = createClient(url);
        var properties = new Properties();

        var throwable = assertThrows(NullPointerException.class, () -> client.decrypt(properties));
        assertEquals("Cannot validate hash, missing Vault Transit Key", throwable.getMessage());

        properties.put("gruntr__vault_transit_key", "keyname");
        throwable = assertThrows(NullPointerException.class, () -> client.decrypt(properties));
        assertEquals("Cannot validate hash, missing Vault host", throwable.getMessage());

        properties.put("gruntr__vault_host", "http://localhost");
        throwable = assertThrows(NullPointerException.class, () -> client.decrypt(properties));
        assertEquals("Cannot validate hash, missing Vault Transit Path", throwable.getMessage());

        properties.put("gruntr__vault_transit_path", "transit");
        throwable = assertThrows(NullPointerException.class, () -> client.decrypt(properties));
        assertEquals("Cannot validate hash, missing Vault SHA3 value", throwable.getMessage());

        properties.put("gruntr__sha3", "fake value");
        MOCK_SERVER.enqueue(new MockResponse().setBody(mockBody));
        var illegalStateException = assertThrows(IllegalStateException.class, () -> client.decrypt(properties));
        assertEquals("Hash validation failed, gruntr__ values were tampered with?", illegalStateException.getMessage());

        MOCK_SERVER.takeRequest(); // dequeue request
    }

    @Test
    void testVaultClientDecryptProperties() throws MalformedURLException, InterruptedException, VaultException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var sha3ResultCheck = "{ \"data\": {\"plaintext\": \"OWVlYWUwMjU5NWQzMWRmNmFmYjhkZDlhMDI4NzllNzU0YzA4NTA3MTYzYzFhNDg0Y2IzY2FkMjUwYWE2MjhhZg==\" } }";
        var mockBody = "{ \"data\": {\"plaintext\": \"bXkgdmVyeSBzZWN1cmUgdmFsdWU=\" } }";

        var client = createClient(url);
        var properties = createGruntrProperties();

        properties.put("my.plaintext", "this is plain text");
        properties.put("my.encryption", "vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==");

        MOCK_SERVER.enqueue(new MockResponse().setBody(sha3ResultCheck));
        MOCK_SERVER.enqueue(new MockResponse().setBody(mockBody));

        var decryptedProperties = client.decrypt(properties);

        assertEquals(decryptedProperties.get("my.plaintext"), properties.get("my.plaintext"));
        assertEquals("my very secure value", decryptedProperties.get("my.encryption"));

        assertEquals("/v1/transit/project_name/decrypt/appkey", MOCK_SERVER.takeRequest().getPath());
        assertEquals("/v1/transit/project_name/decrypt/appkey", MOCK_SERVER.takeRequest().getPath());
    }

    @Test
    void testVaultClientEncryptProperties() throws MalformedURLException, InterruptedException, VaultException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var mockBody = "{ \"data\": {\"ciphertext\": \"vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==\" } }";

        var client = createClient(url);
        var properties = new Properties();

        properties.put("my.plaintext", "this is plain text");
        properties.put("my.secret", "my secret");
        properties.put("my.password", "my password");
        properties.put("my.token", "my token");

        // it should encode 4 properties + the hash
        final var numberOfRequests = 5;

        for (int i = 0; i < numberOfRequests; i++)
            MOCK_SERVER.enqueue(new MockResponse().setBody(mockBody));

        var encryptedProperties = client.encrypt(properties);

        assertEquals(8, encryptedProperties.size());

        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get("my.plaintext"));
        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get("my.secret"));
        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get("my.password"));
        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get("my.token"));

        assertEquals("appkey", encryptedProperties.get(VaultTransitRestClient.GRUNTR__VAULT_TRANSIT_KEY));
        assertEquals("transit/project_name", encryptedProperties.get(VaultTransitRestClient.GRUNTR__VAULT_TRANSIT_PATH));
        assertEquals("http://localhost:" + MOCK_SERVER.getPort(), encryptedProperties.get(VaultTransitRestClient.GRUNTR__VAULT_HOST));
        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get(VaultTransitRestClient.GRUNTR__SHA_3));

        for (int i = 0; i < numberOfRequests; i++) //dequeue requests
            assertEquals("/v1/transit/project_name/encrypt/appkey", MOCK_SERVER.takeRequest().getPath());
    }

    @Test
    void testVaultClientRewrapProperties() throws MalformedURLException, InterruptedException, VaultException {
        var url = URI.create("http://localhost:" + MOCK_SERVER.getPort()).toURL();
        var sha3ResultCheck = "{ \"data\": {\"plaintext\": \"OWVlYWUwMjU5NWQzMWRmNmFmYjhkZDlhMDI4NzllNzU0YzA4NTA3MTYzYzFhNDg0Y2IzY2FkMjUwYWE2MjhhZg==\" } }";
        var mockBody = "{ \"data\": {\"ciphertext\": \"vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==\" } }";

        var client = createClient(url);
        var properties = createGruntrProperties();

        properties.put("my.plaintext", "vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==");
        properties.put("my.secret", "vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==");
        properties.put("my.password", "vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==");
        properties.put("my.token", "vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==");

        // it should encode 4 properties + the hash
        final var numberOfRequests = 5;

        MOCK_SERVER.enqueue(new MockResponse().setBody(sha3ResultCheck));
        for (int i = 0; i < numberOfRequests - 1; i++)
            MOCK_SERVER.enqueue(new MockResponse().setBody(mockBody));

        var encryptedProperties = client.rewrap(properties);

        assertEquals(8, encryptedProperties.size());

        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get("my.plaintext"));
        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get("my.secret"));
        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get("my.password"));
        assertEquals("vault:v2:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==", encryptedProperties.get("my.token"));

        assertEquals("keyname", encryptedProperties.get(VaultTransitRestClient.GRUNTR__VAULT_TRANSIT_KEY));
        assertEquals("transit", encryptedProperties.get(VaultTransitRestClient.GRUNTR__VAULT_TRANSIT_PATH));
        assertEquals("http://localhost:" + MOCK_SERVER.getPort(), encryptedProperties.get(VaultTransitRestClient.GRUNTR__VAULT_HOST));
        assertEquals("vault:v1:9eeae02595d31df6afb8dd9a02879e754c08507163c1a484cb3cad250aa628af", encryptedProperties.get(VaultTransitRestClient.GRUNTR__SHA_3));

        assertEquals("/v1/transit/project_name/decrypt/appkey", MOCK_SERVER.takeRequest().getPath());
        for (int i = 0; i < numberOfRequests-1; i++) //dequeue requests
            assertEquals("/v1/transit/project_name/rewrap/appkey", MOCK_SERVER.takeRequest().getPath());
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

    private Properties createGruntrProperties() {
        var properties = new Properties();

        properties.put("gruntr__vault_transit_key", "keyname");
        properties.put("gruntr__vault_host", "http://localhost:" + MOCK_SERVER.getPort());
        properties.put("gruntr__vault_transit_path", "transit");
        properties.put("gruntr__sha3", "vault:v1:9eeae02595d31df6afb8dd9a02879e754c08507163c1a484cb3cad250aa628af");

        //if you want to render a "correct" Digest, look at the DigestUtilsTest to render a new one.

        return properties;
    }
}