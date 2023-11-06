package io.acuz.gruntr.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayUtilsTest {
    @Test
    void test_ToCharArray() {
        var message = "this is a test";
        var chars = ArrayUtils.toCharArray(message.getBytes());

        assertEquals(message, String.copyValueOf(chars));
    }
}