package ru.cubesolutions.evam.sendsayaction;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Garya on 05.02.2018.
 */
public class SendsayJsonHelper {

    private final static Logger log = Logger.getLogger(SendsayJsonHelper.class);

    public static String loginJsonPart() {
        return "{" +
                "\"action\" : \"login\"" +
                ",\"login\"  : \"%s\"" +
                ",\"sublogin\"  : \"%s\"" +
                ",\"passwd\" : \"%s\"" +
                "}";
    }

    public static String packRequestParams(String request) {
        try {
            return "apiversion=100&json=1&request=" + URLEncoder.encode(request, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            throw new RuntimeException("can't encode request params ", e);
        }
    }

    public static String findTemplateJsonPart() {
        return "{" +
                "\"action\" : \"infolett.get\"" + ",\"id\"  : \"%s\"" +
                "}";
    }

    public static String findAnketaJsonPart() {
        return "{" +
                "\"action\" : \"anketa.get\"" +
                ",\"id\"  : \"%s\"" +
                "}";
    }

    public static String findAnketsJsonPart() {
        return "{" +
                "\"action\" : \"anketa.list\"" +
                "}";
    }

    public static String sendEmailJsonPart() {
        return "{\n" +
                "  \"action\": \"issue.send\",\n" +
                "  \"sendwhen\": \"now\",\n" +
                "  \"group\": \"personal\",\n" +
                "  \"email\": \"%s\",\n" +
                "  \"letter\": {\n" +
                "    \"subject\": \"%s\",\n" +
                "    \"from.name\": \"%s\",\n" +
                "    \"from.email\": \"%s\",\n" +
                "    \"message\": {" +
                "       \"html\": \"%s\"\n" +
                "     }\n" +
                "  },\n" +
                "   %s\n" +
                "}";
    }

    public static String extraJsonPart(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "\"extra\":{}";
        }
        JSONObject extra = new JSONObject();
        JSONObject root = new JSONObject().put("extra", extra);

        for (String paramName : params.keySet()) {
            String[] paramNameParts = paramName.split("\\.");
            if (paramNameParts.length == 0 || paramNameParts.length == 1) {
                extra.put(paramName, params.get(paramName));
            } else {
                JSONObject tail = findExists(extra, paramNameParts[paramNameParts.length - 2], paramNameParts.length - 1);
                if (tail == null) {
                    tail = new JSONObject();
                }
                tail.put(paramNameParts[paramNameParts.length - 1], params.get(paramName));
                for (int i = paramNameParts.length - 2; i >= 1; i--) {
                    JSONObject exists = findExists(extra, paramNameParts[i - 1], i);
                    JSONObject current = new JSONObject(tail.toString());
                    if (exists == null) {
                        tail = new JSONObject();
                        tail.put(paramNameParts[i], current);
                    } else {
                        exists.remove(paramNameParts[i]);
                        exists.put(paramNameParts[i], current);
                        tail = exists;
                    }

                }
                extra.put(paramNameParts[0], tail);
            }
        }

        return root.toString().substring(1, root.toString().length() - 1) + ",";
    }

    private static JSONObject findExists(JSONObject root, String key, int level) {
        return findExists(root, key, level, 1);
    }

    private static JSONObject findExists(JSONObject root, String key, int level, int currentLevel) {
        JSONObject result = null;
        if (root.has(key) && level == currentLevel) {
            Object obj = root.get(key);
            if (obj instanceof JSONObject) {
                result = root.getJSONObject(key);
            }
        } else {
            Iterator<String> keys = root.keys();
            if (keys.hasNext()) {
                String iKey = keys.next();
                Object obj = root.get(iKey);
                if (obj instanceof JSONObject) {
                    result = findExists(((JSONObject) obj), key, level, ++currentLevel);
                }
            }
        }
        return result;
    }
}
