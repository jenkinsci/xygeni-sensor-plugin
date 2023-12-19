package io.jenkins.plugins.xygeni.saltcommand;

import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Item;
import java.util.List;

public class XygeniSaltAtRunCommandBuilder extends XygeniSaltAtCommandBuilder {

    private static final String INIT_COMMAND = "run";

    private int maxout;
    private String step;
    private int maxerr;
    private int timeout;
    private final List<Item> items;

    private String commandline;

    public String getMaxoutString() {
        return String.valueOf(maxout);
    }

    public String getStep() {
        return step;
    }

    public String getMaxerrString() {
        return String.valueOf(maxerr);
    }

    public String getTimeoutString() {
        return String.valueOf(timeout);
    }

    public List<Item> getItems() {
        return this.items;
    }

    public String getCommandLine() {
        return this.commandline;
    }

    public XygeniSaltAtRunCommandBuilder(
            int maxout, String step, int maxerr, int timeout, List<Item> items, String commandline) {
        this.maxout = maxout;
        this.step = step;
        this.maxerr = maxerr;
        this.timeout = timeout;
        this.items = items;
        this.commandline = commandline;
    }

    @Override
    protected String getCommand() {
        return INIT_COMMAND;
    }

    @Override
    protected void addCommandArgs(ArgumentListBuilder args, Run<?, ?> build) {

        args.add("--max-out=" + getMaxoutString());
        args.add("--step=" + getStep());
        args.add("--max-err=" + getMaxerrString());
        args.add("--timeout=" + getTimeoutString());

        for (Item item : items) {
            args.add("--name=" + item.getName());
            if (item.getType() != null) {
                args.add("--type=" + item.getType());
            }
            if (item.isValue()) {
                args.add("--value=" + item.getValue());
            } else if (item.isFile()) {
                args.add("--file=" + item.getFile());
            } else if (item.isDigest()) {
                args.add("--digest=" + item.getDigest());
            } else {
                args.add("--image=" + item.getImage());
            }
        }
        args.add("--", getCommandLine());
    }
}
