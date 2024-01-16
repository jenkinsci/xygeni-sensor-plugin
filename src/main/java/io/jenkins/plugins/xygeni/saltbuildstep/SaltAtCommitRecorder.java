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
import io.jenkins.plugins.xygeni.saltbuildstep.model.AttestationOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Certs;
import io.jenkins.plugins.xygeni.saltbuildstep.model.OutputOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtCommitCommandBuilder;
import java.io.PrintStream;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Salt Attestation Commit Command Builder Class.
 *
 * @author Victor de la Rosa
 */
public class SaltAtCommitRecorder extends Recorder implements SimpleBuildStep {

    private static final Logger logger = Logger.getLogger(SaltAtCommitRecorder.class.getName());

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
     * @param certs
     * @param attestationOptions
     * @param outputOptions
     */
    @DataBoundConstructor
    public SaltAtCommitRecorder(Certs certs, AttestationOptions attestationOptions, OutputOptions outputOptions) {
        this.certs = certs;
        this.attestationOptions = attestationOptions;
        this.outputOptions = outputOptions;
    }

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener) {

        PrintStream console = listener.getLogger();

        console.println("[xygeniSalt Attestation Commit] running ..");

        // default configuration
        if (this.attestationOptions == null) attestationOptions = new AttestationOptions(false, null, false);
        if (this.outputOptions == null) outputOptions = new OutputOptions(null, false, null);

        new XygeniSaltAtCommitCommandBuilder(
                        certs.getKey(),
                        certs.getKeyPassword(),
                        certs.getPublicKey(),
                        certs.getPkiFormat(),
                        certs.getCertificate(),
                        certs.getKeyless())
                .withRun(run, launcher, listener)
                .withAttestationOptions(attestationOptions)
                .withOutputOptions(outputOptions)
                .withPaths(paths)
                .build()
                .run();
    }

    /**
     * Descriptor for {@link SaltAtCommitRecorder}.
     */
    @Symbol("xygeniSaltAtCommit")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        // descritor

        public static DescriptorImpl get() {
            return ExtensionList.lookupSingleton(DescriptorImpl.class);
        }

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            logger.info("configure " + json.toString());

            return super.configure(req, json);
        }

        @NonNull
        public String getDisplayName() {
            return "Xygeni Salt Attestation 'Commit' post command";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
