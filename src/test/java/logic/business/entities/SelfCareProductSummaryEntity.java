package logic.business.entities;

public class SelfCareProductSummaryEntity {

    public static String [] setProductSummary(String productCount, String product, String total){
        return  new String[]{productCount, product, total};
    }
}
