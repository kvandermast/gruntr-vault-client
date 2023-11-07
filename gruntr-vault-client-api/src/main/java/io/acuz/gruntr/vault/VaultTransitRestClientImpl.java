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
import io.acuz.gruntr.util.ArrayUtils;
import io.acuz.gruntr.vault.exception.VaultException;
import io.acuz.gruntr.vault.model.VaultToken;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class VaultTransitRestClientImpl implements VaultTransitRestClient {
    private static final String JSON_DATA_FIELD = "data";
    private static final String JSON_PLAINTEXT_FIELD = "plaintext";
    private static final String JSON_CIPHERTEXT_FIELD = "ciphertext";
    @SuppressWarnings("UastIncorrectHttpHeaderInspection")
    private static final String HEADER_X_VAULT_TOKEN = "X-Vault-Token";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
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

        builder.token.invalidate();
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

    private char[] request(
            VaultTransitEndpoint endpoint,
            char[] value

    ) throws VaultException {
        var inputFieldName = endpoint == VaultTransitEndpoint.ENCRYPT ? JSON_PLAINTEXT_FIELD : JSON_CIPHERTEXT_FIELD;
        var outputFieldName = endpoint == VaultTransitEndpoint.DECRYPT ? JSON_PLAINTEXT_FIELD : JSON_CIPHERTEXT_FIELD;

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
}
