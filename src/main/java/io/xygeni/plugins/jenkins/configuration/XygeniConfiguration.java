package io.xygeni.plugins.jenkins.configuration;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.PluginWrapper;
import hudson.XmlFile;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.xygeni.plugins.jenkins.services.XygeniApiClient;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Global configuration of Xygeni Plugin.
 * </p>
 * Xygeni Plugin require Xygeni Api url (use cloud production by default),
 * and a Xygeni Api Token generate by customer administrator to connect to Xygeni api.
 * </p>
 * Xygeni Api Token should be saved as credential secret at Jenkins instance and pass here as secret-id.
 */
@Extension
public class XygeniConfiguration extends GlobalConfiguration {

    private static final Logger logger = Logger.getLogger(XygeniConfiguration.class.getName());

    private static final String XYGENIURL_FIELD = "xygeniUrl";
    private static final String XYGENITOKENSECRET_FIELD = "xygeniTokenSecret";

    private String xygeniTokenSecret;
    private String xygeniUrl;
    private boolean validConnection = false;

    /** @return the singleton instance */
    public static XygeniConfiguration get() {
        return ExtensionList.lookupSingleton(XygeniConfiguration.class);
    }

    public XygeniConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    public String getVersion() {
        for (PluginWrapper plugin : Jenkins.get().getPluginManager().getPlugins()) {
            if(plugin.getShortName().equals("xygeni")) {
                return plugin.getVersion();
            }
        }
        return null;
    }

    /** @return the currently field value, if any */
    public String getXygeniTokenSecret() {
        return xygeniTokenSecret;
    }

    /**
     * Together with {@link #getXygeniTokenSecret}, binds to entry in {@code config.jelly}.
     * @param xygeniTokenSecret the new value of this field
     */
    @DataBoundSetter
    public void setXygeniTokenSecret(String xygeniTokenSecret) {
        this.xygeniTokenSecret = xygeniTokenSecret;
        save();
    }

    /** @return the currently configured label, if any */
    public String getXygeniUrl() {
        return xygeniUrl;
    }

    /**
     * Together with {@link #getXygeniUrl}, binds to entry in {@code config.jelly}.
     * @param xygeniUrl the new value of this field
     */
    @DataBoundSetter
    public void setXygeniUrl(String xygeniUrl) {
        this.xygeniUrl = xygeniUrl;
        save();
    }

    /**
     * Check if field is not empty
     * @param value form field value
     * @return FormValidation ok if not empty or warning message
     */
    public FormValidation doCheckXygeniToken(@QueryParameter String value) {
        if (value.equals("")) {
            return FormValidation.warning(
                    "Please specify a Credential Secret that Xygeni API Token could be read from.");
        }
        if (!isValidToken()) {
            return FormValidation.warning("Please specify a valid Credential Secret to get Xygeni Api Token.");
        }
        return FormValidation.ok();
    }

    /**
     * Check if field is not empty
     * @param value form field value
     * @return FormValidation ok if not empty or warning message
     */
    public FormValidation doCheckXygeniUrl(@QueryParameter String value) {
        if (value.equals("")) {
            return FormValidation.warning("Please specify a Xygeni platform URL.");
        }
        if (!isValidUrl()) {
            return FormValidation.warning("Please specify a valid URL.");
        }
        return FormValidation.ok();
    }

    /**
     * Check if current configuration allow to connect to Xygeni platform.
     *
     * @param xygeniToken tokenSecret field value
     * @param xygeniUrl xygeniurl field value
     * @return FormValidation ok if connect could be establish and token is valid
     *
     */
    @RequirePOST
    public FormValidation doTestXygeniConnection(
            @QueryParameter(XYGENITOKENSECRET_FIELD) final String xygeniToken,
            @QueryParameter(XYGENIURL_FIELD) final String xygeniUrl) {

        XygeniApiClient client = XygeniApiClient.getInstance();
        if (client == null) {
            return FormValidation.error("Check required values first.");
        }

        if (!client.validateXygeniPing(xygeniUrl)) {
            return FormValidation.error("Cannot connect to Xygeni platform. Check URL.");
        }

        validConnection = client.validateTokenConnection(xygeniUrl, getXygeniToken());

        if (!validConnection) {
            return FormValidation.error("Connect to Xygeni Platform but Api Token is not valid.");
        }

        return FormValidation.ok("Great! Connect successfully.");
    }


    /**
     * Read token from credential and return a {@link Secret}
     * @return a Secret
     */
    public Secret getXygeniToken() {
        try {
            if (xygeniTokenSecret == null) return null;
            StringCredentials credential = getCredentialFromId(xygeniTokenSecret);
            if (credential == null || credential.getSecret().getPlainText().isEmpty()) return null;
            return credential.getSecret();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error reading xygeni token secret:", e);
            return null;
        }
    }

    private boolean isValidUrl() {

        if (xygeniUrl == null) return false;
        try {
            new URL(xygeniUrl).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidToken() {
        if (xygeniTokenSecret == null) return false;
        try {
            StringCredentials credential = getCredentialFromId(xygeniTokenSecret);
            return credential != null && !credential.getSecret().getPlainText().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private String getFieldValue(String fieldName, XmlFile file) {
        return "ht";
    }

    public StringCredentials getCredentialFromId(String credentialId) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StringCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        URIRequirementBuilder.fromUri(null).build()),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialId)));
    }
}
