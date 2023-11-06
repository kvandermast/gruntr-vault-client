package io.acuz.gruntr.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultValidationUtilTest {
    @Test
    void whenVaultHostContainsGarbage() {
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("null"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("x//fake"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("http//localhost:8200"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("http:/localhost:8200"));
    }

    @Test
    void whenVaultHostIsNullOrEmpty() {
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultHost(null));
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultHost(""));
        assertThrows(NullPointerException.class, () -> VaultValidationUtil.checkVaultHost("    "));
    }

    @Test
    void whenVaultHostIsNotHttpCompliant() {
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("ftp://server"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("ssl://something"));
    }

    @Test
    void whenVaultHostHasQueryParameters() {
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("http://server?server=x"));
        assertThrows(IllegalArgumentException.class, () -> VaultValidationUtil.checkVaultHost("https://server/subcontext?redirectTo=http"));
    }

    @Test
    void whenVaultHostIsValid() {
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultHost("http://localhost"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultHost("http://localhost:8200"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultHost("https://localhost:8200"));
        assertDoesNotThrow(() -> VaultValidationUtil.checkVaultHost("HTTps://vault:8200/v1"));
    }
}