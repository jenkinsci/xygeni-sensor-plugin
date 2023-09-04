package io.xygeni.plugins.jenkins.model;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SCMEvent extends XygeniEvent {

    private final String TYPE_CLASS = "scmEvent";

    private static final Logger LOGGER = Logger.getLogger(SCMEvent.class.getName());

    private final Action action;

    public enum Action {
        onCheckout
    }

    public SCMEvent(Action action) {
        this.action = action;
    }

    public static SCMEvent from(Run run, TaskListener listener, Action type) {

        if (run == null) {
            return null;
        }

        EnvVars envVars;
        try {
            envVars = getEnvVars(run, listener);
        } catch (IOException | InterruptedException exp) {
            LOGGER.log(Level.SEVERE, "[SCMEvent] Failed to read env vars", exp);
            return null;
        }

        SCMEvent scmEvent = new SCMEvent(type);
        scmEvent.setProperty("BuildNumber", String.valueOf(run.getNumber()));
        scmEvent.setProperty("BuildUrl", envVars.get("BUILD_URL"));
        scmEvent.setProperty("NodeName", envVars.get("NODE_NAME"));

        String jobName = getJobName(run);
        scmEvent.setProperty("JobName", jobName == null ? "unknown" : jobName);

        String branchName = envVars.get("GIT_BRANCH"); // remote branch from Git plugin
        scmEvent.setProperty("Branch", branchName == null ? "unknown" : branchName);

        String gitCommit = envVars.get("GIT_COMMIT"); // commit-sha from Git plugin
        scmEvent.setProperty("GitCommit", gitCommit == null ? "unknown" : gitCommit);

        String gitUrl = envVars.get("GIT_URL"); // first repo url from Git plugin
        scmEvent.setProperty("GitUrl", gitUrl == null ? "unknown" : gitUrl);

        return scmEvent;
    }

    private static EnvVars getEnvVars(Run run, TaskListener listener) throws IOException, InterruptedException {

        if (listener != null) {
            return run.getEnvironment(listener);
        } else {
            return run.getEnvironment(new LogTaskListener(LOGGER, Level.INFO));
        }
    }

    private static String getJobName(Run run) {

        String jobName = null;
        if (run != null) {
            jobName = run.getParent().getFullName();
        }

        if (jobName == null) {
            return null;
        }
        return jobName.replaceAll("»", "/").replaceAll(" ", "");
    }

    @Override
    protected String getType() {
        return TYPE_CLASS;
    }

    @Override
    protected String getAction() {
        return action.name();
    }
}
