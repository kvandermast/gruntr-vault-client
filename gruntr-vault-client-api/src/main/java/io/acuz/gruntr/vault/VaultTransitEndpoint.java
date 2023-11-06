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
