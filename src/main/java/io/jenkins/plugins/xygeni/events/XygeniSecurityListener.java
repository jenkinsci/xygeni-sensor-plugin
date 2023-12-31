/*
The MIT License

Copyright (c) 2015-Present Datadog, Inc <opensource@datadoghq.com>
All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package io.jenkins.plugins.xygeni.events;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.xygeni.model.SecurityEvent;
import io.jenkins.plugins.xygeni.services.XygeniApiClient;
import io.jenkins.plugins.xygeni.util.UserUtil;
import java.util.logging.Logger;
import jenkins.security.SecurityListener;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Listener of user access events.
 *
 * @author Victor de la Rosa
 */
@Extension
public class XygeniSecurityListener extends SecurityListener {

    private static final Logger logger = Logger.getLogger(XygeniSecurityListener.class.getName());

    @Override
    protected void authenticated2(@NonNull UserDetails details) {

        try {
            String userEmail = UserUtil.getUserConfiguredEmail(details.getUsername());
            sendEvent(details.getUsername(), userEmail, SecurityEvent.Action.authenticated);

        } catch (Exception e) {
            logger.severe("[XygeniSecurityListener] Failed to process userCreated event: " + e.getMessage());
        }
    }

    @Override
    protected void userCreated(@NonNull String username) {
        try {
            String userEmail = UserUtil.getUserConfiguredEmail(username);
            sendEvent(username, userEmail, SecurityEvent.Action.created);

        } catch (Exception e) {
            logger.severe("[XygeniSecurityListener] Failed to process userCreated event: " + e.getMessage());
        }
    }

    @Override
    protected void failedToLogIn(@NonNull String username) {
        System.out.println("xxx failed");
        sendEvent(username, SecurityEvent.Action.failedToLogin);
    }

    @Override
    protected void failedToAuthenticate(@NonNull String username) {
        System.out.println("xxx failed a");
        sendEvent(username, SecurityEvent.Action.failedToLogin);
    }

    private void sendEvent(String username, SecurityEvent.Action action) {
        sendEvent(username, null, action);
    }

    private void sendEvent(String username, String userEmail, SecurityEvent.Action action) {

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            logger.fine("[XygeniSecurityListener] Client null. Event Not Send.");
            return;
        }

        SecurityEvent event = SecurityEvent.from(username, userEmail, action);

        logger.finer("[XygeniSecurityListener] Sending event: " + event);

        client.sendEvent(event);
    }
}
