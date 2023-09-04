package io.xygeni.plugins.jenkins.util;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.security.ACL;
import hudson.util.Secret;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class CredentialUtil {

    private static final Logger logger = Logger.getLogger(CredentialUtil.class.getName());

    public static Secret getSecret(String secretName) {
        try {
            if (secretName == null) return null;
            StringCredentials credential = getCredentialFromId(secretName);
            if (credential == null || credential.getSecret().getPlainText().isEmpty()) return null;
            return credential.getSecret();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error reading secret:", e);
            return null;
        }
    }

    public static StringCredentials getCredentialFromId(String credentialId) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StringCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        URIRequirementBuilder.fromUri(null).build()),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialId)));
    }
}
