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
import hudson.tasks.Builder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Material;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtInitCommandBuilder;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Salt Init Builder Class.
 * <p>
 *
 *
 * @author Victor de la Rosa
 */
public class SaltAtInitStepBuilder extends Builder implements SimpleBuildStep {

    private static final Logger logger = Logger.getLogger(SaltAtInitStepBuilder.class.getName());

    private boolean gitAttestor;
    private boolean attestorEnv;

    private String exclude;

    private List<Material> materials;

    private Paths paths;

    @DataBoundSetter
    public void setGitAttestor(boolean gitAttestor) {
        this.gitAttestor = gitAttestor;
    }

    public boolean getGitAttestor() {
        return this.gitAttestor;
    }

    @DataBoundSetter
    public void setAttestorEnv(boolean attestorEnv) {
        this.attestorEnv = attestorEnv;
    }

    public boolean getAttestorEnv() {
        return this.attestorEnv;
    }

    @DataBoundSetter
    public void setExclude(String exclude) {
        if ("".equals(exclude)) this.exclude = null;
        else this.exclude = exclude;
    }

    public String getExclude() {
        return this.exclude;
    }

    @DataBoundSetter
    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    @DataBoundSetter
    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    public List<Material> getMaterials() {
        return this.materials;
    }

    public Paths getPaths() {
        return paths;
    }

    @DataBoundConstructor
    public SaltAtInitStepBuilder() {}

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener) {

        PrintStream console = listener.getLogger();

        console.println("[xygeniSalt Attestation Init] running ..");

        new XygeniSaltAtInitCommandBuilder(getAttestors(), getExclude(), getMaterials())
                .withRun(run, launcher, listener)
                .withPaths(paths)
                .build()
                .run();
    }

    private List<String> getAttestors() {
        ArrayList<String> attestors = new ArrayList<>();
        if (this.gitAttestor) attestors.add("git");
        if (this.attestorEnv) attestors.add("environment");
        return attestors;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link SaltAtInitStepBuilder}.
     */
    @Symbol("xygeniSaltAtInit")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

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
            return "Xygeni Salt Attestation 'Init' command";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
