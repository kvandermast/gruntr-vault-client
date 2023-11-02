package io.acuz.gruntr.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Objects;

public final class CliProperties {
    private final Path inputFilePath;
    private final Path outputFilePath;
    private final String hcToken;
    private final String hcServer;
    private final String hcTransitPath;


    private CliProperties(Builder builder) {
        this.hcServer = builder.hcServer;
        this.hcTransitPath = builder.hcTransitPath;
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

    static final class Builder {

        private ArrayDeque<String> params;
        private Path inputFilePath;
        private Path outputFilePath;
        private String hcToken;
        private String hcServer;
        private String hcTransitPath;

        CliProperties build() {
            preValidate();

            prepare();

            postValidate();

            return new CliProperties(this);
        }

        private void preValidate() {
            Objects.requireNonNull(params);

            if (params.isEmpty() || params.size() < 2) {
                throw new IllegalStateException("Insufficient parameters provided");
            }
        }

        private void postValidate() {
            this.params = null;

            Objects.requireNonNull(this.inputFilePath);
            Objects.requireNonNull(this.hcToken);
            Objects.requireNonNull(this.hcServer);
            Objects.requireNonNull(this.hcTransitPath);
        }

        private void prepare() {
            while (!this.params.isEmpty()) {
                var next = this.params.remove();

                if ("--input".equalsIgnoreCase(next) || "-i".equalsIgnoreCase(next)) {
                    var path = this.params.remove();

                    this.inputFilePath = Paths.get(path);
                } else if ("--output".equalsIgnoreCase(next) || "-o".equalsIgnoreCase(next)) {
                    var path = this.params.remove();

                    this.outputFilePath = Paths.get(path);
                } else if ("--hc-token".equalsIgnoreCase(next) || "--token".equalsIgnoreCase(next) || "-t".equalsIgnoreCase(next)) {
                    this.hcToken = this.params.remove();
                } else if ("--hc-vault-server".equalsIgnoreCase(next)) {
                    this.hcServer = this.params.remove();
                } else if ("--hc-transit-path".equalsIgnoreCase(next)) {
                    this.hcTransitPath = this.params.remove();
                }
            }
        }

        public Builder parameters(ArrayDeque<String> params) {
            this.params = params;
            return this;
        }
    }
}