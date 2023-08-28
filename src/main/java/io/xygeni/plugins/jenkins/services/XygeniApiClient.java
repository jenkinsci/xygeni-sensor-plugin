package io.xygeni.plugins.jenkins.services;

import hudson.util.Secret;
import io.xygeni.plugins.jenkins.configuration.XygeniConfiguration;
import io.xygeni.plugins.jenkins.model.XygeniEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class XygeniApiClient {

    private static XygeniApiClient client;

    private final String url;
    private final Secret tokenSecret;

    private static final Logger logger = Logger.getLogger(XygeniApiClient.class.getName());

    public static XygeniApiClient getInstance() {
        if (client != null) return client;

        XygeniConfiguration descriptor = XygeniConfiguration.get();

        if (descriptor.getXygeniTokenSecret() == null) return null;
        if (descriptor.getXygeniUrl() == null) return null;

        client = new XygeniApiClient(descriptor.getXygeniUrl(), descriptor.getXygeniToken());

        return client;
    }

    private XygeniApiClient(String url, Secret tokenSecret) {
        this.url = url;
        this.tokenSecret = tokenSecret;
    }

    public boolean sendEvent(XygeniEvent event) {

        try {
            return post(event.toJson().toString(), url, tokenSecret == null ? null : tokenSecret.getPlainText());
        } catch (IOException e) {
            logger.log(Level.WARNING, "[XygeniApiClient] sendEvent error", e);
        }
        return false;
    }

    public boolean validateXygeniPing(String xygeniUrl) {
        try {
            return post(null, xygeniUrl + "/ping", null);

        } catch (IOException e) {
            logger.log(Level.WARNING, "[XygeniApiClient] validateXygeniPing - error.", e);
            return false;
        }
    }

    public boolean validateTokenConnection(String xygeniUrl, Secret tokenSecret) {
        try {
            return post("", xygeniUrl + "/jenkins/check", tokenSecret.getPlainText());

        } catch (IOException e) {
            logger.log(Level.WARNING, "[XygeniApiClient] validateTokenConnection error.", e);
            return false;
        }
    }

    private boolean post(String json, String url, String token) throws IOException {
        if (json == null) {
            logger.fine("[XygeniApiClient] Event null. Abort sending post.");
            return true;
        }

        logger.finest("[XygeniApiClient] Sending post: " + url + " body: " + json);

        final HttpPost httpPost = new HttpPost(url);

        final StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        if (token != null) {
            httpPost.setHeader("Authorization", "Bearer " + token);
        }

        try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(httpPost)) {

            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) return true;
            logger.fine("[XygeniApiClient] Http error: " + statusCode + " requesting: " + url);
        }
        return false;
    }
}
