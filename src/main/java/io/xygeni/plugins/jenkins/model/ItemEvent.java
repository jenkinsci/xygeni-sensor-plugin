package io.xygeni.plugins.jenkins.model;

import hudson.model.Item;

public class ItemEvent extends XygeniEvent {

    public static final String TYPE_CLASS = "itemEvent";

    public enum Action {
        LOCATION_CHANGED,
        UPDATED,
        RENAMED,
        CREATED,
        DELETE
    };

    private final Action action;

    public ItemEvent(Action action) {
        this.action = action;
    }

    public static ItemEvent from(Item item, Action action) {

        ItemEvent itemEvent = new ItemEvent(action);
        itemEvent.setProperty("fullName", getItemName(item));

        return itemEvent;
    }

    private static String getItemName(Item item) {
        if (item == null) {
            return "unknown";
        }
        return item.getName();
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
