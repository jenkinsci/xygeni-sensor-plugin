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
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import io.jenkins.plugins.xygeni.configuration.XygeniConfiguration;
import io.jenkins.plugins.xygeni.model.ConfigEvent;
import io.jenkins.plugins.xygeni.services.XygeniApiClient;
import java.util.List;
import java.util.logging.Logger;

/**
 * Listener of saveable events.
 *
 * @author Victor de la Rosa
 */
@Extension
public class XygeniSaveableListener extends SaveableListener {

    private static final Logger logger = Logger.getLogger(XygeniSaveableListener.class.getName());

    private List<String> excludedFiles = List.of("queue.xml", "build.xml");

    @Override
    public void onChange(Saveable config, XmlFile file) {
        try {

            if (!XygeniConfiguration.get().isEmitConfigEvents()) return;

            if (config == null || file == null) return;

            XygeniApiClient client = XygeniApiClient.getInstance();
            if (client == null) {
                logger.finer("[XygeniSaveableListener] Client null. Event Not Send.");
                return;
            }

            if (excludedFiles.contains(file.getFile().getName())) return;

            ConfigEvent event = ConfigEvent.from(config, file, ConfigEvent.Action.onChange);

            logger.finer("[XygeniSaveableListener] Sending event: " + event);

            client.sendEvent(event);

        } catch (Exception e) {
            logger.severe("[XygeniSaveableListener] Failed to process saveable change event: " + e.getMessage());
        }
    }
}
