package io.xygeni.plugins.jenkins.configuration;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.PluginWrapper;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.xygeni.plugins.jenkins.services.XygeniApiClient;
import io.xygeni.plugins.jenkins.util.CredentialUtil;
import java.net.URL;
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
 *
 * @author Victor de la Rosa
 */
@Extension
public class XygeniConfiguration extends GlobalConfiguration {

    private static final Logger logger = Logger.getLogger(XygeniConfiguration.class.getName());

    private static final String XYGENIURL_FIELD = "xygeniUrl";
    private static final String XYGENITOKENSECRETID_FIELD = "xygeniTokenSecretId";

    private String xygeniTokenSecretId;
    private String xygeniUrl;

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
            if (plugin.getShortName().equals("xygeni")) {
                return plugin.getVersion();
            }
        }
        return null;
    }

    /** @return the currently field value, if any */
    public String getXygeniTokenSecretId() {
        return xygeniTokenSecretId;
    }

    /**
     * Together with {@link #getXygeniTokenSecretId}, binds to entry in {@code config.jelly}.
     * @param xygeniTokenSecret the new value of this field
     */
    @DataBoundSetter
    public void setXygeniTokenSecretId(String xygeniTokenSecret) {
        this.xygeniTokenSecretId = xygeniTokenSecret;
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
        if (value.isEmpty()) {
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
        if (value.isEmpty()) {
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
     * @param xygeniTokenSecretIdField tokenSecret field value
     * @param xygeniUrlField xygeniurl field value
     * @return FormValidation ok if connect could be establish and token is valid
     *
     */
    @RequirePOST
    public FormValidation doTestXygeniConnection(
            @QueryParameter(XYGENITOKENSECRETID_FIELD) final String xygeniTokenSecretIdField,
            @QueryParameter(XYGENIURL_FIELD) final String xygeniUrlField) {

        Secret xygeniToken = CredentialUtil.getSecret(xygeniTokenSecretIdField);

        XygeniApiClient client = XygeniApiClient.getInstance(xygeniUrlField, xygeniToken);
        if (client == null) {
            return FormValidation.error("Check required values first.");
        }

        if (!client.validateXygeniPing()) {
            return FormValidation.error("Cannot connect to Xygeni platform. Check URL.");
        }

        boolean validConnection = client.validateTokenConnection();

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
        return CredentialUtil.getSecret(xygeniTokenSecretId);
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
        if (xygeniTokenSecretId == null) return false;
        try {
            StringCredentials credential = CredentialUtil.getCredentialFromId(xygeniTokenSecretId);
            return credential != null && !credential.getSecret().getPlainText().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
