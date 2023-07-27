package io.xygeni.plugins.jenkins.model;

import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.model.labels.LabelAtom;
import io.xygeni.plugins.jenkins.util.UserUtil;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ComputerEvent implements XygeniEvent {

  private static final Logger logger = Logger.getLogger(ComputerEvent.class.getName());

  public enum Type {
    online,
    offline,
    temporaryOnline,
    temporaryOffline,
    launchFailure

  }

  private String userId;
  private String nodeName;
  private Set<String> labels;
  private Type type;

  public static ComputerEvent from(Computer computer, Type type) {

    ComputerEvent computerEvent = new ComputerEvent();
    computerEvent.setType(type);
    computerEvent.setUserId(UserUtil.getUserId());
    computerEvent.setNodeName(getNodeName(computer));
    computerEvent.setLabels(getComputerLabels(computer));

    return computerEvent;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("type", type);
    json.put("userId", userId);
    json.put("nodeName", nodeName);
    json.put("labels", labels);
    return json;
  }

  public void setLabels(Set<String> labels) { this.labels = labels; }

  public void setNodeName(String nodeName) { this.nodeName = nodeName; }
  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setType(Type type) { this.type = type; }


  private static Set<String> getComputerLabels(Computer computer) {
    Set<String> labels = new HashSet<>();

    if(computer.getNode() == null) return labels;

    for (LabelAtom label: computer.getNode().getAssignedLabels()) {
      labels.add(label.getName());
    }

    return labels;
  }

  private static String getNodeName(Computer computer){
    if(computer == null){
      return "unknown";
    }
    if (computer instanceof Jenkins.MasterComputer) {
      return "master";
    } else {
      return computer.getName();
    }
  }
}
