package io.xygeni.plugins.jenkins.services;

import hudson.util.Secret;
import io.xygeni.plugins.jenkins.configuration.XygeniConfiguration;
import io.xygeni.plugins.jenkins.model.XygeniEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Xygeni platform Jenkins Events api client
 * @author Victor de la Rosa
 */
public class XygeniApiClient {

    private static XygeniApiClient client;

    private final String url;
    private final Secret tokenSecret;

    private static final Logger logger = Logger.getLogger(XygeniApiClient.class.getName());

    public static XygeniApiClient getInstance() {
        if (client != null) return client;

        XygeniConfiguration descriptor = XygeniConfiguration.get();

        if (descriptor.getXygeniTokenSecretId() == null || descriptor.getXygeniUrl() == null) return null;

        client = new XygeniApiClient(descriptor.getXygeniUrl(), descriptor.getXygeniToken());

        return client;
    }

    public static XygeniApiClient getInstance(String xygeniUrl, Secret xygeniTokenSecret) {

        if (xygeniUrl == null || xygeniTokenSecret == null) return null;

        return new XygeniApiClient(xygeniUrl, xygeniTokenSecret);
    }

    private XygeniApiClient(String url, Secret tokenSecret) {
        this.url = url;
        this.tokenSecret = tokenSecret;
    }

    public void sendEvent(XygeniEvent event) {

        try {
            post(
                    event.toJson().toString(),
                    url + "/jenkins/event",
                    tokenSecret == null ? null : tokenSecret.getPlainText());
        } catch (IOException e) {
            logger.log(Level.WARNING, "[XygeniApiClient] sendEvent error " + e.getMessage());
            logger.log(Level.FINEST, "[XygeniApiClient] sendEvent error:", e);
        }
    }

    public boolean validateXygeniPing() {
        try {
            return get(url + "/ping");

        } catch (IOException e) {
            logger.log(Level.WARNING, "[XygeniApiClient] validateXygeniPing error " + e.getMessage());
            logger.log(Level.FINEST, "[XygeniApiClient] validateXygeniPing error:", e);
            return false;
        }
    }

    public boolean validateTokenConnection() {
        try {
            return post("", url + "/jenkins/check", tokenSecret.getPlainText());

        } catch (IOException e) {
            logger.log(Level.WARNING, "[XygeniApiClient] validateTokenConnection error " + e.getMessage());
            logger.log(Level.FINEST, "[XygeniApiClient] validateTokenConnection error.", e);
            return false;
        }
    }

    private boolean get(String url) throws IOException {

        logger.finest("[XygeniApiClient] Sending get: " + url);

        final HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/json");

        try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(httpGet)) {

            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) return true;
            logger.fine("[XygeniApiClient] HttpGet error: " + statusCode + " requesting: " + url);
        }
        return false;
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
            logger.fine("[XygeniApiClient] HttpPost error: " + statusCode + " requesting: " + url);
        }
        return false;
    }
}
