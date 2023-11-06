package io.acuz.gruntr.vault.model;

import java.util.Arrays;

public final class VaultToken {
    private final char[] token;

    private VaultToken(char[] token) {
        this.token = new char[token.length];
        System.arraycopy(token, 0, this.token, 0, token.length);
    }

    public static VaultToken of(String token) {
        return new VaultToken(token.toCharArray());
    }

    public String stringValue() {
        return String.copyValueOf(this.token);
    }

    public char[] getToken() {
        return this.token;
    }

    public void invalidate() {
        if (null != this.token)
            Arrays.fill(this.token, '\0');
    }

    public VaultToken copyOf() {
        return new VaultToken(this.token.clone());
    }
}
