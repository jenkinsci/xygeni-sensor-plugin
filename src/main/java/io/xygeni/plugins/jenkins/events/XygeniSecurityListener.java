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

package io.xygeni.plugins.jenkins.events;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import io.xygeni.plugins.jenkins.model.ConfigEvent;
import io.xygeni.plugins.jenkins.model.SecurityEvent;
import io.xygeni.plugins.jenkins.services.XygeniApiClient;
import jenkins.security.SecurityListener;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.logging.Logger;

/**
 * A listener of user access events
 */
@Extension
public class XygeniSecurityListener extends SecurityListener {

    private static final Logger logger = Logger.getLogger(XygeniSecurityListener.class.getName());

    @Override
    protected void userCreated(@NonNull String username) {
        try {

            XygeniApiClient client = XygeniApiClient.getInstance();
            if(client == null) {
                logger.fine("[XygeniSecurityListener] Client null. Event Not Send.");
                return;
            }

            SecurityEvent event = SecurityEvent.from(username, SecurityEvent.Type.CREATED);

            client.sendEvent(event);

        } catch (Exception e) {
            logger.severe("[XygeniSecurityListener] Failed to process userCreated event: " + e.getMessage());
        }
    }


}
