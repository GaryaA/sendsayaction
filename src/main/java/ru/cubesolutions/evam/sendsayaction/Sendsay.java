package ru.cubesolutions.evam.sendsayaction;

import com.intellica.evam.sdk.outputaction.AbstractOutputAction;
import com.intellica.evam.sdk.outputaction.IOMParameter;
import com.intellica.evam.sdk.outputaction.OutputActionContext;
import com.intellica.evam.sdk.outputaction.OutputActionParameterContext;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Garya on 24.10.2017.
 */
public class Sendsay extends AbstractOutputAction {

    private final static Logger log = Logger.getLogger(Sendsay.class);

    private SendsayApi sendsayApi;

    @Override
    public synchronized void init() {
        isInited = false;
        log.debug("init");
        try (InputStream input = new FileInputStream("./conf/sendsay.properties")) {
            Properties props = new Properties();
            props.load(input);
            initSendsayApi(props);
        } catch (Throwable t) {
            log.error("failed to init Sendsay action", t);
            isInited = false;
            return;
        }
        isInited = true;
    }

    @Override
    public int execute(OutputActionContext outputActionContext) throws Exception {
        Set<String> paramNames = outputActionContext.getParameterNames();
        if (paramNames != null) {
            for (String paramName : paramNames) {
                log.debug("exists param with name: " + paramName);
            }
        }
        String email = (String) outputActionContext.getParameter("email");
        log.debug("email is " + email);
        String subject = (String) outputActionContext.getParameter("subject");
        log.debug("subject is " + subject);
        String templateId = (String) outputActionContext.getParameter("templateId");
        log.debug("templateId is " + templateId);
        Map<String, String> mapValues = new HashMap<>();
        for (String paramName : paramNames) {
            if (Arrays.asList("email", "subject", "templateId").contains(paramName)) {
                continue;
            }
            Map<String, String> catalogVars = sendsayApi.findGlobalParams();
            Map<String, String> catalogVarsReversed = new HashMap<>();
            Set<String> keySet = catalogVars.keySet();
            for (String key : keySet) {
                catalogVarsReversed.put(catalogVars.get(key), key);
            }
            String usableVarName = catalogVarsReversed.get(paramName);
            mapValues.put(usableVarName == null ? paramName : usableVarName, (String) outputActionContext.getParameter(paramName));
        }
        log.debug("extra json part: " + SendsayJsonHelper.extraJsonPart(mapValues));
        this.sendsayApi.sendEmail(subject, email, templateId, mapValues);

        return 0;
    }

    @Override
    public final String getActionInputString() {
        return "Введите id шаблона sendsay и нажмите Evaluate";
    }

    @Override
    protected ArrayList<IOMParameter> getParameters() {
        ArrayList<IOMParameter> result = new ArrayList<>();
        result.add(new IOMParameter("email", "email"));
        result.add(new IOMParameter("subject", "Тема"));
        result.add(new IOMParameter("templateId", "id шаблона"));
        return result;
    }


    @Override
    public ArrayList<IOMParameter> getParameters(OutputActionParameterContext parameterContext) {
        ArrayList<IOMParameter> paramList = new ArrayList<>();
        paramList.add(new IOMParameter("email", "email"));
        paramList.add(new IOMParameter("subject", "Тема"));
        paramList.add(new IOMParameter("templateId", "id шаблона"));
        log.debug("Action input string:" + parameterContext.getActionInputString());
        String templateIdStr = parameterContext.getActionInputString();
        if (sendsayApi == null) {
            try (InputStream input = new FileInputStream("./conf/sendsay.properties")) {
                Properties props = new Properties();
                props.load(input);
                initSendsayApi(props);
            } catch (Throwable t) {
                log.error("failed to init Sendsay action", t);
            }
        }
        List<String> paramNames = sendsayApi.paramNamesTemplate(templateIdStr);
        if (paramNames == null || paramNames.isEmpty()) {
            return paramList;
        }
        for (String paramName : paramNames) {
            log.debug("Param name:" + paramName);
            if (paramName.contains("anketa.member")) {
                continue;
            }
            Map<String, String> catalogVars = sendsayApi.findGlobalParams();
            catalogVars.forEach((k, v) -> System.out.println(k + ": " + v));
            String presentableVarName = catalogVars.get(paramName);
            paramList.add(new IOMParameter(presentableVarName == null ? paramName : presentableVarName,
                    presentableVarName == null ? paramName : presentableVarName));
        }
        return paramList;
    }

    @Override
    public String getVersion() {
        return "v1.0";
    }

    private void initSendsayApi(Properties props) {
        String urlBaseSendsay = props.getProperty("url-base-sendsay");
        String sendsayCommonLogin = props.getProperty("sendsay-common-login");
        String sendsayPrivateLogin = props.getProperty("sendsay-private-login");
        String sendsayPassword = props.getProperty("sendsay-password");
        String sendsayFromName = props.getProperty("sendsay-from-name");
        String fromEmail = props.getProperty("sendsay-from-email");
        Config config = new Config(urlBaseSendsay, sendsayCommonLogin, sendsayPrivateLogin, sendsayPassword, sendsayFromName, fromEmail);
        this.sendsayApi = new SendsayApi(config);
    }
}
