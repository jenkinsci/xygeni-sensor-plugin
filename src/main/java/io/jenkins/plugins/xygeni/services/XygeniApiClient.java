package io.jenkins.plugins.xygeni.services;

import hudson.util.Secret;
import io.jenkins.plugins.xygeni.configuration.XygeniConfiguration;
import io.jenkins.plugins.xygeni.model.XygeniEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;

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

        String url = descriptor.getXygeniUrl();
        Secret secret = descriptor.getXygeniToken();

        if (url == null || secret == null) return null;

        client = new XygeniApiClient(url, secret);

        logger.info("new XygeniApiClient instantiate with url " + url);

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

    public static void invalidateInstance() {
        client = null;
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
            return post("{}", url + "/jenkins/check", tokenSecret.getPlainText());

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
                ClassicHttpResponse response = client.execute(httpGet, classicHttpResponse -> classicHttpResponse)) {

            final int statusCode = response.getCode();
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
                ClassicHttpResponse response = client.execute(httpPost, classicHttpResponse -> classicHttpResponse)) {

            final int statusCode = response.getCode();
            if (statusCode == HttpStatus.SC_OK) return true;
            logger.fine("[XygeniApiClient] HttpPost error: " + statusCode + " requesting: " + url);
        }
        return false;
    }
}
