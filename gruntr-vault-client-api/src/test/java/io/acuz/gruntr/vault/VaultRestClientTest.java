package io.acuz.gruntr.vault;

import io.acuz.gruntr.vault.model.VaultToken;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
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
    void test_ShouldNotFailOnCall() throws MalformedURLException, InterruptedException {
        var url = URI.create("http://localhost:8200").toURL();

        MockResponse mockResponse = new MockResponse().setBody("{ \"data\": {\"plaintext\": \"c29tZXRoaW5nIHZlcnkgc2VjcmV0\" } }");

        MOCK_SERVER.enqueue(mockResponse);


        var client = VaultTransitRestClient
                .builder()
                .host(url)
                .transitPath("transit/project_name")
                .transitKeyName("appkey")
                .token(VaultToken.of("root"))
                .build();

        var result = client.decrypt("vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==".toCharArray());

        assertEquals(1, MOCK_SERVER.getRequestCount());
        var mockrequest = MOCK_SERVER.takeRequest();

        assertEquals("/v1/transit/project_name/decrypt/appkey", mockrequest.getPath());
        assertEquals("root", mockrequest.getHeaders().get("X-Vault-Token"));
        assertEquals("POST", mockrequest.getMethod());

        assertEquals(
                new String(Base64.getDecoder().decode("c29tZXRoaW5nIHZlcnkgc2VjcmV0")),
                String.copyValueOf(result));
    }
}