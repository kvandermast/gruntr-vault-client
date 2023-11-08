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

package io.acuz.gruntr.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionKeysTest {
    @Test
    void testEncryptionKeyMatcher() {
        EncryptionKeys.register(EncryptionKeys.SECRETS);

        var exp = EncryptionKeys.compile();

        assertTrue(exp.matcher("secret").find());
        assertTrue(exp.matcher("token").find());
        assertTrue(exp.matcher("password").find());

        assertTrue(exp.matcher("my.secret").find());
        assertTrue(exp.matcher("token.secure").find());
        assertTrue(exp.matcher("my.password.for.a.service").find());
        assertFalse(exp.matcher("my.plaintext").find());
    }
}