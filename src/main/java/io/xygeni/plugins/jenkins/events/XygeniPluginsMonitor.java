package io.xygeni.plugins.jenkins.events;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.xygeni.plugins.jenkins.model.PluginsEvent;
import io.xygeni.plugins.jenkins.services.XygeniApiClient;
import java.util.Set;
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

    private static final long RECURRENCE_PERIOD = 60 * 1000; // 1 minute

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

        if (pluginsSnapshot == null) {
            // load initial plugin list on instance start
            pluginsSnapshot = activePlugins;
            onAction(activePlugins, PluginsEvent.Action.onStart, listener);
        } else if (!pluginsSnapshot.equals(activePlugins)) {
            // changes on plugin:version are notified
            onAction(activePlugins, PluginsEvent.Action.onNewPlugins, listener);
        }
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

            logger.log(Level.FINER, "[XygeniPluginMonitor] send event " + event);

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
