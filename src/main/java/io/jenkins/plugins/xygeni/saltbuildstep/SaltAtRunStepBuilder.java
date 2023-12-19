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
import io.jenkins.plugins.xygeni.saltbuildstep.model.OutputOptions;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtRunCommandBuilder;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Salt Attestation Run Command Builder Class.
 * <p>
 *
 *
 * @author Victor de la Rosa
 */
public class SaltAtRunStepBuilder extends Builder implements SimpleBuildStep {

    private static final Logger logger = Logger.getLogger(SaltAtRunStepBuilder.class.getName());

    private int maxout;
    private String step;
    private int maxerr;
    private int timeout;

    private List<Item> items;

    private String command;

    private OutputOptions outputOptions;

    public int getMaxout() {
        return maxout;
    }

    @DataBoundSetter
    public void setMaxout(int maxout) {
        this.maxout = maxout;
    }

    public String getStep() {
        return step;
    }

    @DataBoundSetter
    public void setStep(String step) {
        this.step = step;
    }

    public int getMaxerr() {
        return maxerr;
    }

    @DataBoundSetter
    public void setMaxerr(int maxerr) {
        this.maxerr = maxerr;
    }

    public int getTimeout() {
        return timeout;
    }

    @DataBoundSetter
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<Item> getItems() {
        return this.items;
    }

    @DataBoundSetter
    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getCommand() {
        return this.command;
    }

    @DataBoundSetter
    public void setCommand(String command) {
        this.command = command;
    }

    @DataBoundSetter
    public void setOutputOptions(OutputOptions outputOptions) {
        this.outputOptions = outputOptions;
    }

    public OutputOptions getOutputOptions() {
        return this.outputOptions;
    }

    @DataBoundConstructor
    public SaltAtRunStepBuilder(OutputOptions outputOptions) {
        this.outputOptions = outputOptions;
        if (outputOptions == null) this.outputOptions = new OutputOptions(null, false, null);
    }

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener) {

        PrintStream console = listener.getLogger();

        console.println("[xygeniSalt Attestation Run] running ..");

        new XygeniSaltAtRunCommandBuilder(getMaxout(), getStep(), getMaxerr(), getTimeout(), getItems(), getCommand())
                .withRun(run, launcher, listener)
                .withOutputOptions(outputOptions)
                .build()
                .run();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link SaltAtRunStepBuilder}.
     */
    @Symbol("xygeniSaltAtRun")
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
            return "Xygeni Salt Attestation 'Run' command";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
