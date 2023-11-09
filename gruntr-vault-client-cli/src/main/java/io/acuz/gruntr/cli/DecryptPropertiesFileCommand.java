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

import io.acuz.gruntr.ClientImpl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Objects;

final class DecryptPropertiesFileCommand extends AbstractCommand implements Command {
    private DecryptPropertiesFileCommand(Builder builder) {
        super(builder.properties);
    }


    static Builder builder() {
        return new Builder();
    }

    @Override
    public void run() {
        var client = ClientImpl.builder()
                .setPath(this.cliProperties.getInputFilePath())
                .setToken(this.cliProperties.getHcToken())
                .build();

        try {
            storeProperties(client.decryptProperties());
        } catch (IOException e) {
            throw new IllegalStateException(e);
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
