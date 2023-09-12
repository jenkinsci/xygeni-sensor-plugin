package io.jenkins.plugins.xygeni.events;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.xygeni.model.BuildEvent;
import io.jenkins.plugins.xygeni.services.XygeniApiClient;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Monitor job events.
 *
 * @author Victor de la Rosa
 */
@Extension
public class XygeniRunListener extends RunListener<Run> {

    private static final Logger logger = Logger.getLogger(XygeniRunListener.class.getName());

    @Override
    public void onCompleted(Run r, @NonNull TaskListener listener) {

        onAction(r, BuildEvent.Action.onComplete);
    }

    @Override
    public void onFinalized(Run r) {

        onAction(r, BuildEvent.Action.onFinalized);
    }

    @Override
    public void onStarted(Run r, TaskListener listener) {

        onAction(r, BuildEvent.Action.onStarted);
    }

    public void onAction(Run r, BuildEvent.Action action) {
        try {
            XygeniApiClient client = XygeniApiClient.getInstance();
            if (client == null) {
                logger.finer("[XygeniRunListener] Client null. Event Not Send.");
                return;
            }

            BuildEvent event = BuildEvent.from(r, action);

            logger.log(Level.FINER, "[XygeniRunListener] send event " + event);

            client.sendEvent(event);
        } catch (Exception e) {
            logger.severe("[XygeniRunListener] Failed to process event: " + e.getMessage());
        }
    }
}
