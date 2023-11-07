package io.acuz.gruntr.cli;

import io.acuz.gruntr.util.VaultValidationUtil;
import io.acuz.gruntr.vault.model.VaultToken;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;

import static java.util.Objects.requireNonNull;

final class CliProperties {
    private final Path inputFilePath;
    private final Path outputFilePath;
    private final VaultToken hcToken;
    private final URL hcServer;
    private final String hcTransitPath;
    private final String hcTransitKeyName;


    private CliProperties(Builder builder) {
        try {
            this.hcServer = new URL(builder.hcServer);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Vault server contains an invalid url", e);
        }

        this.hcTransitPath = builder.hcTransitPath;
        this.hcTransitKeyName = builder.hcTransitKeyName;
        this.inputFilePath = builder.inputFilePath;
        this.outputFilePath = builder.outputFilePath;
        this.hcToken = builder.hcToken;
    }

    static Builder builder() {
        return new Builder();
    }

    public Path getInputFilePath() {
        return inputFilePath;
    }

    public Path getOutputFilePath() {
        return outputFilePath;
    }

    public VaultToken getHcToken() {
        return hcToken;
    }

    public URL getHcServer() {
        return hcServer;
    }

    public String getHcTransitPath() {
        return hcTransitPath;
    }

    public String getHcTransitKeyName() {
        return hcTransitKeyName;
    }


    static final class Builder {

        private ArrayDeque<String> params;
        private Path inputFilePath;
        private Path outputFilePath;
        private VaultToken hcToken;
        private String hcServer;
        private String hcTransitPath;
        private String hcTransitKeyName;

        Builder() {
            //no-op
        }

        public Builder parameters(ArrayDeque<String> params) {
            this.params = params;
            return this;
        }

        CliProperties build() {
            preValidate();

            prepare();

            postValidate();

            return new CliProperties(this);
        }

        private void preValidate() {
            requireNonNull(params);

            if (params.isEmpty()) {
                throw new IllegalStateException("Insufficient parameters provided");
            }
        }

        private void postValidate() {
            this.params = null;

            requireNonNull(this.inputFilePath, "Missing inputPath");
            requireNonNull(this.hcToken, "Missing Vault Token");

            VaultValidationUtil.checkVaultHost(this.hcServer);
            VaultValidationUtil.checkVaultPathComponent(this.hcTransitPath);
            VaultValidationUtil.checkVaultPathComponent(this.hcTransitKeyName);
        }

        private void prepare() {
            while (!this.params.isEmpty()) {
                var name = CliParameterName.get(this.params.remove());

                if (null != name) {
                    switch (name) {
                        case INPUT_FILE:
                            this.inputFilePath = Paths.get(this.params.remove());
                            break;
                        case OUTPUT_FILE:
                            this.outputFilePath = Paths.get(this.params.remove());
                            break;
                        case HC_VAULT_TOKEN:
                            this.hcToken = VaultToken.of(this.params.remove());
                            break;
                        case HC_VAULT_HOST:
                            this.hcServer = this.params.remove();
                            break;
                        case HC_VAULT_TRANSIT_PATH:
                            this.hcTransitPath = this.params.remove();
                            break;
                        case HC_VAULT_TRANSIT_KEY:
                            this.hcTransitKeyName = this.params.remove();
                            break;
                    }
                }
            }
        }
    }
}
