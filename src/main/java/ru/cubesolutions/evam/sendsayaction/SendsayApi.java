package ru.cubesolutions.evam.sendsayaction;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.cubesolutions.evam.sendsayaction.AutoLoginResourcesApi.checkNotNullResponse;
import static ru.cubesolutions.evam.sendsayaction.SendsayJsonHelper.*;

/**
 * Created by Garya on 24.10.2017.
 */
public class SendsayApi {

    private final static Logger log = Logger.getLogger(SendsayApi.class);
    private AutoLoginResourcesApi autoLoginResourcesApi;
    private Config config;

    public SendsayApi(Config config, String proxyUser, String proxyPassword) {
        this.config = config;
        this.autoLoginResourcesApi = new AutoLoginResourcesApi(config, proxyUser, proxyPassword);
    }

    public static void main(String[] args) {
        String urlBaseSendsay = "https://api.sendsay.ru/clu206";
        String sendsayCommonLogin = "bankastany";
        String sendsayPrivateLogin = "evam1";
        String sendsayPassword = "Cc+1234569";
        String sendsayFromName = "BankAstana";
        String fromEmail = "24@bankastana.kz";
        Config config = new Config(urlBaseSendsay, sendsayCommonLogin, sendsayPrivateLogin, sendsayPassword, sendsayFromName, fromEmail);
        SendsayApi sendsayApi = new SendsayApi(config, "", "");

        Map<String, String> params = new HashMap<>();
        params.put("sd.sd", "оаоаоа");
        System.out.println(extraJsonPart(params));

        System.out.println(sendsayApi.findTemplate("" + 248));

        System.out.println();

//        sendsayApi.sendEmail("Ваще тема тема тема тема тема тема",
//                "nimgirov@mail.ru",
//                "" + 248,
//                new HashMap<>());
    }

    public void sendEmail(String subject, String email, String draftId, Map<String, String> mapValues) {
        String template = findTemplate(draftId);
        String messageHtml = "";
        Pattern p = Pattern.compile("\"message\":\\{.*?\"\\}");
        Matcher m = p.matcher(template);
        if (m.find()) {
            String messageBody = m.group();
            Pattern p2 = Pattern.compile("\"text\":\".*?\"[\\},]");
            Matcher m2 = p2.matcher(messageBody);
            if (m2.find()) {
                messageHtml = m2.group();
                messageHtml = messageHtml.substring(8, messageHtml.length() - 2);
            }
            Pattern p1 = Pattern.compile("\"html\":\".*?\"[\\},]");
            Matcher m1 = p1.matcher(messageBody);
            if (m1.find()) {
                messageHtml = m1.group();
                messageHtml = messageHtml.substring(8, messageHtml.length() - 2);
            }
        } else {
            throw new RuntimeException("error with getting template, mb this temlate doesn't exists? template: " + template);
        }

        String request = String.format(sendEmailJsonPart(), email, subject, this.config.getSendsayFromName(), this.config.getSendsayFromEmail(), messageHtml, extraJsonPart(mapValues));
        String response = autoLoginResourcesApi.sendPost(this.config.getUrlBaseSendsay(), request);
        log.debug("sending email response: " + response);
    }

    public List<String> paramNamesTemplate(String id) {
        List<String> result = new ArrayList<>();
        String templateStr = findTemplate(id);
        log.debug("templateStr: " + templateStr);
        Pattern p = Pattern.compile("\\[% [^%]* %\\]");
        Matcher m = p.matcher(templateStr);
        while (m.find()) {
            String param = m.group();
            result.add(param.substring(3, param.length() - 3));
        }
        return result;
    }

    public String findTemplate(String id) {
        log.debug("findTemplate, id: " + id);
        String request = String.format(findTemplateJsonPart(), id);
        log.debug("request: " + request);
        String response = autoLoginResourcesApi.sendPost(this.config.getUrlBaseSendsay(), request);
        log.debug("response: " + response);
        checkNotNullResponse(response);
        return response;
    }

    public Map<String, String> findGlobalParams() {
        Map<String, String> globalParams = new HashMap<>();
        List<String> anketsIds = findAnketsIds();
        for (String anketaId : anketsIds) {
            globalParams.putAll(findParamsByAnketaId(anketaId));
        }
        return globalParams;
    }

    private Map<String, String> findParamsByAnketaId(String anketaId) {
        Map<String, String> result = new HashMap<>();
        String request = String.format(findAnketaJsonPart(), anketaId);
        String response = autoLoginResourcesApi.sendPost(this.config.getUrlBaseSendsay(), request);
        Pattern p = Pattern.compile("\"order\":\\[[^\\]]+\\]");
        Matcher m = p.matcher(response);
        List<String> paramIds = new ArrayList<>();
        if (m.find()) {
            String paramIdsStr = m.group();
            paramIdsStr = paramIdsStr.substring(9, paramIdsStr.length() - 1);
            String[] paramIdsArr = paramIdsStr.split(",");
            for (int i = 0; i < paramIdsArr.length; i++) {
                paramIds.add(paramIdsArr[i].substring(1, paramIdsArr[i].length() - 1));
            }
        }

        for (String paramId : paramIds) {
            String paramObjPart = response.substring(response.indexOf(paramId, response.indexOf("quests")));
            Pattern p1 = Pattern.compile("\"name\":\"[^\"]*\"");
            Matcher m1 = p1.matcher(paramObjPart);
            m1.find();
            String paramName = m1.group();
            paramName = paramName.substring(8, paramName.length() - 1);
            result.put("anketa." + anketaId + "." + paramId, paramName);
        }

        return result;
    }

    public List<String> findAnketsIds() {
        List<String> result = new ArrayList<>();
        String anketsResponse = findAnkets();
        Pattern p = Pattern.compile("\"id\":\"[^\"]*\"");
        Matcher m = p.matcher(anketsResponse);
        while (m.find()) {
            String param = m.group();
            result.add(param.substring(6, param.length() - 1));
        }
        return result;
    }

    public String findAnkets() {
        String request = String.format(findAnketsJsonPart());
        return autoLoginResourcesApi.sendPost(this.config.getUrlBaseSendsay(), request);
    }


}
