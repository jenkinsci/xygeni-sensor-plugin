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
import io.jenkins.plugins.xygeni.saltbuildstep.model.Item;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtAddCommandBuilder;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Salt Attestation Add Builder Class.
 *
 *
 * @author Victor de la Rosa
 */
public class SaltAtAddStepBuilder extends Builder implements SimpleBuildStep {

    private static final Logger logger = Logger.getLogger(SaltAtAddStepBuilder.class.getName());

    private List<Item> items;

    private Paths paths;

    @DataBoundSetter
    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return this.items;
    }

    @DataBoundSetter
    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    public Paths getPaths() {
        return paths;
    }

    @DataBoundConstructor
    public SaltAtAddStepBuilder() {}

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener) {

        PrintStream console = listener.getLogger();

        console.println("[xygeniSalt Attestation Add] running ..");

        new XygeniSaltAtAddCommandBuilder(getItems())
                .withRun(run, launcher, listener)
                .withPaths(paths)
                .build()
                .run();
    }

    /**
     * Descriptor for {@link SaltAtAddStepBuilder}.
     */
    @Symbol("xygeniSaltAtAdd")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        // descritor

        public static SaltAtAddStepBuilder.DescriptorImpl get() {
            return ExtensionList.lookupSingleton(SaltAtAddStepBuilder.DescriptorImpl.class);
        }

        @NonNull
        public String getDisplayName() {
            return "Xygeni Salt Attestation 'Add' command";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
