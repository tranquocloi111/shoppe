package suite.regression;

import logic.business.ws.OrderingWS;
import org.testng.annotations.Test;
import suite.BaseTest;

public class DemoTest extends BaseTest {

    @Test
    public void createOrderTest() {
        OrderingWS ows = new OrderingWS();
       // ows.createOrder();
    }
}
