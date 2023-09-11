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

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import io.jenkins.plugins.xygeni.model.SCMEvent;
import io.jenkins.plugins.xygeni.services.XygeniApiClient;
import java.io.File;
import java.util.logging.Logger;

/**
 * Listener of scm events.
 *
 * @author Victor de la Rosa
 */
@Extension
public class XygeniScmListener extends SCMListener {

    private static final Logger logger = Logger.getLogger(XygeniScmListener.class.getName());

    @Override
    public void onCheckout(
            Run<?, ?> run,
            SCM scm,
            FilePath workspace,
            TaskListener listener,
            File changelogFile,
            SCMRevisionState pollingBaseline) {
        try {

            XygeniApiClient client = XygeniApiClient.getInstance();
            if (client == null) {
                logger.finer("[XygeniScmListener] Client null. Event Not Send.");
                return;
            }

            SCMEvent event = SCMEvent.from(run, listener, SCMEvent.Action.onCheckout);

            logger.finer("[XygeniScmListener] Sending event " + event);

            client.sendEvent(event);

        } catch (Exception e) {
            logger.severe("[XygeniSaveableListener] Failed to process saveable change event: " + e.getMessage());
        }
    }
}
