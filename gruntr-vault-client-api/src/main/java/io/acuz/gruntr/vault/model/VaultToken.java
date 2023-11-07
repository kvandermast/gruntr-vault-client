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
