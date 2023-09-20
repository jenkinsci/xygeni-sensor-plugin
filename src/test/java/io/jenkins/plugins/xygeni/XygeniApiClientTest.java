package io.jenkins.plugins.xygeni;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import io.jenkins.plugins.xygeni.events.XygeniPluginsMonitor;
import java.util.logging.Level;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsSessionRule;
import org.jvnet.hudson.test.LoggerRule;

public class XygeniApiClientTest extends XygeniBaseTest {

    @Rule
    public JenkinsSessionRule jr = new JenkinsSessionRule();

    public @Rule LoggerRule logger = new LoggerRule();

    @Test
    public void testPluginMonitorEvents() throws Throwable {
        jr.then(r -> {
            logger.capture(100).record("io.jenkins.plugins.xygeni", Level.ALL);

            addCredentialToRule("XygeniToken", "anyvalue", r);
            configureXygeni(r);

            XygeniPluginsMonitor pm = new XygeniPluginsMonitor();

            pm.doRun();

            // simulate periodic run
            Thread.sleep(500);
            pm.doRun();

            assertThat(
                    "send event is not logged",
                    logger,
                    LoggerRule.recorded(Level.FINER, containsString("[XygeniPluginMonitor] Sending event:")));

            assertThat(
                    "api client request is not logged",
                    logger,
                    LoggerRule.recorded(Level.FINEST, containsString("[XygeniApiClient] Sending post:")));

            assertThat(
                    "api client error is not logged",
                    logger,
                    LoggerRule.recorded(Level.WARNING, containsString("[XygeniApiClient] sendEvent error")));
        });
    }
}
