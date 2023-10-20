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

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
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

    private final String COMMANDNAME = "xygeniSalt";

    private static final Logger logger = Logger.getLogger(SaltProvenanceRecorder.class.getName());

    // fields

    private String artifactFilter;

    private String key;
    private String publicKey;
    private String certificate;
    private String keyPassword;
    private String pkiFormat;

    private List<Subject> subjects;

    // state

    private boolean artifactFilterOn = false;
    private boolean otherSubjectsOn = false;

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

    // SUBJECTs STATE

    @DataBoundSetter
    public void setOnArtifactFilter(boolean filterOn) {
        this.artifactFilterOn = filterOn;
        if (!this.artifactFilterOn) {
            this.artifactFilter = null;
        }
    }

    public boolean getOnArtifactFilter() {
        return this.artifactFilterOn;
    }

    public boolean isArtifactFilterOn() {
        return this.artifactFilterOn;
    }

    @DataBoundSetter
    public void setOnOtherSubjects(boolean othersOn) {
        this.otherSubjectsOn = othersOn;
        if (!this.otherSubjectsOn) {
            this.subjects = null;
        }
    }

    public boolean getOnOtherSubjects() {
        return this.otherSubjectsOn;
    }

    public boolean isOtherSubjectsOn() {
        return this.otherSubjectsOn;
    }

    // constructor
    @DataBoundConstructor
    public SaltProvenanceRecorder(String artifactFilter, List<Subject> subjects) {
        this.artifactFilter = artifactFilter;
        this.subjects = subjects;
    }

    public String getName() {
        return COMMANDNAME;
    }

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull EnvVars env,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener) throws IOException, InterruptedException {

        PrintStream console = listener.getLogger();

        if (run.getResult() != Result.SUCCESS) {
            console.println("[xygeniSalt] - build not successful, abort generating provenance attestations");
            return;
        }

        List<Subject> subjects = new ArrayList<>();

        if (getArtifactFilter() != null) {
            addSubjects(workspace, env, listener, getArtifactFilter(), subjects);
        }

        if (getSubjects() != null) {
            subjects.addAll(getSubjects());
        }

        XygeniSaltCommand.run(
                run, launcher, listener, getKey(), getKeyPassword(), getSubjects());
    }

    private void addSubjects(FilePath workspace, EnvVars env, TaskListener listener, String artifactFilter, List<Subject> subjects) throws IOException, InterruptedException {

        PrintStream console = listener.getLogger();

        String expandedFilter = env.expand(artifactFilter);
        FilePath[] artifacts  = workspace.list(expandedFilter);

        console.println("[xygeniSalt] collecting artifacts");

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
    @Symbol("xygeniSalt")
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
            return FormValidation.ok();
        }

        @RequirePOST
        public FormValidation doCheckPublicKey(@QueryParameter String key) {
            if (key.isEmpty()) {
                return FormValidation.error("Please set a Public Key");
            }
            return FormValidation.ok();
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
            return "Xygeni Salt Attestation Provenance (SLSA) Command";
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
    }
}
