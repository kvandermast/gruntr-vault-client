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

import io.acuz.gruntr.util.VaultValidationUtil;
import io.acuz.gruntr.vault.exception.VaultException;
import io.acuz.gruntr.vault.model.VaultToken;

import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public interface VaultTransitRestClient {
    static VaultTransitRestClientImpl.Builder builder() {
        return new Builder();
    }

    char[] encrypt(byte[] value) throws VaultException;

    char[] decrypt(char[] value) throws VaultException;

    char[] rewrap(char[] value) throws VaultException;

    Properties decrypt(Properties properties) throws VaultException;

    Properties encrypt(Properties properties) throws VaultException;

    Properties rewrap(Properties properties) throws VaultException;

    final class Builder {
        URL host;
        VaultToken token;
        String transitPath;
        String transitKeyName;

        public VaultTransitRestClient build() {
            validate();
            return new VaultTransitRestClientImpl(this);
        }

        private void validate() {
            Objects.requireNonNull(token, "Vault token should not be null");

            VaultValidationUtil.checkVaultHost(host.toExternalForm());
            VaultValidationUtil.checkVaultPathComponent(transitPath);
            VaultValidationUtil.checkVaultPathComponent(transitKeyName);
        }

        public VaultTransitRestClientImpl.Builder host(URL hcServer) {
            this.host = hcServer;
            return this;
        }

        public VaultTransitRestClientImpl.Builder token(VaultToken hcToken) {
            this.token = hcToken;
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
