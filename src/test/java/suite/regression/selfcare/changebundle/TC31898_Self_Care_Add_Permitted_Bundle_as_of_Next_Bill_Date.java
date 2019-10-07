package suite.regression.selfcare.changebundle;

import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.List;

public class TC31898_Self_Care_Add_Permitted_Bundle_as_of_Next_Bill_Date extends BaseTest {


   // @Test(enabled = true, description = "TC31898 SelfCare add permitted bundle as of next bill date", groups = "SelfCare")
    public void TC31898_Self_Care_Add_Permitted_Bundle_as_of_Next_Bill_Date() {
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        test.get().info("Step 1 : Create a customer with 2 NC subscription");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String sub = owsActions.serviceRef;
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        Date newStartDate = TimeStamp.TodayMinus10Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7 : Verify customer data is updated ");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 8 : Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 1");
        String serviceRefOf2stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 2");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " FC Mobile 1");
        String discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(newStartDate, discountGroupCodeOfMobileRef1);

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " FC Mobile 2");
        String discountGroupCodeOfMobileRef2 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(newStartDate, discountGroupCodeOfMobileRef2);

        test.get().info("Step 9 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 11 : Click add or change a bundle button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").clickAddOrChangeABundleButton();

        test.get().info("Step 12 : Click add or change a bundle page is displayed");
        SelfCareTestBase.page().verifyMonthlyBundlesAddChangeOrRemovePageDisplayed();
    }

    private void verifyMonthlyBundleAddChangeOrRemovePageResultIsCorrect() {

    }

    private void verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(Date newStartDate, String discountGroupCode) {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(8, discountBundles.size());

        verifyFCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TMT5K");
    }

    private static void verifyDiscountBundlesForNewNCTariff(List<DiscountBundleEntity> allDiscountBundles) {
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByBundleCode(allDiscountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayMinus16DaysAdd2Months(), "500-FONMIN-0-FC", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByBundleCode(allDiscountBundles, "NC", TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.TodayMinus16DaysAdd2Months(), "500-FONMIN-0-FC", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByBundleCode(allDiscountBundles, "NC", TimeStamp.TodayMinus15DaysAdd2Months(), TimeStamp.TodayMinus16DaysAdd3Months(), "500-FONMIN-0-FC", "ACTIVE"));
    }

}
