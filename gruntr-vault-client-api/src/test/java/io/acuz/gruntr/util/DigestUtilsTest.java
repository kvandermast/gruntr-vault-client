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

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class DigestUtilsTest {

    @Test
    void testSha3DigestWithSingleValue() throws NoSuchAlgorithmException {
        var result = DigestUtils.sha3digest("test");

        assertNotNull(result);
        assertEquals("36f028580bb02cc8272a9a020f4200e346e276ae664e45ee80745574e2f5ab80", new String(result));
    }

    @Test
    void testSha3DigestWithMultipleValues() throws NoSuchAlgorithmException {
        var result = DigestUtils.sha3digest("test", "message", "hello", "world!");

        assertNotNull(result);
        assertEquals("412e555fcbfc43f2e1ab2ed3d3b3bf5cc020dc4ad0bedb4ec88923c714e0888b", new String(result));
    }

    @Test
    void testSha3DigestValidation() {
        assertThrows(NullPointerException.class, () -> DigestUtils.sha3digest(null));
        assertThrows(NullPointerException.class, () -> DigestUtils.sha3digest(""));
        assertThrows(NullPointerException.class, () -> DigestUtils.sha3digest("test", (String) null));
    }
}