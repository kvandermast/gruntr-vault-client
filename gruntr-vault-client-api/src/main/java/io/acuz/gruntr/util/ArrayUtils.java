package io.acuz.gruntr.util;

public final class ArrayUtils {
    private ArrayUtils() {
        //no-op
    }

    public static char[] toCharArray(byte[] bytes) {
        var chars = new char[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) bytes[i];
        }

        return chars;
    }
}
