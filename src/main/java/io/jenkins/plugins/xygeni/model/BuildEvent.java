package io.jenkins.plugins.xygeni.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Run;

public class BuildEvent extends XygeniEvent {

    public static final String TYPE_CLASS = "buildEvent";

    public enum Action {
        onStarted,
        onFinalized,
        onComplete
    }

    private final Action action;

    public BuildEvent(Action action) {
        this.action = action;
    }

    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "False positive r.getResult()")
    public static BuildEvent from(Run r, Action action) {

        BuildEvent buildEvent = new BuildEvent(action);

        buildEvent.setProperty("id", r.getId());
        buildEvent.setProperty("url", r.getUrl());
        buildEvent.setProperty("fullDisplayName", r.getFullDisplayName());
        buildEvent.setProperty("resultSummary", r.getBuildStatusSummary().message);
        buildEvent.setProperty("causes", String.valueOf(r.getCauses()));

        if (r.getResult() != null) {
            buildEvent.setProperty("failed", String.valueOf(!r.getResult().isCompleteBuild()));
        }

        buildEvent.setProperty("startTime", String.valueOf(r.getStartTimeInMillis()));
        buildEvent.setProperty("runningTime", String.valueOf(r.getDuration()));

        return buildEvent;
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
