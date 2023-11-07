package io.acuz.gruntr.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

public final class VaultValidationUtil {
    public static final Pattern HTTP_SCHEME_PATTERN = Pattern.compile("^https?$", Pattern.CASE_INSENSITIVE);
    public static final Pattern URL_PATH_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9/_]*$", Pattern.CASE_INSENSITIVE);

    private VaultValidationUtil() {
        //no-op
    }

    public static void checkVaultHost(String vaultHost) {
        if (null == vaultHost || vaultHost.trim().isEmpty()) {
            throw new NullPointerException("Missing Vault Host/Server");
        }

        URL uri;
        try {
            uri = URI.create(vaultHost).toURL();
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Vault host provided: unable to parse '" + vaultHost + "'", e);
        }

        if (null == uri.getHost() || uri.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid Vault host provided: missing host information");
        }

        if (!HTTP_SCHEME_PATTERN.matcher(uri.getProtocol()).matches()) {
            throw new IllegalArgumentException("Invalid Vault host provided: only supporting http and https, got " + uri.getProtocol());
        }

        if (null != uri.getQuery() && !uri.getQuery().isEmpty()) {
            throw new IllegalArgumentException("Invalid Vault host provided: we don't support query parameters, got " + uri.getQuery());
        }
    }

    public static void checkVaultPathComponent(String value) {
        if (null == value || value.trim().isEmpty()) {
            throw new NullPointerException("Vault transit value or key name is null or empty");
        }

        if (!URL_PATH_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid Vault transit value or key name provided, received '" + value + "' does not match '" + URL_PATH_PATTERN.pattern() + "'");
        }
    }
}
