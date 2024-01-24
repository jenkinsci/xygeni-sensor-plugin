package io.jenkins.plugins.xygeni.saltcommand;

import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;
import java.util.List;

public class XygeniSaltAtSlsaCommandBuilder extends XygeniSaltCommandBuilder {

    private static final String SLSA_COMMAND = "slsa";

    private String key;
    private String keyPassword;
    private String publicKey;
    private String pkiFormat;
    private String certificate;

    private boolean keyless = false;
    private final List<Subject> subjects;

    public XygeniSaltAtSlsaCommandBuilder(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public XygeniSaltAtSlsaCommandBuilder withKey(
            String key, String keyPassword, String publicKey, String pkiFormat, String certificate) {
        this.key = key;
        this.keyPassword = keyPassword;
        this.publicKey = publicKey;
        this.pkiFormat = pkiFormat;
        this.certificate = certificate;
        return this;
    }

    public XygeniSaltAtSlsaCommandBuilder withKeyless() {
        this.keyless = true;
        return this;
    }

    @Override
    protected String getCommand() {
        return SLSA_COMMAND;
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

        subjects.forEach(subject -> {
            args.add("-n", subject.getName());
            if (subject.isValue()) {
                args.add("-v", subject.getValue());
            } else if (subject.isFile()) {
                args.add("-f", subject.getFile());
            } else {
                args.add("-i", subject.getImage());
            }
            if (subject.getDigest() != null && !subject.getDigest().isBlank()) {
                args.add("--digest=" + subject.getDigest());
            }
        });
    }
}
