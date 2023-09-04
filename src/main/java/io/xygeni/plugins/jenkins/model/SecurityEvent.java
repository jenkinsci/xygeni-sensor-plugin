package io.xygeni.plugins.jenkins.model;

import java.util.List;

public class SecurityEvent extends XygeniEvent {

    public static final String TYPE_CLASS = "securityEvent";

    // event type
    private final Action action;

    public enum Action {
        authenticated,
        created,
        failedToLogin
    }

    public SecurityEvent(Action action) {
        this.action = action;
    }

    public static SecurityEvent from(String userName, String email, Action action) {
        SecurityEvent securityEvent = new SecurityEvent(action);
        securityEvent.setProperty("username", userName);
        if (email != null) securityEvent.setProperty("email", email);

        return securityEvent;
    }

    public static SecurityEvent from(String userName, List<String> grantedAuthorities, Action action) {
        SecurityEvent securityEvent = new SecurityEvent(action);
        securityEvent.setProperty("username", userName);
        securityEvent.setProperty("grantedAuthorities", String.join(",", grantedAuthorities));

        return securityEvent;
    }

    @Override
    protected String getType() {
        return TYPE_CLASS;
    }

    @Override
    protected String getAction() {
        return this.action.name();
    }
}
