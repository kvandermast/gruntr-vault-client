package io.acuz.gruntr.vault;

import java.util.Objects;

public interface VaultTransitRestClient {
    static VaultTransitRestClientImpl.Builder builder() {
        return new Builder();
    }

    String encrypt(byte[] unencryptedData);

    byte[] decrypt(String encryptedMasterKey);

    char[] rewrap(char[] originalToken);

    final class Builder {
        String host;
        char[] token;
        String transitPath;

        String transitKeyName;

        public VaultTransitRestClient build() {
            validate();
            return new VaultTransitRestClientImpl(this);
        }

        private void validate() {
            Objects.requireNonNull(host, "Vault host should not be null");
            Objects.requireNonNull(token, "Vault token should not be null");
            Objects.requireNonNull(transitPath, "Vault transit path should not be null");
            Objects.requireNonNull(transitKeyName, "Vault transit key name should not be null");
        }

        public VaultTransitRestClientImpl.Builder host(String hcServer) {
            this.host = hcServer;
            return this;
        }

        public VaultTransitRestClientImpl.Builder token(char[] hcToken) {
            this.token = new char[hcToken.length];
            System.arraycopy(hcToken, 0, this.token, 0, hcToken.length);

            return this;
        }

        public VaultTransitRestClientImpl.Builder transitPath(String hcTransitPath) {
            this.transitPath = hcTransitPath;
            return this;
        }

        public VaultTransitRestClientImpl.Builder transitKeyName(String hcTransitKeyName) {
            this.transitKeyName = hcTransitKeyName;

            return this;
        }
    }
}
