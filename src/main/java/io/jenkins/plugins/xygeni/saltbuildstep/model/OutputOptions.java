package io.jenkins.plugins.xygeni.saltbuildstep.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class OutputOptions extends AbstractDescribableImpl<OutputOptions> {

    private String output;

    private boolean prettyPrint = false;

    private String outputUnsigned;

    public String getOutput() {
        return this.output;
    }

    @DataBoundSetter
    public void setOutput(String output) {
        this.output = output;
    }

    public boolean getPrettyPrint() {
        return this.prettyPrint;
    }

    @DataBoundSetter
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public String getOutputUnsigned() {
        return this.outputUnsigned;
    }

    @DataBoundSetter
    public void setOutputUnsigned(String outputUnsigned) {
        this.outputUnsigned = outputUnsigned;
    }

    @DataBoundConstructor
    public OutputOptions(String output, boolean prettyPrint, String outputUnsigned) {
        this.output = output;
        this.prettyPrint = prettyPrint;
        this.outputUnsigned = outputUnsigned;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<OutputOptions> {
        public String getDisplayName() {
            return "Output Options";
        }
    }
}
