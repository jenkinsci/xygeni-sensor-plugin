package io.jenkins.plugins.xygeni.model;

import java.util.Objects;
import java.util.Set;

public class PluginsEvent extends XygeniEvent {

    public static final String TYPE_CLASS = "pluginsEvent";

    public enum Action {
        onNewPlugins,
        onStart
    }

    private final Action action;

    public PluginsEvent(Action action) {
        this.action = action;
    }

    public static PluginsEvent from(Set<ActivePlugin> activePlugins, Action action) {

        PluginsEvent pluginsEvent = new PluginsEvent(action);

        activePlugins.forEach(ap -> pluginsEvent.setProperty("plugin:" + ap.getName(), ap.getVersion()));

        return pluginsEvent;
    }

    @Override
    protected String getType() {
        return TYPE_CLASS;
    }

    @Override
    protected String getAction() {
        return action.name();
    }

    public static class ActivePlugin {
        private final String name;
        private final String version;

        public ActivePlugin(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ActivePlugin that = (ActivePlugin) o;
            return Objects.equals(name, that.name) && Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, version);
        }
    }
}
