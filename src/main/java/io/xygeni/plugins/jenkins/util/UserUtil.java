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
}
