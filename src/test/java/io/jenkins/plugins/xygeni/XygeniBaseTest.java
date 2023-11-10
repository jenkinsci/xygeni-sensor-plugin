package io.jenkins.plugins.xygeni;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import hudson.ExtensionList;
import hudson.util.Secret;
import java.io.IOException;
import java.util.Collections;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlTextInput;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jvnet.hudson.test.JenkinsRule;

public class XygeniBaseTest {

    public void addCredentialToRule(String credentialId, String value, JenkinsRule r) throws IOException {
        SystemCredentialsProvider.ProviderImpl system =
                ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(r.getInstance());
        systemStore.addDomain(
                new Domain("test", "", Collections.<DomainSpecification>emptyList()),
                new StringCredentialsImpl(CredentialsScope.GLOBAL, credentialId, "", Secret.fromString(value)));
    }

    public void configureXygeni(JenkinsRule r) throws Exception {
        configureXygeni(r, "http://localhost:9999", "XygeniToken");
    }

    public void configureXygeni(JenkinsRule r, String xygeniUrl, String tokenSecret) throws Exception {
        try (JenkinsRule.WebClient client = r.createWebClient()) {
            HtmlForm config = client.goTo("configure").getFormByName("config");
            HtmlTextInput tokenField = config.getInputByName("_.xygeniTokenSecretId");
            tokenField.setText(tokenSecret);
            HtmlTextInput urlbox = config.getInputByName("_.xygeniUrl");
            urlbox.setText(xygeniUrl);
            r.submit(config);
        }
    }

    public void execFailedLogin(JenkinsRule r) {
        try (JenkinsRule.WebClient client = r.createWebClient()) {
            r.createDummySecurityRealm();
            client.login("dummyuser"); // authenticated
        } catch (Exception e) {
            System.out.println("exc " + e.getMessage());
            // discard login error
        }
    }
}
