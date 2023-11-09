package io.jenkins.plugins.xygeni.saltbuildstep;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;
import io.jenkins.plugins.xygeni.util.CredentialUtil;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Salt Builder Class.
 * <p>
 * {@link #perform} will be invoke on each build
 *
 * @author Victor de la Rosa
 */
public class SaltProvenanceRecorder extends Recorder implements SimpleBuildStep {

    /** Step name at Pipeline Syntax dropdown and step command name */
    private static final String STEP_NAME = "xygeniSaltSlsa";

    /** Prefix for PEM-encoded objects */
    private static final String PEM_PREFIX = "-----BEGIN ";

    private static final Logger logger = Logger.getLogger(SaltProvenanceRecorder.class.getName());

    // fields

    private String artifactFilter;

    private String key;
    private String publicKey;
    private String certificate;
    private String keyPassword;
    private String pkiFormat;

    private boolean testingMode;
    private String output;

    private List<Subject> subjects;

    // getters/setters

    public String getArtifactFilter() {
        return this.artifactFilter;
    }

    public void setArtifactFilter(String artifactFilter) {
        this.artifactFilter = artifactFilter;
    }

    public String getKey() {
        return key;
    }

    @DataBoundSetter
    public void setKey(String key) {
        this.key = key;
    }

    public String getPublicKey() {
        return publicKey;
    }

    @DataBoundSetter
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getCertificate() {
        return certificate;
    }

    @DataBoundSetter
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    @DataBoundSetter
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getPkiFormat() {
        return pkiFormat;
    }

    @DataBoundSetter
    public void setPkiFormat(String pkiformat) {
        this.pkiFormat = pkiformat;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    @DataBoundSetter
    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public boolean getTestingMode() {
        return this.testingMode;
    }

    @DataBoundSetter
    public void setTestingMode(boolean testingMode) {
        this.testingMode = testingMode;
    }

    public String getOutput() {
        return this.output;
    }

    @DataBoundSetter
    public void setOutput(String output) {
        this.output = output;
    }

    // STATE

    public boolean isArtifactFilterOn() {
        return artifactFilter != null && !artifactFilter.isEmpty();
    }

    public boolean isOtherSubjectsOn() {
        return subjects != null && !subjects.isEmpty() && subjects.get(0).getName() != null;
    }

    // constructor
    @DataBoundConstructor
    public SaltProvenanceRecorder(String artifactFilter, List<Subject> subjects) {
        this.artifactFilter = artifactFilter;
        this.subjects = subjects;
    }

    public String getName() {
        return STEP_NAME;
    }

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull EnvVars env,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener)
            throws IOException, InterruptedException {

        PrintStream console = listener.getLogger();

        if (run.getResult() != Result.SUCCESS) {
            console.println("[xygeniSaltSlsa] - build not successful, abort generating provenance attestations");
            return;
        }

        if (subjects == null) subjects = new ArrayList<>(10);

        if (getArtifactFilter() != null) {
            addArtifactSubjects(workspace, env, listener, getArtifactFilter(), subjects);
        }

        XygeniSaltSlsaCommand.run(
                run,
                launcher,
                listener,
                getKey(),
                getKeyPassword(),
                getPublicKey(),
                getPkiFormat(),
                getCertificate(),
                subjects,
                getTestingMode(),
                getOutput());
    }

    private void addArtifactSubjects(
            FilePath workspace, EnvVars env, TaskListener listener, String artifactFilter, List<Subject> subjects)
            throws IOException, InterruptedException {

        PrintStream console = listener.getLogger();

        String expandedFilter = env.expand(artifactFilter);
        FilePath[] artifacts = workspace.list(expandedFilter);

        console.println("[xygeniSaltSlsa] collecting artifacts");

        for (FilePath artifact : artifacts) {
            console.println(" > " + artifact.getRemote());
            subjects.add(Subject.of(artifact, workspace));
        }
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link SaltProvenanceRecorder}.
     */
    @Symbol("xygeniSaltSlsa")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        // descritor

        public static DescriptorImpl get() {
            return ExtensionList.lookupSingleton(DescriptorImpl.class);
        }

        // CHECKS

        @RequirePOST
        public FormValidation doCheckKey(@QueryParameter String key) {
            if (key.isEmpty()) {
                return FormValidation.error("Please set a Key");
            }

            if (key.startsWith(PEM_PREFIX) || key.startsWith("env:") || key.startsWith("path:"))
                return FormValidation.ok(); // ok

            return FormValidation.error("Please set a valid key");
        }

        @RequirePOST
        public FormValidation doCheckPublicKey(@QueryParameter String key) {
            if (key.isEmpty()) {
                return FormValidation.error("Please set a Public Key");
            }

            if (key.startsWith(PEM_PREFIX) || key.startsWith("env:") || key.startsWith("path:"))
                return FormValidation.ok(); // ok

            return FormValidation.error("Please set a valid public key.");
        }

        @RequirePOST
        public FormValidation doCheckKeyPassword(@QueryParameter String key) {
            if (key.isEmpty()) {
                return FormValidation.error("Please set a Key Password");
            }

            return FormValidation.ok();
        }

        @RequirePOST
        public FormValidation doCheckPkiFormat(@QueryParameter String key) {
            if (key.isEmpty()) {
                return FormValidation.error("Please select a Pki Format");
            }
            return FormValidation.ok();
        }

        @RequirePOST
        public FormValidation doCheckArtifactFilter(@QueryParameter String artifactFilter) {
            if (artifactFilter.isEmpty()) {
                return FormValidation.error("Please set an Artifact Filter");
            }
            return FormValidation.ok();
        }

        /**
         * In order to load the persisted global configuration, you have to call load()
         * in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        @NonNull
        public String getDisplayName() {
            return "Building a Xygeni SLSA provenance attestation";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            logger.info("configure " + json.toString());
            save();
            return super.configure(req, json);
        }

        @Override
        public synchronized void save() {
            logger.info("save ");
            super.save();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Apply to all kinds of project types
            return true;
        }

        private boolean isValidPasswordSecret(String key) {
            if (key == null) return false;
            try {
                StringCredentials credential = CredentialUtil.getCredentialFromId(key);
                return credential != null
                        && !credential.getSecret().getPlainText().isEmpty();
            } catch (Exception e) {
                return false;
            }
        }
    }
}
