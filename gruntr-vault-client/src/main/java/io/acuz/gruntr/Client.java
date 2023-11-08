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

package io.acuz.gruntr;

import java.util.Properties;

public interface Client {
    String GRUNTR__VAULT_TRANSIT_KEY = "gruntr__vault_transit_key";
    String GRUNTR__VAULT_HOST = "gruntr__vault_host";
    String GRUNTR__VAULT_TRANSIT_PATH = "gruntr__vault_transit_path";
    String GRUNTR__SHA_3 = "gruntr__sha3";

    Properties decryptProperties();

    Properties getEncryptedProperties();
}
