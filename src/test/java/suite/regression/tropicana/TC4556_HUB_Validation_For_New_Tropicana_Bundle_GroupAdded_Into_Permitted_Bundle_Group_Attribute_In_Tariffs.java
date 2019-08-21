package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;

import java.sql.Date;

public class TC4556_HUB_Validation_For_New_Tropicana_Bundle_GroupAdded_Into_Permitted_Bundle_Group_Attribute_In_Tariffs extends BaseTest {

    String customerNumber;
    Date newStartDate;
    String mpnOf1stSubscription;

    @Test(enabled = true, description = "HUB - Validation for new Tropicana Bundle Group added into Permitted Bundle Group attribute in Tariffs.", groups = "Tropicana")
    public void TC4556_HUB_Validation_For_New_Tropicana_Bundle_GroupAdded_Into_Permitted_Bundle_Group_Attribute_In_Tariffs() {
        test.get().info("Step 1 : The new Bonus bundles are created under Bonus Bundle Groups on HubVB.");
        Assert.assertTrue(CommonActions.isBonusBundleExisting());


    }

    private  void  verifyExistingOfBonusBundle(){
        CommonActions.getAllBundlesGroupByTariff("");
    }
}
