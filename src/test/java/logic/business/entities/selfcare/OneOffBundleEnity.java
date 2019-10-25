package logic.business.entities.selfcare;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.HashMap;

public class OneOffBundleEnity {

    public static HashMap<String,String> getBundleChargesEnity(String currentBundle,String description, String allowance, String remaining, Date renewsOn, String monthlyCharge)
    {
        HashMap<String, String> currentBundleEnity   = new HashMap<String, String>();
        currentBundleEnity.put("Current Bundles", currentBundle);
        currentBundleEnity.put("", description);
        currentBundleEnity.put("Allowance", allowance);
        currentBundleEnity.put("Remaining", remaining);
        currentBundleEnity.put("Renews on", Parser.parseDateFormate(renewsOn,TimeStamp.DATE_FORMAT_IN_PDF));
        currentBundleEnity.put("Monthly Charge", monthlyCharge);
        return currentBundleEnity;
    }
    public static HashMap<String,String> getAvailableOneOffDataBundleEnity(String description, String allowance, Date expiryOn, String cost)
    {
        HashMap<String, String> enity   = new HashMap<String, String>();
        enity.put("Available One off data bundles", description);
        enity.put("Allowance", allowance);
        enity.put("Expires on", Parser.parseDateFormate(expiryOn,TimeStamp.DATE_FORMAT_IN_PDF));
        enity.put("Cost", cost);
        return enity;
    }
}
