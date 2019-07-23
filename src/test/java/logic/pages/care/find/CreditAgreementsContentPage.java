package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class CreditAgreementsContentPage extends BasePage {

    public static class CreditAgreementsGridPage extends CreditAgreementsContentPage{

        private static final String subscription = "Subscription";
        private static final String description = "Description";
        private static final String startDate = "Start Date";
        private static final String secondPhaseStartDate = "Second Phase Start Date";
        private static final String endDate = "End Date";
        private static final String balance = "Balance";
        private static final String status = "Status";

        private static CreditAgreementsGridPage instance = new CreditAgreementsGridPage();
        public static CreditAgreementsGridPage getInstance(){
            return new CreditAgreementsGridPage();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Credit Agreements')]/../../..//following-sibling::div[1]//table[.//div[contains(@class,'GroupPanel')]]")
        WebElement creditAgreementsTable;
        TableControlBase table = new TableControlBase(creditAgreementsTable);

        public void clickExpandButtonOfCABySubscription(String value){
            click(table.getRowByContainsColumnNameAndCellValue(subscription, value).findElement(By.tagName("img")));
        }

        public CADetailClass getCADetailBySubscription(String value){
            return new CADetailClass(table.getRowByContainsColumnNameAndCellValue(subscription,value));
        }

        public List<WebElement> getCreditAgreement(HashMap<String,String> creditAgreements) {
            return table.findRowsByColumns(creditAgreements);
        }

        public int getRowOfCreditAgreementsGrid(){
            return table.getRowsCount() - 4;
        }


        public class CADetailClass extends CreditAgreementsGridPage {
            WebElement parent;
            WebElement detailTable;
            public CADetailClass(WebElement parent){
                this.parent = parent;
                detailTable = parent.findElement(By.xpath(".//following-sibling::tr[1]")).findElement(By.tagName("table"));
            }

            public String agreementNumber(){
               return findValueByLabel(detailTable, "Agreement Number");
            }

            public String otherPayments(){
                return findValueByLabel(detailTable, "Other Payments");
            }

            public String dealType(){
                return findValueByLabel(detailTable, "Deal Type");
            }

            public String ccaPhases() {
                return findValueByLabel(detailTable, "CCA Phases");
            }

            public String ccaFirstPhaseMonths(){return  findValueByLabel(detailTable, "CCA First Phase Months");}

            public String ccaFirstPhaseMonthlyCharge (){return  findValueByLabel(detailTable, "CCA First Phase Monthly Charge");}

            public String ccaSecondPhaseMonths (){return  findValueByLabel(detailTable, "CCA Second Phase Months");}

            public String ccaSecondPhaseMonthlyCharge(){return findValueByLabel(detailTable, "CCA Second Phase Monthly Charge");}

            public String ccaTotalDurationMonths(){return findValueByLabel(detailTable, "CCA Total Duration Months");}

            public String contractValue(){return  findValueByLabel(detailTable, "Contract Value");}

            public String monthlyChargePaid(){return findValueByLabel(detailTable, "Monthly Charge Paid");}

            public String device(){return findValueByLabel(detailTable, "Device");}

        }


    }


    public static  class CreditAgreementPaymentsGrid extends CreditAgreementsContentPage{
        private static final String date = "Date";
        private static final String details = "Details";
        private static final String subscription = "Subscription";
        private static final String Amount = "Amount";

        private static CreditAgreementPaymentsGrid instance = new CreditAgreementPaymentsGrid();
        public static CreditAgreementPaymentsGrid getInstance(){
            return  new CreditAgreementPaymentsGrid();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Credit Agreement Payments')]/../../..//following-sibling::div[1]//table")
        WebElement creditAgreementPaymentsTable;
        TableControlBase table = new TableControlBase(creditAgreementPaymentsTable);

        public List<WebElement> getCreditAgreementPayments(HashMap<String,String> creditAgreementPayments) {
            return table.findRowsByColumns(creditAgreementPayments);
        }

        public int getNumberOfCreditAgreementPayments(HashMap<String,String> creditAgreementPayments) {
            return table.findRowsByColumns(creditAgreementPayments).size();
        }

        public int getRowOfCreditAgreementPaymentsGrid(){
            return table.getRowsCount();
        }
    }
}
