package io.jenkins.plugins.xygeni.events;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.plugins.xygeni.model.PluginsEvent;
import io.jenkins.plugins.xygeni.services.XygeniApiClient;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;

/**
 * Performs monitoring of installed plugins.
 *
 * @author Victor de la Rosa
 */
@Extension
public class XygeniPluginsMonitor extends AsyncPeriodicWork {

    private static final long RECURRENCE_PERIOD = TimeUnit.MINUTES.toMillis(5); // 5 min

    private static final Logger logger = Logger.getLogger(XygeniPluginsMonitor.class.getName());

    private Set<PluginsEvent.ActivePlugin> pluginsSnapshot;

    public XygeniPluginsMonitor() {
        super("Xygeni Plugins Monitor");
    }

    @Override
    public long getRecurrencePeriod() {
        return RECURRENCE_PERIOD;
    }

    @Override
    protected void execute(TaskListener listener) {
        if (!isActivated()) return;

        final Jenkins instance = Jenkins.get();

        Set<PluginsEvent.ActivePlugin> activePlugins = instance.getPluginManager().getPlugins().stream()
                .map(pw -> new PluginsEvent.ActivePlugin(pw.getShortName(), pw.getVersion()))
                .collect(Collectors.toSet());

        PluginsEvent.Action action = getAction(pluginsSnapshot, activePlugins);
        if (action == null) return; // no change

        updatePluginSnapshot(activePlugins);
        onAction(activePlugins, action, listener);
    }

    private PluginsEvent.Action getAction(
            Set<PluginsEvent.ActivePlugin> pluginsSnapshot, Set<PluginsEvent.ActivePlugin> activePlugins) {
        if (pluginsSnapshot == null) {
            return PluginsEvent.Action.onStart;
        } else if (!pluginsSnapshot.equals(activePlugins)) {
            return PluginsEvent.Action.onNewPlugins;
        } else {
            return null;
        }
    }

    // synchronized as async periodic work could change this concurrently
    private synchronized void updatePluginSnapshot(Set<PluginsEvent.ActivePlugin> activePlugins) {
        pluginsSnapshot = activePlugins;
    }

    private void onAction(
            Set<PluginsEvent.ActivePlugin> activePlugins, PluginsEvent.Action action, TaskListener taskListener) {
        try {
            XygeniApiClient client = XygeniApiClient.getInstance();
            if (client == null) {
                logger.finer("[XygeniPluginMonitor] Client null. Event Not Send.");
                return;
            }

            PluginsEvent event = PluginsEvent.from(activePlugins, action);

            logger.log(Level.FINER, "[XygeniPluginMonitor] Sending event: " + event);

            client.sendEvent(event);
        } catch (Exception e) {
            logger.severe("[XygeniPluginMonitor] Failed to process event: " + e.getMessage());
        }
    }

    public boolean isActivated() {
        final Jenkins instance = Jenkins.get();
        return instance.getInitLevel() == InitMilestone.COMPLETED;
    }
}
