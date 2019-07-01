package logic.business.ws;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.Xml;
import logic.business.db.OracleDB;
import logic.business.helper.MiscHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseWs {
    protected String owsUrl;
    protected String swsUrl;
    protected Map<String, String> commonModMap;
    protected Xml request;
    protected Xml response;

    protected BaseWs() {
        this.owsUrl = Config.getProp("owsUrl");
        this.swsUrl = Config.getProp("swsUrl");
        this.commonModMap = new HashMap<>();
    }

    public void checkAsyncProcessIsCompleted(String orderId) {
        MiscHelper.waitForAsyncProcessComplete(orderId);
    }


}

