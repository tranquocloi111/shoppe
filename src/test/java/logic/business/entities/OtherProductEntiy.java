package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OtherProductEntiy {

    public String productCode;
    public String type;
    public String description;
    public String startDate;
    public String endDate;
    public String charge;


    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public static List<HashMap<String, String>> dataForOtherProduct(Date startDate, Date endDate) {
        List<HashMap<String, String>> listOtherProduct = new ArrayList<>();
        HashMap<String, String> otherProduct1 = new HashMap<String, String>();
        otherProduct1.put("Start Date", Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));

        listOtherProduct.add(otherProduct1);
        listOtherProduct.add(otherProduct1);
        listOtherProduct.add(otherProduct1);

        return listOtherProduct;
    }

    public static List<HashMap<String, String>> dataForOtherProductOldFPBundle(Date startDate) {
        List<HashMap<String, String>> listOtherProduct = new ArrayList<>();

        HashMap<String, String> otherProduct1 = new HashMap<String, String>();
        otherProduct1.put("Product Code", "BUNDLER - [250MB-FDATA-0-FC]");
        otherProduct1.put("Type", "Bundle");
        otherProduct1.put("Description", "Discount Bundle Recurring - [Family perk - 250MB per month]");
        otherProduct1.put("Start Date", Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT));
        otherProduct1.put("Charge", "£0.00");

        HashMap<String, String> otherProduct2 = new HashMap<String, String>();
        otherProduct1.put("Product Code", "BUNDLER - [500MB-FDATA-0-FC-4G]");
        otherProduct1.put("Type", "Bundle");
        otherProduct1.put("Description", "Discount Bundle Recurring - [Family perk - 500MB per month - 4G]");
        otherProduct1.put("Start Date", Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", "");
        otherProduct1.put("Charge", "£0.00");

        listOtherProduct.add(otherProduct1);
        listOtherProduct.add(otherProduct2);

        return listOtherProduct;
    }

    public static List<HashMap<String, String>> dataForOtherProductHTCWILDFIRE() {
        List<HashMap<String, String>> listOtherProduct = new ArrayList<>();

        HashMap<String, String> otherProduct1 = new HashMap<String, String>();
        otherProduct1.put("Product Code", "HTC-WILDFIRE-XXX-60");
        otherProduct1.put("Type", "Device");
        otherProduct1.put("Description", "HTC Wildfire");
        otherProduct1.put("Start Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", "");
        otherProduct1.put("Charge", "£60.00");

        HashMap<String, String> otherProduct2 = new HashMap<String, String>();
        otherProduct1.put("Product Code", "FLEXCAP - [02000-SB-A]");
        otherProduct1.put("Type", "Bundle");
        otherProduct1.put("Description", "Flexible Cap - £20 - [£20 safety buffer]");
        otherProduct1.put("Start Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", "");
        otherProduct1.put("Charge", "£0.00");

        HashMap<String, String> otherProduct3 = new HashMap<String, String>();
        otherProduct1.put("Product Code", "BUNDLER - [500MB-DATA-500-FC]");
        otherProduct1.put("Type", "Bundle");
        otherProduct1.put("Description", "Discount Bundle Recurring - [Monthly 500MB data allowance]");
        otherProduct1.put("Start Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", "");
        otherProduct1.put("Charge", "£5.00");

        listOtherProduct.add(otherProduct1);
        listOtherProduct.add(otherProduct2);
        listOtherProduct.add(otherProduct3);

        return listOtherProduct;
    }

    public static HashMap<String, String> dataBundlerForOtherProductForFC(Date newStartDate) {
        HashMap<String, String> otherProduct1 = new HashMap<String, String>();
        otherProduct1.put("Product Code", "BUNDLER - [500-FONMIN-0-FC]");
        otherProduct1.put("Type", "Bundle");
        otherProduct1.put("Description", "Discount Bundle Recurring - [Family perk - 500 Tesco Mobile only minutes per month]");
        otherProduct1.put("Start Date", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", "");
        otherProduct1.put("Charge", "£0.00");


        return otherProduct1;
    }

    public static HashMap<String, String> dataBundlerForOtherProductForNC(Date newStartDate) {
        HashMap<String, String> otherProduct1 = new HashMap<String, String>();
        otherProduct1.put("Product Code", "BUNDLER - [500-FONMIN-0-FC]");
        otherProduct1.put("Type", "Bundle");
        otherProduct1.put("Description", "Discount Bundle Recurring - [Monthly Family perk - 500 Tesco Mobile only minutes (Capped)]");
        otherProduct1.put("Start Date", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", "");
        otherProduct1.put("Charge", "£0.00");


        return otherProduct1;
    }

}
