/*
 * Copyright (c) 2023. Gruntr/ACUZIO BV
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * https://www.gnu.org/licenses/gpl-3.0.html
 *
 */

package io.acuz.gruntr;

import io.acuz.gruntr.util.ArrayUtils;
import io.acuz.gruntr.util.DigestUtils;
import io.acuz.gruntr.vault.VaultTransitRestClient;
import io.acuz.gruntr.vault.exception.VaultException;
import io.acuz.gruntr.vault.model.VaultToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public final class ClientImpl implements Client {
    private final Path path;

    private final VaultToken token;

    private final Properties encryptedProperties;

    public ClientImpl(Builder builder) {
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

    @Override
    public Properties decryptProperties() {
        var originalProperties = this.getEncryptedProperties();

        String vaultTransitKey = originalProperties.getProperty("gruntr__vault_transit_key");
        String vaultHost = originalProperties.getProperty("gruntr__vault_host");
        String vaultTransitPath = originalProperties.getProperty("gruntr__vault_transit_path");
        String gruntrSha3Value = originalProperties.getProperty("gruntr__sha3");

        Objects.requireNonNull(gruntrSha3Value);

        VaultTransitRestClient vaultTransitRestClient;
        try {
            vaultTransitRestClient = VaultTransitRestClient.builder()
                    .host(URI.create(vaultHost).toURL())
                    .token(token)
                    .transitPath(vaultTransitPath)
                    .transitKeyName(vaultTransitKey)
                    .build();

            var sha3HexValue = vaultTransitRestClient.decrypt(gruntrSha3Value.toCharArray());
            var recomputedSha3HexValue = ArrayUtils.toCharArray(DigestUtils.sha3digest(vaultHost, vaultTransitPath, vaultTransitKey));

            if (!Arrays.equals(sha3HexValue, recomputedSha3HexValue)) {
                throw new IllegalStateException("Hash validation failed, gruntr__ values were tampered with?");
            }
        } catch (MalformedURLException | VaultException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        var properties = new Properties();

        originalProperties.forEach((key, val) -> {
            if (val instanceof String) {
                var stringValue = ((String) val).trim();
                var keyName = (String) key;

                if (!keyName.startsWith("gruntr__")) {
                    if (stringValue.startsWith("vault:")) {
                        try {
                            properties.put(key, String.copyValueOf(vaultTransitRestClient.decrypt(stringValue.toCharArray())));
                        } catch (VaultException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // non-encrypted value
                        properties.put(key, val);
                    }
                }
            }
        });

        return properties;
    }

    @Override
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

        public ClientImpl build() {
            validate();
            return new ClientImpl(this);
        }

        private void validate() {
            Objects.requireNonNull(path);
        }
    }
}
