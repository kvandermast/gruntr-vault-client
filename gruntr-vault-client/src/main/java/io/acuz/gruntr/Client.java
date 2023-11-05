package io.acuz.gruntr;

import io.acuz.gruntr.vault.VaultTransitRestClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public final class Client {
    private final Path path;

    private final char[] token;

    private final Properties encryptedProperties;

    public Client(Builder builder) {
        this.path = builder.path;
        this.token = new char[builder.token.length];
        System.arraycopy(builder.token, 0, this.token, 0, this.token.length);

        this.encryptedProperties = readEncryptedProperties(this.path);

        Arrays.fill(builder.token, '\0');
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

        private char[] token;

        public Builder setPath(Path path) {
            this.path = path;

            return this;
        }

        public Builder setToken(char[] token) {
            this.token = new char[token.length];

            System.arraycopy(token, 0, this.token, 0, token.length);

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
