package io.acuz.gruntr.vault;

import io.acuz.gruntr.vault.model.VaultToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@SuppressWarnings("SpellCheckingInspection")
class VaultRestClientTest {
    @Test
    void test_ShouldNotFailOnCall() throws MalformedURLException {
        var url = URI.create("http://vault:8201").toURL();

        var client = VaultTransitRestClient
                .builder()
                .host(url)
                .transitPath("transit/project_name")
                .transitKeyName("appkey")
                .token(VaultToken.of("root"))
                .build();
        Assertions.assertDoesNotThrow(() -> client.decrypt("vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ=="));

        var result = client.decrypt("vault:v1:pN9yeht0umD/TqT3tSpRGUoLUuTYazDPgxj/dkOJTULzFCv2vovHgbhBfh99EmD+wQ==");

        assertArrayEquals(Base64.getDecoder().decode("c29tZXRoaW5nIHZlcnkgc2VjcmV0"), result);
    }
}