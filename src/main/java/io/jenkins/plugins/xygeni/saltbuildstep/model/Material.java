package io.jenkins.plugins.xygeni.saltbuildstep.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class Material extends AbstractDescribableImpl<Material> {

    public String material;

    public String getMaterial() {
        return this.material;
    }

    @DataBoundConstructor
    public Material(String material) {
        this.material = material;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Material> {
        @NonNull
        public String getDisplayName() {
            return "Material";
        }
    }
}
