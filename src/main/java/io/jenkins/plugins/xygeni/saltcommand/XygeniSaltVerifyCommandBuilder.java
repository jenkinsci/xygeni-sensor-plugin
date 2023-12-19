package io.jenkins.plugins.xygeni.saltcommand;

import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;
import java.util.List;

public class XygeniSaltVerifyCommandBuilder extends XygeniSaltAtCommandBuilder {

    private static final String VERIFY_COMMAND = "verify";

    private String output;
    private String publicKey;
    private String certificate;
    private String id;
    private String attestation;

    private List<Subject> subjects;

    public XygeniSaltVerifyCommandBuilder(
            String output,
            String publicKey,
            String certificate,
            String id,
            String attestation,
            List<Subject> subjects) {
        this.output = output;
        this.publicKey = publicKey;
        this.certificate = certificate;
        this.id = id;
        this.attestation = attestation;
        this.subjects = subjects;
    }

    @Override
    protected String getCommand() {
        return VERIFY_COMMAND;
    }

    @Override
    protected void addCommandArgs(ArgumentListBuilder args, Run<?, ?> build) {

        if (output != null && !output.isBlank()) {
            args.add("-o", output);
        }
        if (publicKey != null && !publicKey.isBlank()) {
            args.add("-k", publicKey);
        }
        if (certificate != null && !certificate.isBlank()) {
            args.add("--certificate=" + certificate);
        }
        if (id != null && !id.isBlank()) {
            args.add("--id=" + id);
        }
        if (attestation != null && !attestation.isBlank()) {
            args.add("--attestation=" + attestation);
        }

        for (Subject subject : subjects) {
            if (subject.getName() != null) {
                args.add("-n", subject.getName());
            }
            if (subject.isValue()) {
                args.add("-v", subject.getValue());
            } else if (subject.isFile()) {
                args.add("-f", subject.getFile());
            } else if (subject.isDigest()) {
                args.add("--digest=" + subject.getDigest());
            } else {
                args.add("-i", subject.getImage());
            }
        }
    }
}
