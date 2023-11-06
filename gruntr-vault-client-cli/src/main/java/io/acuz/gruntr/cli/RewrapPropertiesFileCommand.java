package io.acuz.gruntr.cli;

import io.acuz.gruntr.vault.VaultTransitRestClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Properties;

final class RewrapPropertiesFileCommand implements Command {
    private final CliProperties properties;


    private RewrapPropertiesFileCommand(Builder builder) {
        this.properties = builder.properties;
    }


    static Builder builder() {
        return new Builder();
    }

    @Override
    public void run() {
        try (var fileInputStream = new FileInputStream(this.properties.getInputFilePath().toFile())) {
            var vaultClient = VaultTransitRestClient.builder()
                    .host(this.properties.getHcServer())
                    .token(this.properties.getHcToken())
                    .transitPath(this.properties.getHcTransitPath())
                    .transitKeyName(this.properties.getHcTransitKeyName())
                    .build();

            var originalProperties = new Properties();
            var encryptedProperties = new Properties();

            originalProperties.load(fileInputStream);
            originalProperties.forEach((key, value) -> encryptedProperties.put(
                    key,
                    vaultClient.rewrap(((String) value).toCharArray())));

            encryptedProperties.put("gruntr__vault_host", this.properties.getHcServer());
            encryptedProperties.put("gruntr__vault_transit_path", this.properties.getHcTransitPath());
            encryptedProperties.put("gruntr__vault_transit_key", this.properties.getHcTransitKeyName());

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

        public RewrapPropertiesFileCommand build() {
            validate();
            return new RewrapPropertiesFileCommand(this);
        }

        private void validate() {
            Objects.requireNonNull(properties);
        }
    }
}
