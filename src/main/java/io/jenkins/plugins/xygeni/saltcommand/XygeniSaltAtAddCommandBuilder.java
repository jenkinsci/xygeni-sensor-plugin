package io.jenkins.plugins.xygeni.saltcommand;

import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Item;
import java.util.List;

public class XygeniSaltAtAddCommandBuilder extends XygeniSaltCommandBuilder {

    private static final String ADD_COMMAND = "add";

    private final List<Item> items;

    public XygeniSaltAtAddCommandBuilder(List<Item> items) {
        this.items = items;
    }

    @Override
    protected String getCommand() {
        return ADD_COMMAND;
    }

    @Override
    protected boolean isAttestationCommand() {
        return true;
    }

    @Override
    protected void addCommandArgs(ArgumentListBuilder args, Run<?, ?> build) {

        for (Item item : items) {
            args.add("--name=" + item.getName());
            if (item.getType() != null) {
                args.add("--type=" + item.getType());
                if (item.getType().equals(Item.Type.predicate.name())) {
                    args.add("--predicate-type=" + item.getPredicateType());
                }
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
    }
}
