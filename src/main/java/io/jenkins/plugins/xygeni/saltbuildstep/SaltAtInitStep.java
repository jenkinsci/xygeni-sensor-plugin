package io.jenkins.plugins.xygeni.saltbuildstep;
/**
 * @author vdlr
 * @version 23-Jan-2024 (vdlr)
 */
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Material;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Paths;
import io.jenkins.plugins.xygeni.saltcommand.XygeniSaltAtInitCommandBuilder;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 *
 * @author vdlr
 * @version 23-Jan-2024 (vdlr)
 */
public class SaltAtInitStep extends Step {

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
    public SaltAtInitStep() {}

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(getAttestors(), exclude, materials, paths, context);
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        private List<String> attestors;
        private String exclude;
        private List<Material> materials;

        private Paths paths;

        protected Execution(
                List<String> attestors, String exclude, List<Material> materials, Paths paths, StepContext context) {
            super(context);
            this.attestors = attestors;
            this.exclude = exclude;
            this.materials = materials;
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

            console.println("[xygeniSalt Attestation Init] running ..");

            new XygeniSaltAtInitCommandBuilder(attestors, exclude, materials)
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
            return "xygeniSaltAtInit";
        }

        @Override
        public String getDisplayName() {
            return "Xygeni Salt Attestation 'Init' command";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }
    }

    private List<String> getAttestors() {
        ArrayList<String> attestors = new ArrayList<>();
        if (this.gitAttestor) attestors.add("git");
        if (this.attestorEnv) attestors.add("environment");
        return attestors;
    }
}
