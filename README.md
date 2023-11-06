# gruntr-vault-client

## Goal

The idea behind the Gruntr Vault Client is based on the Mozilla SOPS file encryption, but with that difference that it solely relies on the Vault transit engine for

- encryption of the property values
- decryption of the property values 
- rewrapping of encrypted values on key rotation
- facilitator to encrypt and decrypt values on the fly

It is available as a CLI and a Client that you can use in Java web applications

## Building the dependencies

The project uses gradle to build the artifacts:

```shell
$ ./gradlew clean jar

BUILD SUCCESSFUL in 1s
9 actionable tasks: 6 executed, 3 up-to-date

$ find . -name "*gruntr*.jar"
./gruntr-vault-client-api/build/libs/gruntr-vault-client-api-0.0.1-SNAPSHOT.jar
./gruntr-vault-client-cli/build/libs/gruntr-vault-client-cli-0.0.1-SNAPSHOT.jar
./gruntr-vault-client/build/libs/gruntr-vault-client-0.0.1-SNAPSHOT.jar
```

## CLI

The CLI is a "fat" or "uber"-jar that contains all the required dependencies, which can be run as a Java executable jar.

### Encryption

Give the following unencrypted properties file:

```properties
my.secret=something very secret
yet.another.secret=more secret even
```

When running the main class you pass the following parameters:

```shell
java -jar ./gruntr-vault-client-cli.jar \ 
        encrypt \                                  # use the "encrypt" command
        -i ./application.env \                     # the "original" properties files
        --token TOKEN_YOUR_RECEIVED \              # your vault token
        --hc-vault-server http://vault:8201 \      # point to your Vault Server
        --hc-transit-path transit/project_name \   # this is the mounted transit engine
        --hc-transit-key appkey                    # the key that you created
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
java -jar ./gruntr-vault-client-cli.jar \ 
        decrypt \                                 # use the "decrypt" command 
        -i ./application-encrypted.properties \   # the encrypted properties file
        --token TOKEN_YOUR_RECEIVED \             # your vault token
        --hc-vault-server http://vault:8201 \     # point to your Vault Server
        --hc-transit-path transit/project_name \  # this is the mounted transit engine
        --hc-transit-key appkey                   # the key that you created
```

This will decrypt the file against the defined Transit engine to

```properties
#
#Thu Nov 02 17:02:27 CET 2023
yet.another.secret=more secret even
my.secret=something very secret
```

### Re-wrapping

Re-wrapping means that you take a file with encrypted values and re-encrypt them via Vault. 
This must happen when the key used to encrypt the first batch of secrets is rotated (and thus becomes a new version).

Imagine that you rotate the key in Vault:

```shell
$ curl \
   --header "X-Vault-Token: ....." \
   --request "POST" \
   $VAULT_ADDR/v1/transit/project_name/keys/appkey/rotate
```

This would have rotated the transit engine mounted at `transit/project_name` for `appkey` to the next version (e.g. from v1 to v2).

Give the following encrypted properties file:

```properties
#
#Thu Nov 02 16:25:32 CET 2023
my.secret=vault\:v1\:628GeWoFhb+5xOx2RDYuNK6BEaxRopnYJo+qkfiHUmK60giGXxmRo59mpd7F9cjSvg\=\=
yet.another.secret=vault\:v1\:C3JPohIciC1fZ9tjXLBkMH8JgH++z07WiFDGBP/Rj548es0cc6Lnt46kz70\=
gruntr__vault_transit_path=transit/project_name
gruntr__vault_transit_key=appkey
gruntr__vault_host=http\://vault\:8201
```

Give the example above, you run the CLI with the following parameters:

```bash
java -jar ./gruntr-vault-client-cli.jar \ 
        rewrap \                                  # use the "rewrap" command 
        -i ./application-encrypted.properties \   # the encrypted properties file
        --token TOKEN_YOUR_RECEIVED \             # your vault token
        --hc-vault-server http://vault:8201 \     # point to your Vault Server
        --hc-transit-path transit/project_name \  # this is the mounted transit engine
        --hc-transit-key appkey                   # the key that you created
```

Would upgrade the version of the encrypted values:

```properties
#
#Mon Nov 06 15:01:52 CET 2023
yet.another.secret=vault\:v2\:CtCylZ9QYnLioefOxPeytEyE5obOjFLlYWhH6FY/kIw/dODgr6G3WZjD6LU\=
another.one=vault\:v2\:rk4/aVXoC5BPzKjJ+5rH17/IgzzWsXm5Y1rF6TjYvo9JayL0P/OR8g6R4fSqMOP11Q\=\=
my.secret=vault\:v2\:mLVx9xS2vWyKoN0lnAbSoakaLQk3ZsuXaqkM3EmSOxHAsms1dsz5wR31XX2z4qUhew\=\=
gruntr__vault_transit_path=transit/project_name
gruntr__vault_transit_key=appkey
gruntr__vault_host=http\://vault\:8201
```

## Using the client

You can also use the Gruntr library in a web application, e.g.

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
I just liked the idea that in a DevOps environment we remove the heavy lifting of "securing your sensitive information" away from the developers and into a tool that is made for it.
Hashicorp Vault offers a large toolset of services, with the transit one being one of the most versatile ones around.