package io.jenkins.plugins.xygeni.saltcommand;

import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;
import java.util.List;

public class XygeniSaltAtSlsaCommandBuilder extends XygeniSaltCommandBuilder {

    private static final String SLSA_COMMAND = "slsa";

    private final String key;
    private final String keyPassword;
    private final String publicKey;
    private final String pkiFormat;
    private final String certificate;
    private final List<Subject> subjects;

    public XygeniSaltAtSlsaCommandBuilder(
            String key,
            String keyPassword,
            String publicKey,
            String pkiFormat,
            String certificate,
            List<Subject> subjects) {
        this.key = key;
        this.keyPassword = keyPassword;
        this.publicKey = publicKey;
        this.pkiFormat = pkiFormat;
        this.certificate = certificate;
        this.subjects = subjects;
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

        args.add("-k", key);
        args.add("--key-password=" + keyPassword);
        args.add("--public-key=" + publicKey);
        args.add("--pki-format=" + pkiFormat);
        if (certificate != null && !certificate.isEmpty()) {
            args.add("--certificate=" + certificate);
        }

        subjects.forEach(subject -> {
            if (subject.isValue()) {
                args.add("-n", subject.getName());
                args.add("-v", subject.getValue());
            } else if (subject.isFile()) {
                args.add("-n", subject.getName());
                args.add("-f", subject.getFile());
            } else if (subject.isDigest()) {
                args.add("-n", subject.getName());
                args.add("--digest=" + subject.getDigest());
            } else {
                args.add("-n", subject.getName());
                args.add("-i", subject.getImage());
            }
        });
    }
}
