package io.jenkins.plugins.xygeni.saltcommand;

import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.AttestationOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.OutputOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;

public abstract class XygeniSaltCommandBuilder {

    private ArgumentListBuilder args;

    private Run<?, ?> build;
    private Launcher launcher;
    private TaskListener listener;

    /** salt command path, if null it will run as it is in the env PATH  */
    private String saltCommandPath;

    private String basedir;
    private String output;
    private boolean prettyPrint = false;
    private String outputUnsigned;
    private boolean noUpload;
    private String project;
    private boolean noResultUpload;

    /** if this is an attestation command (salt at ---) */
    protected boolean isAttestationCommand() {
        return false;
    }

    XygeniSaltCommandBuilder() {
        this.args = new ArgumentListBuilder();
    }

    public XygeniSaltCommandBuilder withRun(Run<?, ?> build, Launcher launcher, TaskListener listener) {
        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        return this;
    }

    public XygeniSaltCommandBuilder withPaths(Paths paths) {
        if (paths != null) {
            this.saltCommandPath = paths.getSaltCommandPath();
            this.basedir = paths.getBasedir();
        }
        return this;
    }

    public XygeniSaltCommandBuilder withAttestationOptions(AttestationOptions attestationOptions) {
        if (attestationOptions == null) attestationOptions = new AttestationOptions(false, null, false);
        this.noUpload = attestationOptions.getNoUpload();
        this.project = attestationOptions.getProject();
        this.noResultUpload = attestationOptions.getNoResultUpload();
        return this;
    }

    public XygeniSaltCommandBuilder withOutputOptions(OutputOptions outputOptions) {
        if (outputOptions == null) outputOptions = new OutputOptions(null, false, null);
        this.output = outputOptions.getOutput();
        this.prettyPrint = outputOptions.getPrettyPrint();
        this.outputUnsigned = outputOptions.getOutputUnsigned();
        return this;
    }

    protected abstract String getCommand();

    /**
     * Handle non-general command args to compose final command call.
     * Required args will not be checked here, it should be reported by salt command.
     * @param args
     * @param build
     */
    protected abstract void addCommandArgs(ArgumentListBuilder args, Run<?, ?> build);

    public XygeniSaltCommand build() {

        // salt command path
        if (saltCommandPath != null && !saltCommandPath.isBlank()) {
            args.add(this.saltCommandPath);
        } else {
            args.add("salt");
        }

        if (isAttestationCommand()) {
            args.add("at");
        }
        args.add("--never-fail", getCommand()); // provenance slsa attestation command
        args.add("--pipeline=" + build.getFullDisplayName());

        if (basedir != null && !basedir.isBlank()) {
            args.add("--basedir=" + this.basedir);
        } else {
            args.add("--basedir=" + build.getRootDir().getPath());
        }

        if (noUpload) {
            args.add("--no-upload");
        }
        if (project != null && !project.isBlank()) {
            args.add("--project=" + project);
        }
        if (noResultUpload) {
            args.add("--no-result-upload");
        }
        if (output != null && !output.isBlank()) {
            args.add("-o", output);
        }
        if (prettyPrint) {
            args.add("--pretty-print");
        }
        if (outputUnsigned != null && !outputUnsigned.isBlank()) {
            args.add("--output-unsigned=" + outputUnsigned);
        }

        addCommandArgs(args, build);

        XygeniSaltCommand command = new XygeniSaltCommand();
        command.setLauncher(launcher);
        command.setListener(listener);
        command.setArgs(args);
        return command;
    }
}
