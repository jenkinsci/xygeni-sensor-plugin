package io.xygeni.plugins.jenkins;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

import java.util.logging.Level;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlTextInput;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsSessionRule;
import org.jvnet.hudson.test.LoggerRule;

public class XygeniApiClientTest {

    @Rule
    public JenkinsSessionRule sessions = new JenkinsSessionRule();

    public @Rule LoggerRule logger = new LoggerRule();

    /**
     * Run sendEvent
     */
    @Test
    public void testEventLogger() throws Throwable {
        sessions.then(r -> {
            logger.capture(100).record("io.xygeni.plugins", Level.ALL);

            try (JenkinsRule.WebClient client = r.createWebClient()) {

                // set test connection
                HtmlForm config = client.goTo("configure").getFormByName("config");
                HtmlTextInput tokenField = config.getInputByName("_.xygeniTokenSecretId");
                tokenField.setText("xytoken");
                HtmlTextInput urlbox = config.getInputByName("_.xygeniUrl");
                urlbox.setText("http://localhost:9999");
                r.submit(config);

                assertThat(
                        "listener write event to log",
                        logger,
                        LoggerRule.recorded(Level.FINER, containsString("[XygeniSaveableListener] Sending event:")));

                assertThat(
                        "client write request to log",
                        logger,
                        LoggerRule.recorded(Level.FINEST, containsString("[XygeniApiClient] Sending post:")));

                assertThat(
                        "client write errors to log",
                        logger,
                        LoggerRule.recorded(Level.WARNING, containsString("[XygeniApiClient] sendEvent error")));
            }
        });
    }
}
