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

import java.net.URI;
import java.net.URL;


enum VaultTransitEndpoint {
    DECRYPT("decrypt", "ciphertext", "plaintext"),
    ENCRYPT("encrypt", "plaintext", "ciphertext"),
    REWRAP("rewrap", "ciphertext", "ciphertext");

    private final String action;
    private final String inputFieldName;
    private final String outputFieldName;

    VaultTransitEndpoint(String action, String inputFieldName, String outputFieldName) {
        this.action = action;
        this.inputFieldName = inputFieldName;
        this.outputFieldName = outputFieldName;
    }

    public URI from(URL host, String mountPath, String keyName) {
        return URI.create(
                String.format("%s/v1/%s/%s/%s",
                        host.toExternalForm(),
                        mountPath,
                        action,
                        keyName
                )
        );
    }

    public String getInputFieldName() {
        return inputFieldName;
    }

    public String getOutputFieldName() {
        return outputFieldName;
    }
}
