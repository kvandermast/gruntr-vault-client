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

package io.acuz.gruntr.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class DigestUtils {
    private DigestUtils() {
        //no-op
    }

    public static byte[] sha3digest(String value, String... values) throws NoSuchAlgorithmException {
        Objects.requireNonNull(value);

        if (value.trim().isEmpty())
            throw new NullPointerException("Value cannot be null or empty");

        final MessageDigest digest = MessageDigest.getInstance("SHA3-256");

        byte[] toDigest = value.getBytes();

        for (String val : values) {
            if (null == val || val.trim().isEmpty())
                throw new NullPointerException("Vararg values contained an empty or null string");

            toDigest = append(toDigest, val);
        }

        String sha3Hex = bytesToHex(digest.digest(toDigest));

        return sha3Hex.getBytes();
    }

    private static byte[] append(byte[] source, String value) {
        var valueAsBytes = value.getBytes();
        var bytes = new byte[source.length + valueAsBytes.length];

        System.arraycopy(source, 0, bytes, 0, source.length);
        System.arraycopy(valueAsBytes, 0, bytes, source.length, valueAsBytes.length);

        return bytes;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
