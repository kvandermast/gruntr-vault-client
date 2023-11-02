# gruntr-vault-client

## Goal

The idea behind the Gruntr Vault Client is based on the Mozilla SOPS file encryption, but with that difference that it solely relies on the Vault transit engine for

- encryption of the property values
- decryption of the property values
- available as a CLI and a Client that you can use in Java web applications

## CLI

### Encryption

Give the following unencrypted properties file:

```properties
my.secret=something very secret
yet.another.secret=more secret even
```

When running the main class you pass the following parameters:

```shell
encrypt -i ./application.env \
        --token TOKEN_YOUR_RECEIVED \
        --hc-vault-server http://vault:8201 \    # point to your Vault Server
        --hc-transit-path transit/project_name \ # this is the mounted transit engine
        --hc-transit-key appkey                  # the key that you created
```
This generates an output to stdout, alike:

```properties
#
#Thu Nov 02 16:25:32 CET 2023
my.secret=vault\:v1\:628GeWoFhb+5xOx2RDYuNK6BEaxRopnYJo+qkfiHUmK60giGXxmRo59mpd7F9cjSvg\=\=
yet.another.secret=vault\:v1\:C3JPohIciC1fZ9tjXLBkMH8JgH++z07WiFDGBP/Rj548es0cc6Lnt46kz70\=
gruntr__vault_transit_path=transit/project_name
gruntr__vault_transit_key=appkey
gruntr__vault_host=http\://vault\:8201
```

The file contains correctly encoded values for a properties file.

### Decryption

Give the example above, you run the CLI with the following parameters:

```bash
decrypt -i ./application-encrypted.properties \ 
        --token TOKEN_YOUR_RECEIVED \
        --hc-vault-server http://vault:8201 \    # point to your Vault Server
        --hc-transit-path transit/project_name \ # this is the mounted transit engine
        --hc-transit-key appkey                  # the key that you created
```

This will decrypt the file against the defined Transit engine to

```properties
#
#Thu Nov 02 17:02:27 CET 2023
yet.another.secret=more secret even
my.secret=something very secret
```

## Using the client

You can also use the Gruntr library in a web application, eg

```java
package io.acuz.gruntr;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class ClientTest {
    // ...
    @Test
    void test_createANewClientAndDecrypt() {
        var client = Client.builder()
                .setPath(Path.of("/to/your/path", "application-encrypted.properties"))
                .build();

        var properties = client.getDecryptedProperties();
        assertEquals(2, properties.size());
    }
}
```

## Why 'Gruntr'

To grunt can either stand for "make a low, short guttural sound", like a pig, or for someone that used to be unskilled in a certain profession (to grunt yourself up the ladder).
I just liked the idea that in a DevOps environment wee remove the heavy lifting of "securing your sensitive information" away from the developers and into a tool that is made for it.
Hashicorp Vault offers a large toolset of services, with the transit one being one of the most versatile ones around.