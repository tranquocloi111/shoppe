package logic.business.entities;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventEntity {
    public String description;
    public String status;
    public String username;
    public String dateTime;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }



    public static List<HashMap<String, String>> dataForEventChangeBundle() {
        List<HashMap<String, String>> listEventChangeBundle = new ArrayList<>();
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Description", "Service Order created");
        event.put("Status", "Open Service Order");
        listEventChangeBundle.add(event);

        return listEventChangeBundle;
    }

    public static List<HashMap<String, String>> dataForEventChangeBundleProvisionWait() {
        List<HashMap<String, String>> listEventChangeBundle = new ArrayList<>();
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Description", "Service Order set to Provision Wait");
        event.put("Status", "Provision Wait");
        listEventChangeBundle.add(event);

        return listEventChangeBundle;
    }

    public static List<HashMap<String, String>> dataForEventChangeBundle(String description, String status) {
        List<HashMap<String, String>> listEventChangeBundle = new ArrayList<>();
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Description", description);
        event.put("Status", status);
        listEventChangeBundle.add(event);

        return listEventChangeBundle;
    }

    public static Pair<String,String> setEvents(String description, String status){
        return new Pair<String,String>(description,status);
    }

}
