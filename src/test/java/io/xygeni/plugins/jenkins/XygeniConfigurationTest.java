package io.xygeni.plugins.jenkins;

import static org.junit.Assert.*;

import io.xygeni.plugins.jenkins.configuration.XygeniConfiguration;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlTextInput;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsSessionRule;

public class XygeniConfigurationTest {

    @Rule
    public JenkinsSessionRule sessions = new JenkinsSessionRule();

    /**
     * Tries to exercise enough code paths to catch common mistakes:
     * <ul>
     * <li>missing {@code load}
     * <li>missing {@code save}
     * <li>misnamed or absent getter/setter
     * <li>misnamed {@code textbox}
     * </ul>
     */
    @Test
    public void uiAndStorage() throws Throwable {
        sessions.then(r -> {
            assertNull("url not set initially", XygeniConfiguration.get().getXygeniUrl());
            assertNull("token not set initially", XygeniConfiguration.get().getXygeniTokenSecretId());
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");

            HtmlTextInput tokenField = config.getInputByName("_.xygeniTokenSecretId");
            tokenField.setText("xytoken");
            HtmlTextInput textbox = config.getInputByName("_.xygeniUrl");
            textbox.setText("http://localhost:8080");

            r.submit(config);

            assertEquals(
                    "global config Xygeni Token Secret is permanent saved",
                    "xytoken",
                    XygeniConfiguration.get().getXygeniTokenSecretId());

            assertEquals(
                    "global config Xygeni URL is permanent saved",
                    "http://localhost:8080",
                    XygeniConfiguration.get().getXygeniUrl());
        });

        sessions.then(r -> {
            assertEquals(
                    "still there after restart of Jenkins",
                    "http://localhost:8080",
                    XygeniConfiguration.get().getXygeniUrl());
        });
    }
}
