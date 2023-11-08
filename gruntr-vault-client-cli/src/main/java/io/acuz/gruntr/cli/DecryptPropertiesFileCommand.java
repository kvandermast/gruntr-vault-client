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

import io.acuz.gruntr.Client;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Objects;

final class DecryptPropertiesFileCommand implements Command {
    private final CliProperties properties;


    private DecryptPropertiesFileCommand(Builder builder) {
        this.properties = builder.properties;
    }


    static Builder builder() {
        return new Builder();
    }

    @Override
    public void run() {
        var client = Client.builder()
                .setPath(this.properties.getInputFilePath())
                .setToken(this.properties.getHcToken())
                .build();

        var decryptedProperties = client.getDecryptedProperties();
        try {
            if (null == properties.getOutputFilePath()) {
                decryptedProperties.store(System.out, "");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static final class Builder {

        private CliProperties properties;

        public Builder parameters(ArrayDeque<String> params) {
            this.properties = CliProperties.builder()
                    .parameters(params)
                    .build();

            return this;
        }

        public DecryptPropertiesFileCommand build() {
            validate();
            return new DecryptPropertiesFileCommand(this);
        }

        private void validate() {
            Objects.requireNonNull(properties);
        }
    }
}
