package ru.cubesolutions.evam.sendsayaction;

import org.apache.log4j.Logger;
import ru.cubesolutions.evam.utils.ResourcesApi;

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

    public AutoLoginResourcesApi(Config config) {
        this.config = config;
        this.login(config.getUrlBaseSendsay(),
                config.getSendsayCommonLogin(),
                config.getSendsayPrivateLogin(),
                config.getSendsayPassword());
    }

    public String sendPost(String url, String data) {
        String response = ResourcesApi.sendPost(url,
                packRequestParams(addSessionToData(data, this.session)),
                APPLICATION_FORM_TYPE);
        checkNotNullResponse(response);
        if (!response.contains("error/auth/failed")) {
            return response;
        }
        login(config.getUrlBaseSendsay(),
                config.getSendsayCommonLogin(),
                config.getSendsayPrivateLogin(),
                config.getSendsayPassword());
        return ResourcesApi.sendPost(url,
                packRequestParams(addSessionToData(data, this.session)),
                APPLICATION_FORM_TYPE);
    }

    private synchronized void login(String urlBaseSendsay, String login, String subLogin, String password) {
        String request = String.format(loginJsonPart(), login, subLogin, password);
        String response = ResourcesApi.sendPost(urlBaseSendsay, packRequestParams(request), APPLICATION_FORM_TYPE);
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

    public static void checkNotNullResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new RuntimeException("response is null");
        }
    }

}
