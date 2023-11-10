package io.jenkins.plugins.xygeni.events;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import io.jenkins.plugins.xygeni.configuration.XygeniConfiguration;
import io.jenkins.plugins.xygeni.model.ItemEvent;
import io.jenkins.plugins.xygeni.services.XygeniApiClient;
import java.util.logging.Logger;

/**
 * Monitor item events.
 *
 * @author Victor de la Rosa
 */
@Extension
public class XygeniItemListener extends ItemListener {

    private static final Logger logger = Logger.getLogger(XygeniItemListener.class.getName());

    @Override
    public void onDeleted(Item item) {
        sendEvent(item, ItemEvent.Action.DELETE);
    }

    @Override
    public void onCreated(Item item) {
        sendEvent(item, ItemEvent.Action.CREATED);
    }

    @Override
    public void onRenamed(Item item, String oldName, String newName) {
        sendEvent(item, ItemEvent.Action.RENAMED);
    }

    @Override
    public void onUpdated(Item item) {
        sendEvent(item, ItemEvent.Action.UPDATED);
    }

    @Override
    public void onLocationChanged(Item item, String oldFullName, String newFullName) {
        sendEvent(item, ItemEvent.Action.LOCATION_CHANGED);
    }

    private void sendEvent(Item item, ItemEvent.Action action) {

        if (!XygeniConfiguration.get().isEmitItemEvents()) return;

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            logger.fine("[XygeniItemListener] Client null. Event Not Send.");
            return;
        }

        ItemEvent event = ItemEvent.from(item, action);

        logger.finer("[XygeniItemListener] send event " + event);

        client.sendEvent(event);
    }
}
