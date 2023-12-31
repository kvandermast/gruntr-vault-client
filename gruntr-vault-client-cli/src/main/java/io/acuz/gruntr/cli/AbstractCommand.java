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

import io.acuz.gruntr.vault.VaultTransitRestClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static io.acuz.gruntr.Client.*;

abstract class AbstractCommand {
    protected final CliProperties cliProperties;

    protected AbstractCommand(CliProperties properties) {
        this.cliProperties = properties;
    }

    protected VaultTransitRestClient createClient(Properties properties) {
        String vaultTransitKey = properties.getProperty(GRUNTR__VAULT_TRANSIT_KEY, this.cliProperties.getHcTransitKeyName());
        String vaultHost = properties.getProperty(GRUNTR__VAULT_HOST, this.cliProperties.getHcServer().toExternalForm());
        String vaultTransitPath = properties.getProperty(GRUNTR__VAULT_TRANSIT_PATH, this.cliProperties.getHcTransitPath());

        try {
            return VaultTransitRestClient.builder()
                    .host(new URL(vaultHost))
                    .token(this.cliProperties.getHcToken())
                    .transitPath(vaultTransitPath)
                    .transitKeyName(vaultTransitKey)
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void storeProperties(final Properties encryptedProperties) throws IOException {
        if (null == cliProperties.getOutputFilePath()) {
            encryptedProperties.store(System.out, "");
        } else {
            var path = cliProperties.getOutputFilePath().toFile();

            if (!path.isFile() && !path.canWrite()) {
                System.err.println("Can't write to " + path.getAbsolutePath() + ", redirecting to stdout");
                encryptedProperties.store(System.out, "");
            } else {
                encryptedProperties.store(new FileOutputStream(path), "");
            }
        }
    }

}
