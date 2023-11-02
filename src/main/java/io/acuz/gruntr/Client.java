package io.acuz.gruntr;

import io.acuz.gruntr.vault.VaultRestClient;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

public final class Client {
    private final Path path;

    private final Properties encryptedProperties;

    public Client(Builder builder) {
        this.path = builder.path;
        this.encryptedProperties = readEncryptedProperties(this.path);
    }

    public static Builder builder() {
        return new Builder();
    }

    private Properties readEncryptedProperties(Path path) {
        var properties = new Properties();
        try (var inputStream = new FileInputStream(path.toFile())) {
            properties.load(inputStream);

            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getPath() {
        return path;
    }

    public Properties getDecryptedProperties() {
        var vault = VaultRestClient.builder().build();
        var masterKey = vault.decrypt(this.encryptedProperties.getProperty("sops_hc_vault__list_0__map_enc"));
        var properties = new Properties();

        if (masterKey.length > 0) {
            var encryptedProperties = this.getEncryptedProperties();

            encryptedProperties.forEach((key, val) -> {
                if (val instanceof String) {
                    var stringValue = ((String) val).trim();

                    if (stringValue.startsWith("ENC[") && stringValue.endsWith("]")) {
                        var x = EncryptedProperty.of(stringValue);

                        System.out.println("---->> " + x.decrypt(masterKey));
                    } else {
                        // non-encrypted value
                        properties.put(key, val);
                    }
                }
            });
        }

        Arrays.fill(masterKey, (byte) 0);

        return properties;
    }

    public Properties getEncryptedProperties() {
        var properties = (Properties) this.encryptedProperties.clone();

        final var keys = properties.keySet();

        for (Object key : keys) {
            if (key instanceof String && ((String) key).startsWith("sops_"))
                properties.remove(key);
        }

        return properties;
    }

    public static final class Builder {
        private Path path;

        public Builder setPath(Path path) {
            this.path = path;

            return this;
        }

        public Client build() {
            validate();
            return new Client(this);
        }

        private void validate() {
            Objects.requireNonNull(path);
        }
    }

    private static final class EncryptedProperty {
        private final byte[] data;
        private final byte[] iv;
        private final byte[] tag;
        private final String type;

        private EncryptedProperty(String data, String iv, String tag, String type) {
            this.data = Base64.getDecoder().decode(data);
            this.iv = Base64.getDecoder().decode(iv);
            this.tag = Base64.getDecoder().decode(tag);
            this.type = type;
        }

        static EncryptedProperty of(String value) {
            var pattern  = Pattern.compile("ENC\\[AES256_GCM,data:(.+),iv:(.+),tag:(.+),type:(.+)\\]");
            var matcher = pattern.matcher(value);

            if(matcher.matches())  {
                if(matcher.groupCount() == 4) {
                    return new EncryptedProperty(
                            matcher.group(1), // data
                            matcher.group(2), // iv
                            matcher.group(3), // tag
                            matcher.group(4) // type
                    );
                }
            }

            throw new IllegalStateException("Value of encrypted property not correctly structured");
        }

        public String decrypt(byte[] secret) {
            try {
                var combinedData = new byte[data.length + this.tag.length];
                System.arraycopy(this.data, 0, combinedData, 0, this.data.length);
                System.arraycopy(this.tag, 0, combinedData, this.data.length, this.tag.length);

                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(128, iv));
                cipher.updateAAD(new byte[]{});
                var plaintext = cipher.doFinal(combinedData);

                System.out.println(new String(plaintext));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }

            return null;
        }
    }
}
