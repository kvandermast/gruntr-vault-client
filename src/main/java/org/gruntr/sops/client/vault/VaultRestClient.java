package org.gruntr.sops.client.vault;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class VaultRestClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory factory = mapper.getFactory();
    private final String host;
    private final String token;
    private final String transitPath;

    private VaultRestClient(Builder builder) {
        this.host = builder.host;
        this.token = builder.token;
        this.transitPath = builder.transitPath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String encrypt(byte[] unencryptedData) {
        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var data = "{\"plaintext\": \"" + Base64.getEncoder().encodeToString(unencryptedData) + "\"}";

        var request = HttpRequest.newBuilder()
                .uri(URI.create(this.host + "/v1/" + this.transitPath + "/encrypt/appkey"))
                .header("X-Vault-Token", this.token)
                .header("Accept", "application/json")
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

    public byte[] decrypt(String encryptedMasterKey) {
        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var data = "{\"ciphertext\": \"" + encryptedMasterKey + "\"}";

        var request = HttpRequest.newBuilder()
                .uri(URI.create(this.host + "/v1/" + this.transitPath + "/decrypt/appkey"))
                .header("X-Vault-Token", this.token)
                .header("Accept", "application/json")
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

    public static final class Builder {
        private String host;
        private String token;
        private String transitPath;

        public VaultRestClient build() {
            return new VaultRestClient(this);
        }

        public Builder host(String hcServer) {
            this.host = hcServer;
            return this;
        }

        public Builder token(String hcToken) {
            this.token = hcToken;
            return this;
        }

        public Builder transitPath(String hcTransitPath) {
            this.transitPath = hcTransitPath;
            return this;
        }
    }
}
