package org.gruntr.sops.client.cli;

import org.gruntr.sops.client.vault.VaultRestClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Properties;

public final class EncryptPropertiesFileCommand implements Command {
    private final CliProperties properties;


    private EncryptPropertiesFileCommand(Builder builder) {
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

            var vaultClient = VaultRestClient.builder()
                    .host(this.properties.getHcServer())
                    .token(this.properties.getHcToken())
                    .transitPath(this.properties.getHcTransitPath())
                    .build();

            var encryptedProperties = new Properties();

            originalProperties.entrySet().forEach(entry -> {
                encryptedProperties.put(
                        entry.getKey(),
                        vaultClient.encrypt(((String) entry.getValue()).getBytes()));
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

        public EncryptPropertiesFileCommand build() {
            validate();
            return new EncryptPropertiesFileCommand(this);
        }

        private void validate() {
            Objects.requireNonNull(properties);
        }
    }
}
