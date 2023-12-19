package io.jenkins.plugins.xygeni.saltcommand;

import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.AttestationOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.OutputOptions;

public abstract class XygeniSaltAtCommandBuilder {

    private ArgumentListBuilder args;

    private Run<?, ?> build;
    private Launcher launcher;
    private TaskListener listener;
    private String output;
    private boolean prettyPrint = false;
    private String outputUnsigned;
    private boolean noUpload;
    private String project;
    private boolean noResultUpload;

    XygeniSaltAtCommandBuilder() {
        this.args = new ArgumentListBuilder();
    }

    public XygeniSaltAtCommandBuilder withRun(Run<?, ?> build, Launcher launcher, TaskListener listener) {
        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        return this;
    }

    public XygeniSaltAtCommandBuilder withAttestationOptions(AttestationOptions attestationOptions) {
        if (attestationOptions == null) attestationOptions = new AttestationOptions(false, null, false);
        this.noUpload = attestationOptions.getNoUpload();
        this.project = attestationOptions.getProject();
        this.noResultUpload = attestationOptions.getNoResultUpload();
        return this;
    }

    public XygeniSaltAtCommandBuilder withOutputOptions(OutputOptions outputOptions) {
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

    public XygeniSaltAtCommand build() {

        args.add("salt", "at", "--never-fail", getCommand()); // provenance slsa attestation command
        args.add("--pipeline=" + build.getFullDisplayName());
        args.add("--basedir=" + build.getRootDir().getPath());

        if (noUpload) {
            args.add("--no-upload");
        }
        if (project != null && !project.isEmpty()) {
            args.add("--project=" + project);
        }
        if (noResultUpload) {
            args.add("--no-result-upload");
        }
        if (output != null && !output.isEmpty()) {
            args.add("-o", output);
        }
        if (prettyPrint) {
            args.add("--pretty-print");
        }
        if (outputUnsigned != null && !outputUnsigned.isEmpty()) {
            args.add("--output-unsigned=" + outputUnsigned);
        }

        addCommandArgs(args, build);

        XygeniSaltAtCommand command = new XygeniSaltAtCommand();
        command.setBuild(build);
        command.setLauncher(launcher);
        command.setListener(listener);
        command.setArgs(args);
        return command;
    }
}
