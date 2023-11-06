package io.acuz.gruntr.vault;

import java.net.URI;

enum VaultTransitEndpoint {
    ENCRYPT("encrypt"),
    DECRYPT("decrypt"),
    REWRAP("rewrap");

    private final String action;

    VaultTransitEndpoint(String action) {
        this.action = action;
    }

    public URI uri(String host, String mountPath, String keyName) {
        return URI.create(
                String.format("%s/v1/%s/%s/%s",
                        host,
                        mountPath,
                        action,
                        keyName
                )
        );
    }
}
