package io.jenkins.plugins.xygeni.saltbuildstep;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Item;
import io.jenkins.plugins.xygeni.saltbuildstep.model.OutputOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtRunCommandBuilder;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Salt Attestation Run Command Builder Class.
 *
 * @author Victor de la Rosa
 */
public class SaltAtRunStep extends Step {

    private Integer maxout;
    private String step;
    private Integer maxerr;
    private Integer timeout;

    private String command;

    private List<Item> items;

    private OutputOptions outputOptions;

    private Paths paths;

    public Integer getMaxout() {
        return maxout;
    }

    @DataBoundSetter
    public void setMaxout(Integer maxout) {
        this.maxout = maxout;
    }

    public String getStep() {
        return step;
    }

    @DataBoundSetter
    public void setStep(String step) {
        this.step = step;
    }

    public Integer getMaxerr() {
        return maxerr;
    }

    @DataBoundSetter
    public void setMaxerr(Integer maxerr) {
        this.maxerr = maxerr;
    }

    public Integer getTimeout() {
        return timeout;
    }

    @DataBoundSetter
    public void setTimeout(Integer timeout) {
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

    @DataBoundSetter
    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    public Paths getPaths() {
        return paths;
    }

    public OutputOptions getOutputOptions() {
        return this.outputOptions;
    }

    @DataBoundConstructor
    public SaltAtRunStep(OutputOptions outputOptions) {
        this.outputOptions = outputOptions;
        if (outputOptions == null) this.outputOptions = new OutputOptions(null, false, null);
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new SaltAtRunStep.Execution(
                maxout, step, maxerr, timeout, command, items, outputOptions, paths, context);
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        private Integer maxout;
        private String step;
        private Integer maxerr;
        private Integer timeout;

        private List<Item> items;

        private String command;

        private OutputOptions outputOptions;

        private Paths paths;

        public Execution(
                Integer maxout,
                String step,
                Integer maxerr,
                Integer timeout,
                String command,
                List<Item> items,
                OutputOptions outputOptions,
                Paths paths,
                StepContext context) {
            super(context);
            this.maxout = maxout;
            this.step = step;
            this.maxerr = maxerr;
            this.timeout = timeout;
            this.items = items;
            this.command = command;
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

            console.println("[xygeniSalt Attestation Run] running ..");

            new XygeniSaltAtRunCommandBuilder(maxout, step, maxerr, timeout, items, command)
                    .withRun(
                            getContext().get(Run.class),
                            getContext().get(Launcher.class),
                            getContext().get(TaskListener.class),
                            getContext().get(EnvVars.class))
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
            return "xygeniSaltAtRun";
        }

        @Override
        public String getDisplayName() {
            return "Xygeni Salt Attestation 'Run' command";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }
    }
}
