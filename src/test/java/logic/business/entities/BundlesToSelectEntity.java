package logic.business.entities;

public class BundlesToSelectEntity {

    public static String [] getFCBundleToSelect(){
        return new String[]{"4G data - 8GB - £20.00 per Month (Recurring)",
                "4G data - 6GB - £17.50 per Month (Recurring)",
                "4G data - 500MB - £5.00 per Month (Recurring)",
                "4G data - 4GB - £15.00 per Month (Recurring)",
                "4G data - 3GB - £12.50 per Month (Recurring)",
                "4G data - 2GB - £10.00 per Month (Recurring)",
                "4G data - 1GB - £7.50 per Month (Recurring)",
                "3G data - 8GB - £17.50 per Month (Recurring)",
                "3G data - 6GB - £15.00 per Month (Recurring)",
                "3G data - 500MB - £2.50 per Month (Recurring)",
                "3G data - 4GB - £12.50 per Month (Recurring)",
                "3G data - 3GB - £10.00 per Month (Recurring)",
                "3G data - 2GB - £7.50 per Month (Recurring)",
                "3G data - 1GB - £5.00 per Month (Recurring)"};
    }

    public static String [] getNCBundleToSelect(){
        return new String[]{"Monthly data bundle - 500MB (Capped) - £5.00 per Month (Recurring)",
                "Monthly data bundle - 1GB (Capped) - £7.50 per Month (Recurring)",};
    }

    public static String [] getSafetyBuffersAToSelect(){
        return new String[]{
                "£2.50 safety buffer",
                "£5 safety buffer",
                "£7.50 safety buffer",
                "£10 safety buffer",
                "£20 safety buffer",
                "£30 safety buffer",
                "£40 safety buffer",
                "£50 High usage limit(No safety buffer)",
//                "£50 High usage limit (No safety buffer)",
                "£100 High usage limit(No safety buffer) (For exceptional use - check process first)",
//                "£100 High usage limit (No safety buffer) (For exceptional use - check process first)",
                "£100 safety buffer (For exceptional use - check process first)",

        };
    }

    public static String[] getMonthlySweetenerBundles(){
        return new String[]{
                "Loyalty Bundle - 100MB 4G Data - £0.00 per Month (Non-Recurring)",
                "Loyalty Bundle - 250MB per month - £0.00 per Month (Recurring)",
                "Loyalty Bundle - 500MB per month - £0.00 per Month (Recurring)",
                "Loyalty Bundle - 100 Mins per month - £0.00 per Month (Recurring)",
                "Loyalty Bundle - 150 Mins per month - £0.00 per Month (Recurring)",
                "Loyalty Bundle - 250 Mins per month - £0.00 per Month (Recurring)"
        };
    }

    public static String[] getStandardDataBundles(){
        return new String[]{
                "Monthly 500MB data allowance - £5.00 per Month (Recurring)",
                "Monthly 1GB data allowance - £7.50 per Month (Recurring)",
                "Monthly 250MB data allowance - 4G - £5.00 per Month (Recurring)"

        };
    }

    public static String[] getBonusBundles(){
        return new String[]{
                "Family perk - 250MB per month - £0.00 per Month (Expiring in 61 day/s). Remaining: 250 MB"
        };
    }

    public static String[] getFamilyPerkBundles(){
        return new String[]{
                "Family perk - 150 Mins per month - £0.00 per Month (Recurring)",
                "Family perk - 250MB per month - £0.00 per Month (Recurring)",
                "Family perk - 500 Tesco Mobile only minutes per month - £0.00 per Month (Recurring)",
                "Family perk - 500MB per month - 4G - £0.00 per Month (Recurring)",
                "Family perk - 250MB per month - 4G - £0.00 per Month (Recurring)"
        };
    }

}
