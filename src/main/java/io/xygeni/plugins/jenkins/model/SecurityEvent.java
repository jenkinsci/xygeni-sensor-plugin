package io.xygeni.plugins.jenkins.model;

import hudson.XmlFile;
import hudson.model.Saveable;
import io.xygeni.plugins.jenkins.util.UserUtil;
import net.sf.json.JSONObject;

public class SecurityEvent implements XygeniEvent {

  private String userId;
  private String userName;
  private Type type;

  public enum Type {
    CREATED,
    AUTHENTICATED
  }

  public static SecurityEvent from(String userName, Type type) {
    SecurityEvent securityEvent = new SecurityEvent();
    securityEvent.setUserId(UserUtil.getUserId());
    securityEvent.setUserName(userName);
    securityEvent.setType(type);


    return securityEvent;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("userId", userId);
    json.put("userName", userName);
    json.put("type", type.name());
    return json;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
  public void setUserName(String userName) {
    this.userName = userName;
  }
  public void setType(Type type) {
    this.type = type;
  }
}
