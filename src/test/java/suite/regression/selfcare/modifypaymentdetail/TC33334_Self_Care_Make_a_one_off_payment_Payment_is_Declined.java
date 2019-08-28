package suite.regression.selfcare.modifypaymentdetail;

import logic.business.entities.CardDetailsEntity;
import logic.business.ws.ows.OWSActions;
import logic.pages.selfcare.MakeAOneOffPaymentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.List;

public class TC33334_Self_Care_Make_a_one_off_payment_Payment_is_Declined extends BaseTest {

    @Test(enabled = true, description = "TC33334 self care make a one off payment, payment is declined", groups = "SelfCare")
    public void TC33334_Self_Care_Make_a_one_off_payment_Payment_is_Declined() {

        test.get().info("Step 1 : create an online cc customer ");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_DD_customer_with_FC_2_bundles_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 2 :Login in to self care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 3 : access the make a one off payment page");
        SelfCareTestBase.page().clickMakeAOneOfPayment();
        SelfCareTestBase.page().verifyMakeAOneOffPayment();

        test.get().info("Step 4 : input the payment detail and click continue");
        String paymentAmount = "12.34";
        CardDetailsEntity cardDetails = new CardDetailsEntity();
        cardDetails.setCardType("MasterCard");
        cardDetails.setCardNumber("5105105105105100");
        cardDetails.setCardSecurityCode("333");
        cardDetails.setCardExpiryMonth("12");
        cardDetails.setCardExpiryYear(TimeStamp.TodayPlus1Year().toString().substring(2, 4));

        MakeAOneOffPaymentPage.CardDetailSection.getInstance().inputCardDetail(cardDetails);
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().enterPaymentAmount(paymentAmount);
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().clickSubmitBtn();

        test.get().info("Step 5 : verify the error message in my personal information page");
        String message = "The payment card you supplied could not be authenticated. Please check all card and address details before retrying.";
        List<String> errorMssg= SelfCareTestBase.page().errorMessageStack();
        Assert.assertEquals(errorMssg.get(0),message);


    }


}
