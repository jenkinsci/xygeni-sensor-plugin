package io.jenkins.plugins.xygeni.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Computer;
import hudson.model.labels.LabelAtom;
import java.util.List;
import jenkins.model.Jenkins;
import org.apache.commons.compress.utils.Lists;

public class ComputerEvent extends XygeniEvent {

    private static final String TYPE_CLASS = "computerEvent";

    private final Action action;

    public enum Action {
        online,
        offline,
        temporaryOnline,
        temporaryOffline,
        launchFailure
    }

    public ComputerEvent(Action type) {
        this.action = type;
    }

    public static ComputerEvent from(Computer computer, Action action) {

        ComputerEvent computerEvent = new ComputerEvent(action);
        computerEvent.setProperty("nodeName", getNodeName(computer));
        computerEvent.setProperty("labels", getComputerLabels(computer));

        return computerEvent;
    }

    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "False positive computer.getNode()")
    private static String getComputerLabels(Computer computer) {
        List<String> labels = Lists.newArrayList();

        if (computer.getNode() == null) return "";

        for (LabelAtom label : computer.getNode().getAssignedLabels()) {
            if (label != null) {
                labels.add(label.getName());
            }
        }

        return String.join(",", labels);
    }

    private static String getNodeName(Computer computer) {
        if (computer == null) {
            return "unknown";
        }
        if (computer instanceof Jenkins.MasterComputer) {
            return "master";
        } else {
            return computer.getName();
        }
    }

    @Override
    protected String getType() {
        return TYPE_CLASS;
    }

    @Override
    protected String getAction() {
        return this.action.name();
    }
}
