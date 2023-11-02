package io.acuz.gruntr.cli;

import io.acuz.gruntr.vault.VaultTransitRestClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Properties;

public final class DecryptPropertiesFileCommand implements Command {
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
            var vaultClient = VaultTransitRestClient.builder()
                    .host(this.properties.getHcServer())
                    .token(this.properties.getHcToken())
                    .transitPath(this.properties.getHcTransitPath())
                    .build();

            var originalProperties = new Properties();
            var encryptedProperties = new Properties();

            originalProperties.load(fileInputStream);
            originalProperties.forEach((key, value) -> encryptedProperties.put(
                    key,
                    new String(vaultClient.decrypt((String) value))
            ));

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
