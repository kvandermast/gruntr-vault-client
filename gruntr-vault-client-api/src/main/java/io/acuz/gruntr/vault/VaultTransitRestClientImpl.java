package io.acuz.gruntr.vault;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.acuz.gruntr.vault.model.VaultToken;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class VaultTransitRestClientImpl implements VaultTransitRestClient {
    @SuppressWarnings("UastIncorrectHttpHeaderInspection")
    private static final String HEADER_X_VAULT_TOKEN = "X-Vault-Token";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory factory = mapper.getFactory();
    private final String host;
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
    public String encrypt(byte[] unencryptedData) {
        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var data = "{\"plaintext\": \"" + Base64.getEncoder().encodeToString(unencryptedData) + "\"}";

        var request = HttpRequest.newBuilder()
                .uri(VaultTransitEndpoint.ENCRYPT.uri(this.host, this.transitPath, transitKeyName))
                .header(HEADER_X_VAULT_TOKEN, this.token.stringValue())
                .header(HEADER_ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (200 == response.statusCode()) {
                var parser = factory.createParser(response.body());
                var jsonTree = mapper.readTree(parser);

                if (null != jsonTree && null != jsonTree.get("data")) {
                    var jsonData = (JsonNode) jsonTree.get("data");

                    if (jsonData.isObject() && null != jsonData.get("ciphertext")) {
                        return jsonData.get("ciphertext").asText();
                    }
                }
            } else
                System.out.println("Something went wrong decrypting with Vault");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalStateException("Unable to encrypt the requested value");

    }

    @Override
    public byte[] decrypt(String encryptedMasterKey) {
        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var data = "{\"ciphertext\": \"" + encryptedMasterKey + "\"}";

        var request = HttpRequest.newBuilder()
                .uri(VaultTransitEndpoint.DECRYPT.uri(this.host, this.transitPath, transitKeyName))
                .header(HEADER_X_VAULT_TOKEN, this.token.stringValue())
                .header(HEADER_ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (200 == response.statusCode()) {
                var parser = factory.createParser(response.body());
                var jsonTree = mapper.readTree(parser);

                if (null != jsonTree && null != jsonTree.get("data")) {
                    var jsonData = (JsonNode) jsonTree.get("data");

                    if (jsonData.isObject() && null != jsonData.get("plaintext")) {
                        var plainText = jsonData.get("plaintext").asText();

                        return Base64.getDecoder().decode(plainText);
                    }
                }
            } else
                System.out.println("Something went wrong decrypting with Vault");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalStateException("Unable to decrypt the requested value");
    }

    @Override
    public char[] rewrap(char[] originalToken) {
        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var data = "{\"ciphertext\": \"" + String.copyValueOf(originalToken) + "\"}";

        var request = HttpRequest.newBuilder()
                .uri(VaultTransitEndpoint.REWRAP.uri(this.host, this.transitPath, transitKeyName))
                .header(HEADER_X_VAULT_TOKEN, this.token.stringValue())
                .header(HEADER_ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (200 == response.statusCode()) {
                var parser = factory.createParser(response.body());
                var jsonTree = mapper.readTree(parser);

                if (null != jsonTree && null != jsonTree.get("data")) {
                    var jsonData = (JsonNode) jsonTree.get("data");

                    if (jsonData.isObject() && null != jsonData.get("ciphertext")) {
                        var ciphertext = jsonData.get("ciphertext").asText();

                        return ciphertext.toCharArray();
                    }
                }
            } else
                System.out.println("Something went wrong decrypting with Vault");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalStateException("Unable to decrypt the requested value");
    }
}
