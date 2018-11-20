package logic.business.ws;

import framework.config.Config;

public class OrderingWS {
    private String url;

    public OrderingWS() {
        url = Config.getProp("owsUrl");
    }


}
