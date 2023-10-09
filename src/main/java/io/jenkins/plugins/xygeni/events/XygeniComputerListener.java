package io.jenkins.plugins.xygeni.events;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import hudson.slaves.OfflineCause;
import io.jenkins.plugins.xygeni.configuration.XygeniConfiguration;
import io.jenkins.plugins.xygeni.model.ComputerEvent;
import io.jenkins.plugins.xygeni.services.XygeniApiClient;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * It will monitor slave events.
 *
 * @author Victor de la Rosa
 */
@Extension
public class XygeniComputerListener extends ComputerListener {

    private static final Logger logger = Logger.getLogger(XygeniComputerListener.class.getName());

    @Override
    public void onOnline(final Computer computer, final TaskListener listener) {
        sendEvent(computer, ComputerEvent.Action.online);
    }

    @Override
    public void onOffline(@Nonnull Computer computer, @CheckForNull OfflineCause cause) {
        sendEvent(computer, ComputerEvent.Action.offline);
    }

    @Override
    public void onTemporarilyOnline(Computer computer) {
        sendEvent(computer, ComputerEvent.Action.temporaryOnline);
    }

    @Override
    public void onTemporarilyOffline(Computer computer, OfflineCause cause) {
        sendEvent(computer, ComputerEvent.Action.temporaryOffline);
    }

    @Override
    public void onLaunchFailure(Computer computer, TaskListener taskListener) {
        sendEvent(computer, ComputerEvent.Action.launchFailure);
    }

    private void sendEvent(Computer computer, ComputerEvent.Action action) {

        if (!XygeniConfiguration.get().isEmitComputerEvents()) return;

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            logger.fine("[XygeniComputerListener] Client null. Event Not Send.");
            return;
        }

        ComputerEvent event = ComputerEvent.from(computer, action);

        logger.finer("[XygeniComputerListener] send event " + event);

        client.sendEvent(event);
    }
}
