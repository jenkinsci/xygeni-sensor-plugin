package io.xygeni.plugins.jenkins.model;

import hudson.XmlFile;
import hudson.model.Saveable;
import io.xygeni.plugins.jenkins.util.UserUtil;
import net.sf.json.JSONObject;

public class ConfigEvent implements XygeniEvent {

  private String implementation;
  private String userId;
  private String fileName;

  public static ConfigEvent from(Saveable saveableConfig, XmlFile file) {
    ConfigEvent configEvent = new ConfigEvent();
    configEvent.setImplementation(saveableConfig.getClass().getSuperclass().toString());
    configEvent.setUserId(UserUtil.getUserId());
    configEvent.setFileName(UserUtil.getUserId());

    return configEvent;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("userId", userId);
    json.put("fileName", fileName);
    json.put("implementation", implementation);
    return json;
  }

  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }


  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}
