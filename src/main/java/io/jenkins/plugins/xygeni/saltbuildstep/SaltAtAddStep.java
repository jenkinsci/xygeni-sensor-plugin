package io.jenkins.plugins.xygeni.saltbuildstep;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Item;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtAddCommandBuilder;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Salt Attestation Add Builder Class.
 *
 *
 * @author Victor de la Rosa
 */
public class SaltAtAddStep extends Step {

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
    public SaltAtAddStep() {}

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new SaltAtAddStep.Execution(items, paths, context);
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        private List<Item> items;

        private Paths paths;

        public Execution(List<Item> items, Paths paths, StepContext context) {
            super(context);
            this.items = items;
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

            console.println("[xygeniSalt Attestation Add] running ..");

            new XygeniSaltAtAddCommandBuilder(items)
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
            return "xygeniSaltAtAdd";
        }

        @Override
        public String getDisplayName() {
            return "Xygeni Salt Attestation 'Add' command";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }
    }
}
