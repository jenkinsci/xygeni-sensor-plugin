package io.jenkins.plugins.xygeni.saltbuildstep.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class Certs extends AbstractDescribableImpl<Certs> implements Serializable {

    private String key;
    private String publicKey;
    private String certificate;
    private String keyPassword;
    private String pkiFormat;
    private boolean keyless = false;

    public String getKey() {
        return key;
    }

    @DataBoundSetter
    public void setKey(String key) {
        this.key = key;
    }

    public String getPublicKey() {
        return publicKey;
    }

    @DataBoundSetter
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getCertificate() {
        return certificate;
    }

    @DataBoundSetter
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    @DataBoundSetter
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getPkiFormat() {
        return pkiFormat;
    }

    @DataBoundSetter
    public void setPkiFormat(String pkiformat) {
        this.pkiFormat = pkiformat;
    }

    public boolean getKeyless() {
        return this.keyless;
    }

    @DataBoundSetter
    public void setKeyless(boolean keyless) {
        this.keyless = keyless;
    }

    @DataBoundConstructor
    public Certs(
            String key, String keyPassword, String publicKey, String pkiFormat, String certificate, boolean keyless) {
        this.key = key;
        this.keyPassword = keyPassword;
        this.publicKey = publicKey;
        this.pkiFormat = pkiFormat;
        this.certificate = certificate;
        this.keyless = keyless;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Certs> {
        public String getDisplayName() {
            return "Signer Configuration";
        }
    }
}
