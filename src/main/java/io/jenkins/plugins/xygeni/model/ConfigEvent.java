package io.jenkins.plugins.xygeni.model;

import hudson.XmlFile;
import hudson.model.Saveable;

public class ConfigEvent extends XygeniEvent {

    public static final String TYPE_CLASS = "configEvent";

    private final Action action;

    public enum Action {
        onChange
    }

    public ConfigEvent(Action type) {
        this.action = type;
    }

    public static ConfigEvent from(Saveable saveableConfig, XmlFile file, Action action) {
        ConfigEvent configEvent = new ConfigEvent(action);
        configEvent.setProperty("implementation", saveableConfig.getClass().toString());
        configEvent.setProperty("fileName", file.getFile().getPath());

        return configEvent;
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
