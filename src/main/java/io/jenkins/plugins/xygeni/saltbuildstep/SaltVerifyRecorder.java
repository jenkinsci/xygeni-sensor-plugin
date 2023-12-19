package io.jenkins.plugins.xygeni.saltbuildstep;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltVerifyCommandBuilder;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Salt Verify Command Recorder Class.
 *
 * @author Victor de la Rosa
 */
public class SaltVerifyRecorder extends Recorder implements SimpleBuildStep {

    private static final Logger logger = Logger.getLogger(SaltVerifyRecorder.class.getName());

    private String output;
    private String publicKey;
    private String certificate;

    private String id;
    private String attestation;

    private List<Subject> subjects;

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

    @DataBoundConstructor
    public SaltVerifyRecorder(String output, String publicKey, String certificate, String id, String attestation) {
        this.output = output;
        this.publicKey = publicKey;
        this.certificate = certificate;
        this.id = id;
        this.attestation = attestation;
    }

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener) {

        PrintStream console = listener.getLogger();

        console.println("[xygeniSalt Attestation Verify] running ..");

        new XygeniSaltVerifyCommandBuilder(output, publicKey, certificate, id, attestation, subjects)
                .withRun(run, launcher, listener)
                .build()
                .run();
    }

    /**
     * Descriptor for {@link SaltVerifyRecorder}.
     */
    @Symbol("xygeniSaltVerify")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        // descritor
        public static DescriptorImpl get() {
            return ExtensionList.lookupSingleton(DescriptorImpl.class);
        }

        @NonNull
        public String getDisplayName() {
            return "Xygeni Salt Attestation 'Verify' post command";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
