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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

abstract class AbstractPropertiesFileCommand {
    protected final CliProperties properties;

    protected AbstractPropertiesFileCommand(CliProperties properties) {
        this.properties = properties;
    }

    protected void flushProperties(Properties encryptedProperties) throws IOException {
        if (null == properties.getOutputFilePath()) {
            encryptedProperties.store(System.out, "");
        } else {
            var path = properties.getOutputFilePath().toFile();

            if (!path.isFile() && !path.canWrite()) {
                System.err.println("Can't write to " + path.getAbsolutePath() + ", redirecting to stdout");
                encryptedProperties.store(System.out, "");
            } else {
                encryptedProperties.store(new FileOutputStream(path), "");
            }
        }
    }
}
