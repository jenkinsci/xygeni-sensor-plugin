package io.jenkins.plugins.xygeni.saltcommand;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import java.io.IOException;

public class XygeniSaltCommand {

    private Launcher launcher;
    private TaskListener listener;

    private EnvVars env;

    private ArgumentListBuilder args;

    public void run() {

        try {

            Launcher.ProcStarter ps = launcher.launch();
            ps.cmds(getCommandArgs());
            if (env != null) ps.envs(env);
            ps.stdin(null);
            ps.stderr(listener.getLogger());
            ps.stdout(listener.getLogger());
            ps.quiet(true);

            listener.getLogger().println("Running Xygeni Salt command: " + args.toString());
            ps.join(); // RUN !

        } catch (IOException | InterruptedException e) {
            listener.getLogger().println("Error running Xygeni Salt:" + e.getMessage());
        }
    }

    public ArgumentListBuilder getCommandArgs() {
        return args;
    }

    public void setLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    public void setArgs(ArgumentListBuilder args) {
        this.args = args;
    }

    public void setEnvVars(EnvVars env) {
        this.env = env;
    }
}
