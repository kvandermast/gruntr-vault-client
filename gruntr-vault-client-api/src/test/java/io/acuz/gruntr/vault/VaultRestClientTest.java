package io.acuz.gruntr.vault;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class VaultRestClientTest {
    @Test
    void test_ShouldNotFailOnCall() {
        var client = VaultTransitRestClient
                .builder()
                .host("http://vault:8201")
                .transitPath("transit/project_name")
                .transitKeyName("appkey")
                .token("root".toCharArray())
                .build();
        Assertions.assertDoesNotThrow(() -> client.decrypt("vault:v1:BG0m4DWGwiGq7/G4FqGiTVrTYOz6qMtAXJ9a7ZZS/18i0/GjJIosu7bhJeTYky8ExbZPBTxgNuyas7Kv"));

        var result = client.decrypt("vault:v1:BG0m4DWGwiGq7/G4FqGiTVrTYOz6qMtAXJ9a7ZZS/18i0/GjJIosu7bhJeTYky8ExbZPBTxgNuyas7Kv");

        assertArrayEquals(Base64.getDecoder().decode("HRuB7Y1/9jpbk1lR2m579U571Z97GuDXrerbGA3c+Ao="), result);
    }
}