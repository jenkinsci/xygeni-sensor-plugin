package io.jenkins.plugins.xygeni.saltbuildstep;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.xygeni.saltbuildstep.model.*;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtSlsaCommandBuilder;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Salt Provenance Recorder Class.
 *
 * @author Victor de la Rosa
 */
public class SaltProvenanceStep extends Step {

    /** Step name at Pipeline Syntax dropdown and step command name */
    private static final String STEP_NAME = "xygeniSalt-Slsa";

    /** Prefix for PEM-encoded objects */
    private static final String PEM_PREFIX = "-----BEGIN ";

    private static final Logger logger = Logger.getLogger(SaltProvenanceStep.class.getName());

    // fields

    private String artifactFilter;
    private List<Subject> subjects;
    private Certs certs;
    private AttestationOptions attestationOptions;
    private OutputOptions outputOptions;
    private Paths paths;

    // getters/setters

    public String getArtifactFilter() {
        return this.artifactFilter;
    }

    public void setArtifactFilter(String artifactFilter) {
        this.artifactFilter = artifactFilter;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    @DataBoundSetter
    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    @DataBoundSetter
    public void setAttestationOptions(AttestationOptions attestationOptions) {
        this.attestationOptions = attestationOptions;
    }

    @DataBoundSetter
    public void setOutputOptions(OutputOptions outputOptions) {
        this.outputOptions = outputOptions;
    }

    @DataBoundSetter
    public void setCerts(Certs certs) {
        this.certs = certs;
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

    // STATE

    public boolean isArtifactFilterOn() {
        return artifactFilter != null && !artifactFilter.isEmpty();
    }

    public boolean isOtherSubjectsOn() {
        return subjects != null && !subjects.isEmpty() && subjects.get(0).getName() != null;
    }

    // constructor
    @DataBoundConstructor
    public SaltProvenanceStep(String artifactFilter, List<Subject> subjects) {
        this.artifactFilter = artifactFilter;
        this.subjects = subjects;
    }

    public String getName() {
        return STEP_NAME;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new SaltProvenanceStep.Execution(
                artifactFilter, subjects, certs, attestationOptions, outputOptions, paths, context);
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        private String artifactFilter;
        private List<Subject> subjects;
        private Certs certs;
        private AttestationOptions attestationOptions;
        private OutputOptions outputOptions;
        private Paths paths;

        public Execution(
                String artifactFilter,
                List<Subject> subjects,
                Certs certs,
                AttestationOptions attestationOptions,
                OutputOptions outputOptions,
                Paths paths,
                StepContext context) {
            super(context);
            this.artifactFilter = artifactFilter;
            this.subjects = subjects;
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
            if (getContext().get(FilePath.class) == null)
                throw new Exception("Not FilePath, probably not in a step run");
            PrintStream console = getContext().get(TaskListener.class).getLogger();

            console.println("[xygeniSalt Attestation Provenance] running ..");

            if (this.attestationOptions == null) attestationOptions = new AttestationOptions(false, null, false);
            if (this.outputOptions == null) outputOptions = new OutputOptions(null, false, null);

            if (subjects == null) subjects = new ArrayList<>(10);

            if (artifactFilter != null) {
                addArtifactSubjects(
                        getContext().get(FilePath.class),
                        getContext().get(EnvVars.class),
                        getContext().get(TaskListener.class),
                        artifactFilter,
                        subjects);
            }

            XygeniSaltAtSlsaCommandBuilder builder = new XygeniSaltAtSlsaCommandBuilder(subjects);

            if (certs.getKeyless()) {
                builder.withKeyless();
            } else {
                builder.withKey(
                        certs.getKey(),
                        certs.getKeyPassword(),
                        certs.getPublicKey(),
                        certs.getPkiFormat(),
                        certs.getCertificate());
            }

            builder.withRun(
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
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "xygeniSaltAtSlsa";
        }

        @Override
        public String getDisplayName() {
            return "Xygeni Salt Attestation 'Slsa Provenance' command";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }

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
    }
}
