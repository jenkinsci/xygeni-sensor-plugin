package io.xygeni.plugins.jenkins.model;

import hudson.EnvVars;
import hudson.XmlFile;
import hudson.model.Run;
import hudson.model.Saveable;
import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import io.xygeni.plugins.jenkins.util.UserUtil;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SCMEvent implements XygeniEvent {

  private static final Logger LOGGER = Logger.getLogger(SCMEvent.class.getName());

  private String userId;
  private String buildNumber;
  private String jobName;
  private String buildUrl;
  private String branch;
  private String gitUrl;
  private String gitCommit;
  private String nodeName;


  public static SCMEvent from(Run run, TaskListener listener) {

    if (run == null) {
      return null;
    }

    EnvVars envVars;
    try {
      envVars = getEnvVars(run, listener);
    } catch (IOException | InterruptedException exp) {
      LOGGER.log(Level.SEVERE,"[SCMEvent] Failed to read env vars", exp);
      return null;
    }


    SCMEvent scmEvent = new SCMEvent();
    scmEvent.setUserId(UserUtil.getUserId());
    scmEvent.setBuildNumber(String.valueOf(run.getNumber()));
    scmEvent.setBuildUrl(envVars.get("BUILD_URL"));
    scmEvent.setNodeName(envVars.get("NODE_NAME"));

    String jobName = getJobName(run);
    scmEvent.setJobName(jobName == null ? "unknown" : jobName);

    String branchName = envVars.get("GIT_BRANCH"); // remote branch from Git plugin
    scmEvent.setBranch(branchName == null ? "unknown" : branchName);

    String gitCommit = envVars.get("GIT_COMMIT"); // commit-sha from Git plugin
    scmEvent.setGitCommit(gitCommit == null ? "unknown" : gitCommit);

    String gitUrl = envVars.get("GIT_URL"); // first repo url from Git plugin
    scmEvent.setGitUrl(gitUrl == null ? "unknown" : gitUrl);

    return scmEvent;
  }



  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("userId", userId);
    json.put("branch", branch);
    json.put("gitUrl", gitUrl);
    json.put("gitCommit", gitCommit);
    json.put("buildUrl", buildUrl);
    json.put("buildNumber", buildNumber);
    json.put("jobName", jobName);
    json.put("nodeName", nodeName);
    return json;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setBranch(String branch) { this.branch = branch; }
  public void setJobName(String jobName) { this.jobName = jobName; }
  public void setBuildNumber(String buildNumber) {this.buildNumber = buildNumber; }
  public void setBuildUrl(String buildUrl) { this.buildUrl = buildUrl; }
  public void setNodeName(String nodeName) { this.nodeName = nodeName; }
  public void setGitUrl(String gitUrl) { this.gitUrl = gitUrl; }
  public void setGitCommit(String gitCommit) { this.gitCommit = gitCommit; }


  private static EnvVars getEnvVars(Run run, TaskListener listener) throws IOException, InterruptedException {

    if(listener != null){
      return run.getEnvironment(listener);
    }else{
      return run.getEnvironment(new LogTaskListener(LOGGER, Level.INFO));
    }
  }

  private static String getJobName(Run run) {

    String jobName = null;
    try {
      jobName = run.getParent().getFullName();
    } catch(NullPointerException ignored) {}

    if (jobName == null) {
      return null;
    }
    return jobName.replaceAll("»", "/").replaceAll(" ", "");
  }


}
