package ru.cubesolutions.evam.sendsayaction;

/**
 * Created by Garya on 31.07.2017.
 */
public class Config {

    private String urlBaseSendsay;
    private String sendsayCommonLogin;
    private String sendsayPrivateLogin;
    private String sendsayPassword;
    private String sendsayFromName;
    private String sendsayFromEmail;

    public Config(String urlBaseSendsay, String sendsayCommonLogin, String sendsayPrivateLogin, String sendsayPassword, String sendsayFromName, String sendsayFromEmail) {
        this.urlBaseSendsay = urlBaseSendsay;
        this.sendsayCommonLogin = sendsayCommonLogin;
        this.sendsayPrivateLogin = sendsayPrivateLogin;
        this.sendsayPassword = sendsayPassword;
        this.sendsayFromName = sendsayFromName;
        this.sendsayFromEmail = sendsayFromEmail;
    }

    public String getUrlBaseSendsay() {
        return urlBaseSendsay;
    }

    public String getSendsayCommonLogin() {
        return sendsayCommonLogin;
    }

    public String getSendsayPrivateLogin() {
        return sendsayPrivateLogin;
    }

    public String getSendsayPassword() {
        return sendsayPassword;
    }

    public String getSendsayFromName() {
        return sendsayFromName;
    }

    public String getSendsayFromEmail() {
        return sendsayFromEmail;
    }
}
