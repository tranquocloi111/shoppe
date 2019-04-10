package logic.helper;

import javafx.util.Pair;
import logic.utils.DB;
import logic.utils.Log;
import logic.utils.RandomCharacter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import static logic.utils.DB.retriveDataInResultSet;

public class BillingHelper {

    public static Pair<Integer, String> tempBillingGroupHeader;

    private static BillingHelper billingHelper = new BillingHelper();

    public static BillingHelper page(){
        if (billingHelper == null)
            return new BillingHelper();

        return billingHelper;
    }

    private  void changeExistingBillRunCalendar(int daysAgo, int asAtDateOffset){
        try {
            DB.SetToNonOEDatabase().executeQuery(String.format("update billruncalendar set asatdate=trunc(SYSDATE - %d) where asatdate=trunc(SYSDATE - %d)", daysAgo + 2, daysAgo + Math.abs(asAtDateOffset)));
            DB.SetToNonOEDatabase().executeQuery(String.format("update billruncalendar set rundate=trunc(SYSDATE - %d) where rundate=trunc(SYSDATE - %d)", daysAgo + 2, daysAgo));
        }catch(Exception ex){
            Log.error(ex.getMessage());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private  Pair<Integer, String> createNewBillGroupHeader() throws SQLException {
        String sql = "select periodid from PERIOD where descr='Monthly'";
        BigDecimal periodId = (BigDecimal) retriveDataInResultSet( DB.SetToNonOEDatabase().executeQuery(sql), 1);
        sql = String.format("insert into billinggroup (billinggroupid,descr,bgstatus,periodid,bgtype,psid) values (BILLINGGROUPID.nextval,?,'A',%d,'BILL',200)", Integer.parseInt(String.valueOf(periodId)));

        //Execute Insert Query
        String billingGroupName = "TC - " + RandomCharacter.getRandomNumericString(9);
        HashMap<Integer, Object> formParams = new HashMap<Integer, Object>();
        formParams.put(1, billingGroupName);
        DB.SetToNonOEDatabase().executeNonQuery(sql, formParams);

        //Get BillingId
        sql = "Select billinggroupid,descr from billinggroup where descr = '"+billingGroupName+"' ";
        int billingGroupId =  Integer.parseInt(String.valueOf(retriveDataInResultSet( DB.SetToNonOEDatabase().executeQuery(sql), 1)));

        //Update bill group to PUBLIC
        sql = "update bgproperty set propvalchar = 'PUBLIC' where  propertykey = 'BGACCESS' and billinggroupid = " + billingGroupId;
        DB.SetToNonOEDatabase().executeNonQuery(sql);

        sql = String.format("update bgproperty set propvalchar = '%s' where  propertykey = 'EXTERNDESCR' and billinggroupid = %d", billingGroupName, billingGroupId);
        DB.SetToNonOEDatabase().executeNonQuery(sql);

        return new Pair<Integer, String>(billingGroupId, billingGroupName);
    }

    private  void createBillrunCalendar(int billGroupId, Date runDate, Date asAtDat){
        String sql = String.format("insert into BILLRUNCALENDAR (BILLRUNCALENDARID,BILLINGGROUPID,RUNDATE,ASATDATE,BRCALTYPE) values (BILLRUNCALENDARID.nextval,%d,?,?,'R')", billGroupId);
        HashMap<Integer, Object> formParams = new HashMap<Integer, Object>();
        formParams.put(1, runDate);
        formParams.put(2, asAtDat);
        DB.SetToNonOEDatabase().executeNonQueryDate(sql, formParams);
    }

    public Pair<Integer, String> createNewBillingGroup(int daysAgo, Boolean changeExistingBillingGroups , int asAtDateOffset ){
        try {
            if (changeExistingBillingGroups)
                changeExistingBillRunCalendar(daysAgo, asAtDateOffset);

            Pair<Integer, String> billGroupHeader = createNewBillGroupHeader();
            tempBillingGroupHeader = billGroupHeader;
            LocalDate futureDate = LocalDate.now().plusDays(daysAgo);
            for (int i = 0; i < 4; i++){
                createBillrunCalendar(billGroupHeader.getKey(),Date.valueOf(futureDate.plusMonths(i)), Date.valueOf(futureDate.plusMonths(i).plusDays(asAtDateOffset)));
            }

        }catch(Exception ex){
            Log.error(ex.getMessage());
        }
        return tempBillingGroupHeader;
    }

    public void updateBillGroupPaymentCollectionDate(Date collectionDate, int billingGroupId)
    {
        LocalDate localDate = LocalDate.now().plusDays(10);
        Date date =  Date.valueOf(localDate);;
        if (date.getDate() > 28){
            date = Date.valueOf(localDate.plusDays(28 - date.getDate()));
        }
        String sql = String.format("update BGPROPERTY set propvalnumber= %d where propertykey='BGPCDAY' and billinggroupid= %d ", date.getDate(), billingGroupId);
        DB.SetToNonOEDatabase().executeNonQuery(sql);
    }


    public static void setBillGroupForCustomer(String customerId, int billGroupId){
        int hmbrid = getHmbrid(customerId);
        String sql = String.format("update hmbrproperty set propvalnumber=%d where hmbrid = %d and propertykey='BGRP'", billGroupId, hmbrid);
        DB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static int getHmbrid(String customerId){
        String sql = String.format("select hmbrid from hierarchymbr hm,hierarchy h where h.rootbuid = %s and h.hid = hm.hid and hm.hmbrtype = 'BP'", customerId);
        ResultSet resultSet =  DB.SetToNonOEDatabase().executeQuery(sql);
        int hmbrid = Integer.parseInt(String.valueOf(retriveDataInResultSet(resultSet,1)));
        return hmbrid;
    }


}
