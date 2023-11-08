# gruntr-vault-client

- [Goal of Gruntr](#goal)
- [Building the artifact](#building-the-artifact)
- [About the CLI](#cli)
- [Using the Client](#using-the-client)
- [Running the Vault docker image](#running-the-vault-docker-image)
- [Why 'Gruntr'](#why-gruntr)
- [License](LICENSE.md)


## Goal

The idea behind the Gruntr Vault Client is based on the Mozilla SOPS file encryption, but with that difference that it solely relies on the Vault transit engine for

- encryption of the property values
- decryption of the property values 
- re-wrapping of encrypted values on key rotation
- facilitator to encrypt and decrypt values on the fly

It is available as a CLI and a Client that you can use in Java web applications

## Building the artifact

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
#Wed Nov 08 20:25:09 CET 2023
yet.another.secret=vault\:v1\:b14ofszZ1QMD43W9++r8D+ngOuHjkmJfObIBjXeLply3rCnm0KDdK5QUTts\=
just.plaintext=this is just plaintext
my.secret=vault\:v1\:Qq8SSW0iOt2JG6+fpKOwD4Xeld1PaR7IwesGyTi0qkefnqX84QL4TJbapIDcwsEE4A\=\=
gruntr__vault_transit_path=transit/project_name
gruntr__vault_transit_key=appkey
gruntr__vault_host=http\://vault\:8201
gruntr__sha3=vault\:v1\:x/zxPO5Pbazxpr4LfdarLD/JX6KPMv2Lr3uzHmOibZh/VW/qKyUhhisSYGOAR4MIv4QyuvbbHLx2+YTL12JJK2KkcUsmBzjVFHjznW8VjIZ/4uvQDwqvrOKfl/w\=
```

The file contains correctly encoded values for a properties file.

By default, ALL keys in the result are encrypted. You can tailor the behaviour by adding the `--keys` or `-k` parameter.
- if you pass `-k :secrets`, it will only encrypt keys that have `token`, `password` or `secret` in their key name,
- if you pass `-k :secrets,username`, it will encrypt the keys as mentioned above, but also the fields containing username.
- if you pass `-k :secrets,(username|userid)`, it will act as the previous one, and also check for userid next to username.

You can add whatever configuration you desire.

> Note that `:secrets` is a group, ergo it starts with the colon (':'). Your own keys (such as username for example) are to be passed as is.

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
#Wed Nov 08 20:25:28 CET 2023
just.plaintext=this is just plaintext
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
#Wed Nov 08 20:25:39 CET 2023
yet.another.secret=vault\:v1\:cSYGP5EaLQoGScGlwWRxuQYoXA59jSp8dnGxizy8nRluSd9jQca0ZhpvvRw\=
just.plaintext=this is just plaintext
my.secret=vault\:v1\:zVZiN399LBQwbulteZ9Uu0O9g+GHuE7VjostpBnD2pC0jwSuiwtQx1DSUvdZTpfGRA\=\=
gruntr__vault_transit_path=transit/project_name
gruntr__vault_transit_key=appkey
gruntr__vault_host=http\://vault\:8201
gruntr__sha3=vault\:v1\:CkaExD0+Cph+BmByIh440bXYKrXUua4tGT2X4r2viUiJNCQvnfxQWI1NHO9i0A7lYdGgg9Dc0TmsbaAhKJ2U1mW/N87B/838DD3MTUB+qigRJw+XeBhslOY+MUU\=
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
#Wed Nov 08 20:37:41 CET 2023
yet.another.secret=vault\:v2\:TxvjKocwGbNHixxyjqOALfmreVwgE50X5cpmtaCtzZeOumiEl8I+fPA+fUw\=
just.plaintext=this is just plaintext
my.secret=vault\:v2\:/Zb9e6ty/e2sqWOlEHo210nl4BW5gUCgS32Hm2STllzFYZMjp+4pmRLN18qURBdL/A\=\=
gruntr__vault_transit_path=transit/project_name
gruntr__vault_transit_key=appkey
gruntr__vault_host=http\://vault\:8201
gruntr__sha3=vault\:v2\:Be6v4lO6remiLkYSIKuhR0QZv3PrMmFInB6fnM2/sdJA7soyfkLA2lXVW8jQUxBbwH1kwtmZZsXDmF0G42BEutaq3JVymdnuRZeWiHFzbl+INQymQ02af1PJMuE\=
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
                .setToken(VaultToken.of("TOKEN"))
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


# Running the Vault docker image

In the folder `vault-mockup-docker-compose`, you can find a docker-compose definition that you can start to have a local Vault running.

> It is exposed over port `8201` instead of the classic `8200` port;

There is also a `start.sh` script in the `scripts` folder, this creates a transit engine with a different algorithm.
