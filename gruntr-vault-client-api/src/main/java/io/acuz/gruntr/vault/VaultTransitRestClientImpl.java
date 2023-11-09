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

package io.acuz.gruntr.vault;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.acuz.gruntr.cli.EncryptionKeys;
import io.acuz.gruntr.util.ArrayUtils;
import io.acuz.gruntr.util.DigestUtils;
import io.acuz.gruntr.vault.exception.VaultException;
import io.acuz.gruntr.vault.model.VaultToken;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public final class VaultTransitRestClientImpl implements VaultTransitRestClient {
    public static final String GRUNTR__VAULT_TRANSIT_KEY = "gruntr__vault_transit_key";
    public static final String GRUNTR__VAULT_HOST = "gruntr__vault_host";
    public static final String GRUNTR__VAULT_TRANSIT_PATH = "gruntr__vault_transit_path";
    public static final String GRUNTR__SHA_3 = "gruntr__sha3";
    private static final String JSON_DATA_FIELD = "data";
    @SuppressWarnings("UastIncorrectHttpHeaderInspection")
    private static final String HEADER_X_VAULT_TOKEN = "X-Vault-Token";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String GRUNTR__PREFIX = "gruntr__";
    private static final String VAULT_PREFIX = "vault:";
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory factory = mapper.getFactory();
    private final URL host;
    private final VaultToken token;
    private final String transitPath;
    private final String transitKeyName;


    VaultTransitRestClientImpl(Builder builder) {
        this.host = builder.host;
        this.token = builder.token.copyOf();

        this.transitPath = builder.transitPath;
        this.transitKeyName = builder.transitKeyName;
    }

    @Override
    public char[] encrypt(byte[] value) throws VaultException {
        return this.request(
                VaultTransitEndpoint.ENCRYPT,
                Base64.getEncoder().encodeToString(value).toCharArray()
        );
    }

    @Override
    public char[] decrypt(char[] value) throws VaultException {
        char[] base64EncodedPlainText = this.request(
                VaultTransitEndpoint.DECRYPT,
                value
        );

        return ArrayUtils.toCharArray(
                Base64.getDecoder().decode(
                        String.copyValueOf(base64EncodedPlainText)));
    }

    @Override
    public char[] rewrap(char[] value) throws VaultException {
        return this.request(
                VaultTransitEndpoint.REWRAP,
                value
        );
    }

    private char[] request(VaultTransitEndpoint endpoint, char[] value) throws VaultException {
        var inputFieldName = endpoint.getInputFieldName();
        var outputFieldName = endpoint.getOutputFieldName();

        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var data = String.format("{\"%s\": \"%s\"}", inputFieldName, String.copyValueOf(value));

        var request = HttpRequest.newBuilder()
                .uri(endpoint.from(this.host, this.transitPath, transitKeyName))
                .header(HEADER_X_VAULT_TOKEN, this.token.stringValue())
                .header(HEADER_ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (200 == response.statusCode()) {
                var parser = factory.createParser(response.body());
                var jsonTree = mapper.readTree(parser);

                if (null != jsonTree && null != jsonTree.get(JSON_DATA_FIELD)) {
                    var jsonData = (JsonNode) jsonTree.get(JSON_DATA_FIELD);

                    if (jsonData.isObject() && null != jsonData.get(outputFieldName)) {
                        var ciphertext = jsonData.get(outputFieldName).asText();

                        return ciphertext.toCharArray();
                    }
                }

                throw new VaultException("Vault returned unexpected body structure");
            } else {
                throw new VaultException("Vault was unable to handle request, returned statusCode: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new VaultException("Unable to communicate with Vault", e);
        }
    }

    @Override
    public Properties decrypt(Properties properties) throws VaultException {
        validateGruntrSha(properties);

        var decryptedProperties = new Properties();

        properties.forEach((key, val) -> {
            if (val instanceof String) {
                var stringValue = ((String) val).trim();
                var keyName = (String) key;

                if (!keyName.toLowerCase().startsWith(GRUNTR__PREFIX)) {
                    if (stringValue.startsWith(VAULT_PREFIX)) {
                        try {
                            decryptedProperties.put(key, String.copyValueOf(decrypt(stringValue.toCharArray())));
                        } catch (VaultException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // non-encrypted value
                        decryptedProperties.put(key, val);
                    }
                }
            }
        });

        return decryptedProperties;
    }

    @Override
    public Properties encrypt(Properties properties) throws VaultException {
        var keysToEncrypt = EncryptionKeys.compile();
        var encryptedProperties = new Properties();

        properties.forEach((key, value) -> {
            try {
                // the property key matches the keys required to be encrypted
                var keyName = (String) key;
                var stringValue = (String) value;

                if (keysToEncrypt.matcher(keyName).find()) {
                    encryptedProperties.put(
                            key,
                            String.copyValueOf(
                                    encrypt(stringValue.getBytes())
                            )
                    );
                } else {
                    // there is no need to encrypt this key
                    encryptedProperties.put(keyName, stringValue);
                }
            } catch (VaultException e) {
                throw new RuntimeException(e);
            }
        });

        return encryptedProperties;
    }

    private void validateGruntrSha(Properties properties) throws VaultException {
        var vaultTransitKey = requireNonNull(properties.getProperty(GRUNTR__VAULT_TRANSIT_KEY), "Cannot validate hash, missing Vault Transit Key");
        var vaultHost = requireNonNull(properties.getProperty(GRUNTR__VAULT_HOST), "Cannot validate hash, missing Vault host");
        var vaultTransitPath = requireNonNull(properties.getProperty(GRUNTR__VAULT_TRANSIT_PATH), "Cannot validate hash, missing Vault Transit Path");
        var gruntrSha3Value = requireNonNull(properties.getProperty(GRUNTR__SHA_3), "Cannot validate hash, missing Vault SHA3 value");

        try {
            var sha3HexValue = this.decrypt(gruntrSha3Value.toCharArray());
            var recomputedSha3HexValue = ArrayUtils.toCharArray(DigestUtils.sha3digest(vaultHost, vaultTransitPath, vaultTransitKey));

            if (!Arrays.equals(sha3HexValue, recomputedSha3HexValue)) {
                throw new IllegalStateException("Hash validation failed, gruntr__ values were tampered with?");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Properties rewrap(Properties properties) throws VaultException {
        validateGruntrSha(properties);

        var encryptedProperties = new Properties();

        properties.forEach((key, val) -> {
            var stringValue = ((String) val).trim();
            var keyName = (String) key;

            if (!keyName.toLowerCase().startsWith(GRUNTR__PREFIX)) {
                if (stringValue.startsWith(VAULT_PREFIX)) {
                    try {
                        encryptedProperties.put(
                                key,
                                String.copyValueOf(
                                        rewrap(
                                                stringValue.toCharArray()
                                        )
                                )
                        );
                    } catch (VaultException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    encryptedProperties.put(key, stringValue);
                }
            } else {
                encryptedProperties.put(key, stringValue);
            }
        });

        return encryptedProperties;
    }
}
