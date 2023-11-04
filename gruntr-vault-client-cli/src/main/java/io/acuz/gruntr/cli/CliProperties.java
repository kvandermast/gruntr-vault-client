package io.acuz.gruntr.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;

import static java.util.Objects.requireNonNull;

final class CliProperties {
    private final Path inputFilePath;
    private final Path outputFilePath;
    private final String hcToken;
    private final String hcServer;
    private final String hcTransitPath;
    private final String hcTransitKeyName;


    private CliProperties(Builder builder) {
        this.hcServer = builder.hcServer;
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

    public String getHcToken() {
        return hcToken;
    }

    public String getHcServer() {
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
        private String hcToken;
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

            if (params.isEmpty() || params.size() < 2) {
                throw new IllegalStateException("Insufficient parameters provided");
            }
        }

        private void postValidate() {
            this.params = null;

            requireNonNull(this.inputFilePath);
            requireNonNull(this.hcToken);
            requireNonNull(this.hcServer);
            requireNonNull(this.hcTransitPath);
            requireNonNull(this.hcTransitKeyName);
        }

        private void prepare() {
            while (!this.params.isEmpty()) {
                switch (CliParameterName.get(this.params.remove())) {
                    case INPUT_FILE:
                        this.inputFilePath = Paths.get(this.params.remove());
                        break;
                    case OUTPUT_FILE:
                        this.outputFilePath = Paths.get(this.params.remove());
                        break;
                    case HC_VAULT_TOKEN:
                        this.hcToken = this.params.remove();
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
