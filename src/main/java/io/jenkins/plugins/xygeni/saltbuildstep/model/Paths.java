package io.jenkins.plugins.xygeni.saltbuildstep.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class Paths extends AbstractDescribableImpl<Paths> {

    private String saltCommandPath;
    private String basedir;

    @DataBoundSetter
    public void setSaltCommandPath(String saltCommandPath) {
        this.saltCommandPath = saltCommandPath;
    }

    @DataBoundSetter
    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public String getSaltCommandPath() {
        return this.saltCommandPath;
    }

    public String getBasedir() {
        return this.basedir;
    }

    @DataBoundConstructor
    public Paths(String saltCommandPath, String basedir) {

        this.saltCommandPath = saltCommandPath;
        this.basedir = basedir;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Paths> {
        @NonNull
        public String getDisplayName() {
            return "Salt Command Configuration";
        }
    }
}
