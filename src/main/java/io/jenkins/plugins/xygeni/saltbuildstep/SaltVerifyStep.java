package io.jenkins.plugins.xygeni.saltbuildstep;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltVerifyCommandBuilder;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Salt Verify Command Recorder Class.
 *
 * @author Victor de la Rosa
 */
public class SaltVerifyStep extends Step {

    private String output;
    private String publicKey;
    private String certificate;

    private String id;
    private String attestation;

    private List<Subject> subjects;

    private Paths paths;

    @DataBoundSetter
    public void setOutput(String output) {
        this.output = output;
    }

    @DataBoundSetter
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @DataBoundSetter
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @DataBoundSetter
    public void setId(String id) {
        this.id = id;
    }

    @DataBoundSetter
    public void setAttestation(String attestation) {
        this.attestation = attestation;
    }

    @DataBoundSetter
    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    @DataBoundSetter
    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    public String getOutput() {
        return this.output;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public String getCertificate() {
        return this.certificate;
    }

    public String getId() {
        return this.id;
    }

    public String getAttestation() {
        return this.attestation;
    }

    public List<Subject> getSubjects() {
        return this.subjects;
    }

    public Paths getPaths() {
        return paths;
    }

    @DataBoundConstructor
    public SaltVerifyStep(String output, String publicKey, String certificate, String id, String attestation) {
        this.output = output;
        this.publicKey = publicKey;
        this.certificate = certificate;
        this.id = id;
        this.attestation = attestation;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new SaltVerifyStep.Execution(output, publicKey, certificate, id, attestation, subjects, paths, context);
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        private String output;
        private String publicKey;
        private String certificate;

        private String id;
        private String attestation;

        private List<Subject> subjects;

        private Paths paths;

        public Execution(
                String output,
                String publicKey,
                String certificate,
                String id,
                String attestation,
                List<Subject> subjects,
                Paths paths,
                StepContext context) {
            super(context);
            this.output = output;
            this.publicKey = publicKey;
            this.certificate = certificate;
            this.id = id;
            this.attestation = attestation;
            this.subjects = subjects;
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

            console.println("[xygeniSalt Verify] running ..");

            new XygeniSaltVerifyCommandBuilder(output, publicKey, certificate, id, attestation, subjects)
                    .withRun(
                            getContext().get(Run.class),
                            getContext().get(Launcher.class),
                            getContext().get(TaskListener.class),
                            getContext().get(EnvVars.class))
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
            return "xygeniSaltVerify";
        }

        @Override
        public String getDisplayName() {
            return "Xygeni Salt 'Verify' command";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }
    }
}
