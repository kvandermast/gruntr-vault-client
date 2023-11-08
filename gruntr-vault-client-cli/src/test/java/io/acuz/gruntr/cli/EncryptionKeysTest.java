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

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionKeysTest {
    @Test
    void testAll() {
        EncryptionKeys.register(EncryptionKeys.SECRETS);
        testWithSecretsGroup(EncryptionKeys.compile());
        EncryptionKeys.clear();

        EncryptionKeys.register(EncryptionKeys.ALL);
        testWithAllRegExp(EncryptionKeys.compile());
        EncryptionKeys.clear();


        EncryptionKeys.register(EncryptionKeys.SECRETS, "(fish|chips)", "crisps", "(^kit|kat$)");
        testWithCustomExpressions(EncryptionKeys.compile());
        EncryptionKeys.clear();

    }

    private void testWithSecretsGroup(Pattern exp) {
        assertTrue(exp.matcher("secret").find());
        assertTrue(exp.matcher("token").find());
        assertTrue(exp.matcher("password").find());

        assertTrue(exp.matcher("my.secret").find());
        assertTrue(exp.matcher("token.secure").find());
        assertTrue(exp.matcher("my.password.for.a.service").find());
        assertFalse(exp.matcher("my.plaintext").find());
    }

    private void testWithAllRegExp(Pattern exp) {
        assertTrue(exp.matcher("").find());
        assertTrue(exp.matcher(" ").find());
        assertTrue(exp.matcher("test").find());
        assertTrue(exp.matcher("some.secure.token").find());
        assertTrue(exp.matcher("some_password").find());
        assertTrue(exp.matcher("some/secret").find());
    }

    private void testWithCustomExpressions(Pattern exp) {
        assertTrue(exp.matcher("my.secret").find());
        assertTrue(exp.matcher("token.secure").find());
        assertTrue(exp.matcher("my.password.for.a.service").find());
        assertTrue(exp.matcher("my.fish.security").find());
        assertTrue(exp.matcher("my.crisps.security").find());
        assertTrue(exp.matcher("my.chips").find());
        assertTrue(exp.matcher("kit.and.kat").find());

        assertFalse(exp.matcher("my.plaintext").find());
    }
}