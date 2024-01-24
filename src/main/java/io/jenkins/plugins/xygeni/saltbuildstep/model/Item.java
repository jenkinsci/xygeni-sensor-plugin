package io.jenkins.plugins.xygeni.saltbuildstep.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class Item extends AbstractDescribableImpl<Item> implements Serializable {

    public enum Type {
        material,
        product,
        statement,
        predicate
    }

    private String name;
    private String type;
    private String predicateType;
    private String value;
    private String file;
    private String image;
    private String digest;

    @Extension
    public static class DescriptorImpl extends Descriptor<Item> {
        public String getDisplayName() {
            return "Item";
        }

        @RequirePOST
        public FormValidation doCheckName(@QueryParameter String name) {
            if (name == null || name.isEmpty()) {
                return FormValidation.error("Please set a name");
            }
            return FormValidation.ok();
        }

        @RequirePOST
        public FormValidation doCheckType(@QueryParameter String type) {
            if (type == null || type.isEmpty()) {
                return FormValidation.error("Please set a Type");
            }
            try {
                Type.valueOf(type);
            } catch (IllegalArgumentException e) {
                return FormValidation.error("Type unknown");
            }
            return FormValidation.ok();
        }

        @RequirePOST
        public FormValidation doCheckPredicateType(@QueryParameter String predicateType, @QueryParameter String type) {
            if (type == null || !type.equals(Type.predicate.name())) return FormValidation.ok();
            if (predicateType == null || predicateType.isEmpty()) {
                return FormValidation.error("Please set a Type");
            }
            return FormValidation.ok();
        }
    }

    @DataBoundConstructor
    public Item(
            String name, String type, String predicateType, String value, String file, String image, String digest) {
        this.name = name;
        this.type = type;
        this.predicateType = predicateType;
        this.value = value;
        this.file = file;
        this.image = image;
        this.digest = digest;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPredicateType() {
        return predicateType;
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

    public boolean isImage() {
        return (image != null && !image.equals(""));
    }
}
