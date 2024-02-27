package io.jenkins.plugins.xygeni.saltbuildstep;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.xygeni.saltbuildstep.model.AttestationOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Certs;
import io.jenkins.plugins.xygeni.saltbuildstep.model.OutputOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtCommitCommandBuilder;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Salt Attestation Commit Command Builder Class.
 *
 * @author Victor de la Rosa
 */
public class SaltAtCommitStep extends Step {

    private Certs certs;

    private AttestationOptions attestationOptions;

    private OutputOptions outputOptions;

    private Paths paths;

    @DataBoundSetter
    public void setAttestationOptions(AttestationOptions attestationOptions) {
        this.attestationOptions = attestationOptions;
    }

    @DataBoundSetter
    public void setCerts(Certs certs) {
        this.certs = certs;
    }

    @DataBoundSetter
    public void setOutputOptions(OutputOptions outputOptions) {
        this.outputOptions = outputOptions;
    }

    @DataBoundSetter
    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    public AttestationOptions getAttestationOptions() {
        return this.attestationOptions;
    }

    public Certs getCerts() {
        return this.certs;
    }

    public OutputOptions getOutputOptions() {
        return this.outputOptions;
    }

    public Paths getPaths() {
        return paths;
    }

    /**
     * Instance a Salt Attestation Commit command.
     * Required args will not be checked here, it should be reported by salt command.
     * If not options are informed, a default configuration is provided.
     * @param certs certificates option
     * @param attestationOptions attestation option
     * @param outputOptions output options
     */
    @DataBoundConstructor
    public SaltAtCommitStep(
            Certs certs, AttestationOptions attestationOptions, OutputOptions outputOptions, Paths paths) {
        this.certs = certs;
        this.attestationOptions = attestationOptions;
        this.outputOptions = outputOptions;
        this.paths = paths;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new SaltAtCommitStep.Execution(certs, attestationOptions, outputOptions, paths, context);
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        private Certs certs;

        private AttestationOptions attestationOptions;

        private OutputOptions outputOptions;

        private Paths paths;

        public Execution(
                Certs certs,
                AttestationOptions attestationOptions,
                OutputOptions outputOptions,
                Paths paths,
                StepContext context) {
            super(context);
            this.certs = certs;
            this.attestationOptions = attestationOptions;
            this.outputOptions = outputOptions;
            this.paths = paths;
        }

        @Override
        protected Void run() throws Exception {

            if (getContext().get(Launcher.class) == null)
                throw new Exception("Not Launcher, probably not in a step run");
            if (getContext().get(EnvVars.class) == null) throw new Exception("Not EnvVars, probably not in a step run");
            if (getContext().get(TaskListener.class) == null)
                throw new Exception("Not TaskListener, probably not in a step run");
            PrintStream console = getContext().get(TaskListener.class).getLogger();

            console.println("[xygeniSalt Attestation Commit] running ..");

            if (this.attestationOptions == null) attestationOptions = new AttestationOptions(false, null, false);
            if (this.outputOptions == null) outputOptions = new OutputOptions(null, false, null);

            if (certs == null) certs = new Certs(null, null, null, null, null, false);

            new XygeniSaltAtCommitCommandBuilder(
                            certs.getKey(),
                            certs.getKeyPassword(),
                            certs.getPublicKey(),
                            certs.getPkiFormat(),
                            certs.getCertificate(),
                            certs.getKeyless())
                    .withRun(
                            getContext().get(Run.class),
                            getContext().get(Launcher.class),
                            getContext().get(TaskListener.class),
                            getContext().get(EnvVars.class))
                    .withAttestationOptions(attestationOptions)
                    .withOutputOptions(outputOptions)
                    .withPaths(paths)
                    .build()
                    .run();

            return null;
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "xygeniSaltAtCommit";
        }

        @Override
        public String getDisplayName() {
            return "Xygeni Salt Attestation 'Commit' command";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }
    }
}
