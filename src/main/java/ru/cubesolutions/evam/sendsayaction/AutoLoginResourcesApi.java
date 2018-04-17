package ru.cubesolutions.evam.sendsayaction;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;

import static ru.cubesolutions.evam.sendsayaction.SendsayJsonHelper.loginJsonPart;
import static ru.cubesolutions.evam.sendsayaction.SendsayJsonHelper.packRequestParams;

/**
 * Created by Garya on 05.02.2018.
 */
public class AutoLoginResourcesApi {

    private final static Logger log = Logger.getLogger(AutoLoginResourcesApi.class);
    private final static String APPLICATION_FORM_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
    private Config config;
    private String session;
    private String proxyUser;
    private String proxyPassword;

    public AutoLoginResourcesApi(Config config, String proxyUser, String proxyPassword) {
        this.config = config;
        this.login(config.getUrlBaseSendsay(),
                config.getSendsayCommonLogin(),
                config.getSendsayPrivateLogin(),
                config.getSendsayPassword(),
                proxyUser,
                proxyPassword);
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
    }

    public String sendPost(String url, String data) {
        String response = sendPost(url,
                packRequestParams(addSessionToData(data, this.session)),
                APPLICATION_FORM_TYPE,
                proxyUser,
                proxyPassword);
        checkNotNullResponse(response);
        if (!response.contains("error/auth/failed")) {
            return response;
        }
        login(config.getUrlBaseSendsay(),
                config.getSendsayCommonLogin(),
                config.getSendsayPrivateLogin(),
                config.getSendsayPassword(),
                proxyUser,
                proxyPassword);
        return sendPost(url,
                packRequestParams(addSessionToData(data, this.session)),
                APPLICATION_FORM_TYPE,
                proxyUser,
                proxyPassword);
    }

    private synchronized void login(String urlBaseSendsay, String login, String subLogin, String password, String proxyUser, String proxyPassword) {
        String request = String.format(loginJsonPart(), login, subLogin, password);
        String response = sendPost(urlBaseSendsay, packRequestParams(request), APPLICATION_FORM_TYPE, proxyUser, proxyPassword);
        if (response.contains("error/auth/failed")) {
            throw new RuntimeException("Auth failed, response: " + response);
        }
        checkNotNullResponse(response);
        String sessionStr = response.substring(response.indexOf("session") + 10);
        this.session = sessionStr.substring(0, sessionStr.indexOf("\""));
        log.debug("login successfully, session: " + this.session);
    }

    private static String addSessionToData(String data, String session) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(data);
        sb.deleteCharAt(sb.lastIndexOf("}")).append(", \"session\":").append("\"").append(session).append("\"").append("}");
        return sb.toString();
    }


    public static String sendPost(String url, String data, String contentType, String authUser, String authPassword) {
        try {
            HttpPost request = new HttpPost(url);
            String auth = "" + authUser + ":" + authPassword;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("UTF-8")));
            String authHeader = "Basic " + new String(encodedAuth);
            if(authUser != null && !authUser.isEmpty()) {
                request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            }
            request.setHeader("Content-type", contentType);
            request.setEntity(new StringEntity(data));

            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(request);
            log.debug("Response code: " + response.getStatusLine().getStatusCode());
            String result = EntityUtils.toString(response.getEntity());
            log.debug("Response: " + result);
            return result;
        } catch (IOException e) {
            log.error("Can't exec request", e);
            throw new RuntimeException(e);
        }
    }

    public static void checkNotNullResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new RuntimeException("response is null");
        }
    }

}
