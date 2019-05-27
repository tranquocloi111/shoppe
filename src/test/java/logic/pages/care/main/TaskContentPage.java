package logic.pages.care.main;

import javafx.util.Pair;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import logic.pages.care.find.SubscriptionDetailsContentPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class TaskContentPage extends BasePage {


    public static class EventsGridClass extends TaskContentPage{
        private static EventsGridClass instance  = new EventsGridClass();
        public static EventsGridClass getInstance() {
            if (instance  == null)
                instance  = new EventsGridClass();
            return instance ;
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Events ')]/../../..//following-sibling::div[1]//td[@class='informationBox']//table")
        WebElement eventsGridPageTable;
        TableControlBase table = new TableControlBase(eventsGridPageTable);

        public List<WebElement> getEvents(Pair<String,String> events){
            return table.findRowsByColumns(events);
        }
    }


}
