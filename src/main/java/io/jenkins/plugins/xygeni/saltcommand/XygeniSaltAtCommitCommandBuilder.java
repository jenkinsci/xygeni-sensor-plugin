package io.jenkins.plugins.xygeni.saltcommand;

import hudson.model.Run;
import hudson.util.ArgumentListBuilder;

public class XygeniSaltAtCommitCommandBuilder extends XygeniSaltCommandBuilder {

    private static final String COMMIT_COMMAND = "commit";

    private final String key;
    private final String keyPassword;
    private final String publicKey;
    private final String pkiFormat;
    private final String certificate;

    private final boolean keyless;

    public XygeniSaltAtCommitCommandBuilder(
            String key, String keyPassword, String publicKey, String pkiFormat, String certificate, boolean keyless) {
        this.key = key;
        this.keyPassword = keyPassword;
        this.publicKey = publicKey;
        this.pkiFormat = pkiFormat;
        this.certificate = certificate;
        this.keyless = keyless;
    }

    @Override
    protected String getCommand() {
        return COMMIT_COMMAND;
    }

    @Override
    protected boolean isAttestationCommand() {
        return true;
    }

    @Override
    protected void addCommandArgs(ArgumentListBuilder args, Run<?, ?> build) {

        if (keyless) {
            args.add("--keyless");
        } else {
            args.add("-k", key);
            args.add("--key-password=" + keyPassword);
            args.add("--public-key=" + publicKey);
            args.add("--pki-format=" + pkiFormat);
            if (certificate != null && !certificate.isEmpty()) {
                args.add("--certificate=" + certificate);
            }
        }
    }
}
