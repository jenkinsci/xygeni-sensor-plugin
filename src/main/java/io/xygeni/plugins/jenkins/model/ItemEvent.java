package io.xygeni.plugins.jenkins.model;

import hudson.model.Item;
import io.xygeni.plugins.jenkins.util.UserUtil;
import net.sf.json.JSONObject;

public class ItemEvent implements XygeniEvent {

  public enum Action {
    LOCATION_CHANGED, UPDATED, RENAMED, CREATED, DELETE
  };

  private final Action action;

  private String fullName;
  private String userId;

  public ItemEvent(Action action) {
    this.action = action;
  }

  public static ItemEvent from(Item item, Action action) {

    ItemEvent itemEvent = new ItemEvent(action);
    itemEvent.setFullName(getItemName(item));
    itemEvent.setUserId(UserUtil.getUserId());

    return itemEvent;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("userId", userId);
    json.put("fullName", fullName);
    json.put("action", action.name());
    return json;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  private static String getItemName(Item item) {
    if (item == null) {
      return "unknown";
    }
    return item.getName();
  }

}
