package io.jenkins.plugins.xygeni;

import static org.junit.Assert.*;

import io.jenkins.plugins.xygeni.configuration.XygeniConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsSessionRule;

public class XygeniConfigurationTest extends XygeniBaseTest {

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

            configureXygeni(r, "http://localhost:8080", "XygeniToken");

            assertEquals(
                    "global config Xygeni Token Secret is permanent saved",
                    "XygeniToken",
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
