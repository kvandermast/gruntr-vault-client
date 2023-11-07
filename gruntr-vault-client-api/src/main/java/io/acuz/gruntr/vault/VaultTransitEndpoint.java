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
    ENCRYPT("encrypt"),
    DECRYPT("decrypt"),
    REWRAP("rewrap");

    private final String action;

    VaultTransitEndpoint(String action) {
        this.action = action;
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
}
