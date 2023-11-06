package io.acuz.gruntr;

import io.acuz.gruntr.vault.VaultTransitRestClient;
import io.acuz.gruntr.vault.model.VaultToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public final class Client {
    private final Path path;

    private final VaultToken token;

    private final Properties encryptedProperties;

    public Client(Builder builder) {
        this.path = builder.path;
        this.token = builder.token.copyOf();

        this.encryptedProperties = readEncryptedProperties(this.path);

        builder.token.invalidate();
    }

    public static Builder builder() {
        return new Builder();
    }

    private Properties readEncryptedProperties(Path path) {
        var properties = new Properties();
        try (var inputStream = new FileInputStream(path.toFile())) {
            properties.load(inputStream);

            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getPath() {
        return path;
    }

    public Properties getDecryptedProperties() {
        var originalProperties = this.getEncryptedProperties();

        String vaultTransitKey = originalProperties.getProperty("gruntr__vault_transit_key");
        String vaultHost = originalProperties.getProperty("gruntr__vault_host");
        String vaultTransitPath = originalProperties.getProperty("gruntr__vault_transit_path");


        var vault = VaultTransitRestClient.builder()
                .host(vaultHost)
                .token(token)
                .transitPath(vaultTransitPath)
                .transitKeyName(vaultTransitKey)
                .build();

        var properties = new Properties();


        originalProperties.forEach((key, val) -> {
            if (val instanceof String) {
                var stringValue = ((String) val).trim();

                if (stringValue.startsWith("vault:")) {
                    properties.put(key, new String(vault.decrypt(stringValue)));
                } else if (!((String) key).startsWith("gruntr__")) {
                    // non-encrypted value
                    properties.put(key, val);
                }
            }
        });


        return properties;
    }

    public Properties getEncryptedProperties() {
        return (Properties) this.encryptedProperties.clone();
    }

    public static final class Builder {
        private Path path;

        private VaultToken token;

        public Builder setPath(Path path) {
            this.path = path;

            return this;
        }

        public Builder setToken(VaultToken token) {
            this.token = token.copyOf();

            return this;
        }

        public Client build() {
            validate();
            return new Client(this);
        }

        private void validate() {
            Objects.requireNonNull(path);
        }
    }
}
