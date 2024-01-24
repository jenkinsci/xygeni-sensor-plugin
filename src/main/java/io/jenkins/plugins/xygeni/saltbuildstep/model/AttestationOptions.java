package io.jenkins.plugins.xygeni.saltbuildstep.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class AttestationOptions extends AbstractDescribableImpl<AttestationOptions> implements Serializable {

    private boolean noUpload;
    private String project;
    private boolean noResultUpload;

    public boolean getNoUpload() {
        return this.noUpload;
    }

    @DataBoundSetter
    public void setNoUpload(boolean noupload) {
        this.noUpload = noupload;
    }

    public String getProject() {
        return this.project;
    }

    @DataBoundSetter
    public void setProject(String project) {
        this.project = project;
    }

    public boolean getNoResultUpload() {
        return this.noResultUpload;
    }

    @DataBoundSetter
    public void setNoResultUpload(boolean noresultupload) {
        this.noResultUpload = noresultupload;
    }

    @DataBoundConstructor
    public AttestationOptions(boolean noUpload, String project, boolean noResultUpload) {
        this.noUpload = noUpload;
        this.project = project;
        this.noResultUpload = noResultUpload;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<AttestationOptions> {
        public String getDisplayName() {
            return "Attestation Options";
        }
    }
}
