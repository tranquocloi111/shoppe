package logic.business.ws;

import framework.config.Config;
import framework.utils.Xml;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseWs {
    protected String url;
    protected Map<String, String> commonModMap;
    protected Xml request;
    protected Xml response;

    protected BaseWs() {
        this.url = Config.getProp("owsUrl");
        this.commonModMap = new HashMap<>();
    }
}

