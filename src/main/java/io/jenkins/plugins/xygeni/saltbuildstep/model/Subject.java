package io.jenkins.plugins.xygeni.saltbuildstep.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class Subject extends AbstractDescribableImpl<Subject> {

    private String name;
    private String value;
    private String file;
    private String image;

    private String digest;

    @Extension
    public static class DescriptorImpl extends Descriptor<Subject> {
        public String getDisplayName() {
            return "Subject";
        }

        @RequirePOST
        public FormValidation doCheckName(@QueryParameter String name) {
            if (name == null || name.isEmpty()) {
                return FormValidation.error("Please set a name");
            }
            return FormValidation.ok();
        }
    }

    @DataBoundConstructor
    public Subject(String name, String value, String file, String image, String digest) {
        this.name = name;
        this.value = value;
        this.file = file;
        this.image = image;
        this.digest = digest;
    }

    public static Subject of(@NonNull FilePath artifact, @NonNull FilePath workspace) {
        String name = artifact.getName();
        return new Subject(name, null, artifact.getRemote(), null, null);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getFile() {
        return file;
    }

    public String getImage() {
        return image;
    }

    public String getDigest() {
        return digest;
    }

    public boolean isValue() {
        return (value != null && !value.equals(""));
    }

    public boolean isFile() {
        return (file != null && !file.equals(""));
    }

    public boolean isDigest() {
        return (digest != null && !digest.equals(""));
    }
}
