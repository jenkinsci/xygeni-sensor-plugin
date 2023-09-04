package io.xygeni.plugins.jenkins.util;

import hudson.model.User;

public class UserUtil {

    public static String getUserId() {
        User user = User.current();
        if (user == null) {
            return "anonymous";
        } else {
            return user.getId();
        }
    }

    public static String getUserConfiguredEmail(String username) {

        if (username == null) return null;

        User user = User.getById(username, false);

        if (user == null) return null;

        // Mailer.UserProperty mailProperty = user.getProperty(Mailer.UserProperty.class);
        // if (mailProperty == null) return null;

        // return mailProperty.getAddress();
        return username;
    }
}
