package io.xygeni.plugins.jenkins.events;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import io.xygeni.plugins.jenkins.model.ItemEvent;
import io.xygeni.plugins.jenkins.services.XygeniApiClient;

import java.util.logging.Logger;

/**
 * It will monitor item events.
 */
@Extension
public class XygeniItemListener extends ItemListener {

  private static final Logger logger = Logger.getLogger(XygeniItemListener.class.getName());

  @Override
  public void onDeleted(Item item) {

    XygeniApiClient client = XygeniApiClient.getInstance();
    if(client == null) {
      logger.fine("[XygeniItemListener] Client null. Event Not Send.");
      return;
    }

    ItemEvent event = ItemEvent.from(item, ItemEvent.Action.DELETE);

    client.sendEvent(event);

  }

  @Override
  public void onCreated(Item item) {

    XygeniApiClient client = XygeniApiClient.getInstance();
    if(client == null) {
      logger.fine("[XygeniItemListener] Client null. Event Not Send.");
      return;
    }

    ItemEvent event = ItemEvent.from(item, ItemEvent.Action.CREATED);

    client.sendEvent(event);
  }

  @Override
  public void onRenamed(Item item, String oldName, String newName) {

    XygeniApiClient client = XygeniApiClient.getInstance();
    if(client == null) {
      logger.fine("[XygeniItemListener] Client null. Event Not Send.");
      return;
    }

    ItemEvent event = ItemEvent.from(item, ItemEvent.Action.RENAMED);

    client.sendEvent(event);
  }

  @Override
  public void onUpdated(Item item) {

    XygeniApiClient client = XygeniApiClient.getInstance();
    if(client == null) {
      logger.fine("[XygeniItemListener] Client null. Event Not Send.");
      return;
    }

    ItemEvent event = ItemEvent.from(item, ItemEvent.Action.UPDATED);

    client.sendEvent(event);
  }

  @Override
  public void onLocationChanged(Item item, String oldFullName, String newFullName) {

    XygeniApiClient client = XygeniApiClient.getInstance();
    if(client == null) {
      logger.fine("[XygeniItemListener] Client null. Event Not Send.");
      return;
    }

    ItemEvent event = ItemEvent.from(item, ItemEvent.Action.LOCATION_CHANGED);

    client.sendEvent(event);
  }

}