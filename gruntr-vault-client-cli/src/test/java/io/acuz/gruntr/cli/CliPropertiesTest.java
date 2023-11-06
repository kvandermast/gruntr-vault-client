package io.acuz.gruntr.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class CliPropertiesTest {
    @Test
    void test_CliPropertiesBuilderWithoutParameters() {
        var builder = CliProperties.builder();

        assertThrows(NullPointerException.class, builder::build);

        builder.parameters(new ArrayDeque<>());
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void test_CliPropertiesBuilderWithParameters() {
        var builder = CliProperties.builder()
                .parameters(new ArrayDeque<>(
                        List.of("test")
                ));

        assertThrows(NullPointerException.class, builder::build);

        builder.parameters(
                new ArrayDeque<>(
                        List.of("-i",
                                "/tmp/application-encrypted.properties",
                                "--token",
                                "token",
                                "--hc-vault-server",
                                "http://vault:8201",
                                "--hc-transit-path",
                                "transit/project_name",
                                "--hc-transit-key",
                                "appkey")
                )
        );

        var props = builder.build();

        assertNotNull(props);

        assertEquals("http://vault:8201", props.getHcServer().toExternalForm());
        assertArrayEquals("token".toCharArray(), props.getHcToken().getToken());
        assertEquals("appkey", props.getHcTransitKeyName());
        assertEquals("transit/project_name", props.getHcTransitPath());
        assertEquals(Path.of("/tmp/application-encrypted.properties"), props.getInputFilePath());
        assertNull(props.getOutputFilePath());
    }
}