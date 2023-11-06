package io.acuz.gruntr.cli;

import io.acuz.gruntr.vault.VaultTransitRestClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Properties;

final class DecryptPropertiesFileCommand implements Command {
    private final CliProperties properties;


    private DecryptPropertiesFileCommand(Builder builder) {
        this.properties = builder.properties;
    }


    static Builder builder() {
        return new Builder();
    }

    @Override
    public void run() {
        try (var fileInputStream = new FileInputStream(this.properties.getInputFilePath().toFile())) {
            var originalProperties = new Properties();
            originalProperties.load(fileInputStream);

            String vaultTransitKey = originalProperties.getProperty("gruntr__vault_transit_key");
            String vaultHost = originalProperties.getProperty("gruntr__vault_host");
            String vaultTransitPath = originalProperties.getProperty("gruntr__vault_transit_path");

            if (!this.properties.getHcTransitKeyName().equals(vaultTransitKey)
                    || !this.properties.getHcServer().toExternalForm().equals(vaultHost)
                    || !this.properties.getHcTransitPath().equals(vaultTransitPath)) {
                throw new IllegalStateException("Configuration mismatch");
            }

            var vaultClient = VaultTransitRestClient.builder()
                    .host(URI.create(vaultHost).toURL())
                    .token(this.properties.getHcToken())
                    .transitPath(vaultTransitPath)
                    .transitKeyName(vaultTransitKey)
                    .build();

            var encryptedProperties = new Properties();

            originalProperties.forEach((key, value) -> {
                var kn = (String) key;

                if (!kn.toLowerCase().startsWith("gruntr__")) {
                    encryptedProperties.put(
                            key,
                            new String(vaultClient.decrypt((String) value)));
                }
            });

            if (null == properties.getOutputFilePath()) {
                encryptedProperties.store(System.out, "");
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static final class Builder {

        private CliProperties properties;

        public Builder parameters(ArrayDeque<String> params) {
            this.properties = CliProperties.builder()
                    .parameters(params)
                    .build();

            return this;
        }

        public DecryptPropertiesFileCommand build() {
            validate();
            return new DecryptPropertiesFileCommand(this);
        }

        private void validate() {
            Objects.requireNonNull(properties);
        }
    }
}
