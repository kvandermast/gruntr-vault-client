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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionKeysTest {
    @BeforeAll
    static void beforeAll() {
        EncryptionKeys.clear();
    }

    @AfterAll
    static void afterAll() {
        EncryptionKeys.clear();
    }

    @Test
    void testAll() {
        EncryptionKeys.register("(fish|chips)", "crisps", "(^kit|kat$)");
        testWithCustomExpressions(EncryptionKeys.compile());

        EncryptionKeys.register(EncryptionKeys.SECRETS);
        testWithSecretsGroup(EncryptionKeys.compile());

        EncryptionKeys.register(EncryptionKeys.ALL);
        testWithAllRegExp(EncryptionKeys.compile());
    }

    private void testWithSecretsGroup(Pattern exp) {
        assertTrue(exp.matcher("secret").matches());
        assertTrue(exp.matcher("token").matches());
        assertTrue(exp.matcher("password").matches());

        assertTrue(exp.matcher("my.secret").matches());
        assertTrue(exp.matcher("token.secure").matches());
        assertTrue(exp.matcher("my.password.for.a.service").matches());
        assertFalse(exp.matcher("my.plaintext").matches());
    }

    private void testWithAllRegExp(Pattern exp) {
        assertTrue(exp.matcher("test").matches());
        assertTrue(exp.matcher("some.secure.token").matches());
        assertTrue(exp.matcher("some_password").matches());
        assertTrue(exp.matcher("some/secret").matches());

        assertTrue(exp.matcher("my.plaintext").matches());
    }

    private void testWithCustomExpressions(Pattern exp) {
        // these are registered
        assertTrue(exp.matcher("my.fish.security").matches());
        assertTrue(exp.matcher("my.crisps.security").matches());
        assertTrue(exp.matcher("my.chips").matches());
        assertTrue(exp.matcher("kit.and.kat").matches());

        //these are not
        assertFalse(exp.matcher("secret").matches());
        assertFalse(exp.matcher("token").matches());
        assertFalse(exp.matcher("password").matches());
    }
}