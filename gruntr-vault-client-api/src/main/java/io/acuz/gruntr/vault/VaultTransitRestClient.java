package io.acuz.gruntr.vault;

public interface VaultTransitRestClient {
    static VaultTransitRestClientImpl.Builder builder() {
        return new Builder();
    }

    String encrypt(byte[] unencryptedData);

    byte[] decrypt(String encryptedMasterKey);

    final class Builder {
        String host;
        char[] token;
        String transitPath;

        String transitKeyName;

        public VaultTransitRestClient build() {
            return new VaultTransitRestClientImpl(this);
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
