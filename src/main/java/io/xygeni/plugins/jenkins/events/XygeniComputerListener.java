package io.xygeni.plugins.jenkins.events;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import hudson.slaves.OfflineCause;
import io.xygeni.plugins.jenkins.model.ComputerEvent;
import io.xygeni.plugins.jenkins.services.XygeniApiClient;
import java.io.IOException;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * It will monitor slave events.
 */
@Extension
public class XygeniComputerListener extends ComputerListener {

    private static final Logger logger = Logger.getLogger(XygeniComputerListener.class.getName());

    @Override
    public void onOnline(final Computer computer, final TaskListener listener)
            throws IOException, InterruptedException {

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            logger.fine("[XygeniComputerListener] Client null. Event Not Send.");
            return;
        }

        ComputerEvent event = ComputerEvent.from(computer, ComputerEvent.Action.online);

        logger.finer("[XygeniComputerListener] send event " + event);

        client.sendEvent(event);
    }

    @Override
    public void onOffline(@Nonnull Computer computer, @CheckForNull OfflineCause cause) {

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            logger.fine("[XygeniComputerListener] Client null. Event Not Send.");
            return;
        }

        ComputerEvent event = ComputerEvent.from(computer, ComputerEvent.Action.offline);

        logger.finer("[XygeniComputerListener] send event " + event);

        client.sendEvent(event);
    }

    @Override
    public void onTemporarilyOnline(Computer computer) {

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            logger.fine("[XygeniComputerListener] Client null. Event Not Send.");
            return;
        }

        ComputerEvent event = ComputerEvent.from(computer, ComputerEvent.Action.temporaryOnline);

        logger.finer("[XygeniComputerListener] send event " + event);

        client.sendEvent(event);
    }

    @Override
    public void onTemporarilyOffline(Computer computer, OfflineCause cause) {

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            logger.fine("[XygeniComputerListener] Client null. Event Not Send.");
            return;
        }

        ComputerEvent event = ComputerEvent.from(computer, ComputerEvent.Action.temporaryOffline);

        logger.finer("[XygeniComputerListener] send event " + event);

        client.sendEvent(event);
    }

    @Override
    public void onLaunchFailure(Computer computer, TaskListener taskListener) throws IOException, InterruptedException {

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            logger.fine("[XygeniComputerListener] Client null. Event Not Send.");
            return;
        }

        ComputerEvent event = ComputerEvent.from(computer, ComputerEvent.Action.launchFailure);

        logger.finer("[XygeniComputerListener] send event " + event);

        client.sendEvent(event);
    }
}
