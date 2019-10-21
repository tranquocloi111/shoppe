package logic.business.entities;

//import javafx.util.Pair;
import java.util.AbstractMap;
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

    public static List<HashMap<String, String>> dataForEventServiceOrderCreated() {
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

    public static HashMap<String, String> dataForEventServiceOrder(String description, String status) {
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Description", description);
        event.put("Status", status);

        return event;
    }

    public static HashMap<String, String> dataForEventChangePassword(String description, String status, String username) {
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Description", description);
        event.put("Status", status);
        event.put("Username", username);

        return event;
    }

    public static AbstractMap.SimpleEntry<String, String> setEvents(String description, String status) {
        return new AbstractMap.SimpleEntry<String, String>(description, status);
    }

    public static List<HashMap<String, String>> dataForEventChangeBundleImmediate() {
        String[][] eventData = new String[][]{
                {"Service Order created", "Open Service Order"},
                {"SMS Request Completed", "Completed Task"},
                {"Remove offer on network", "In Progress"},
                {"Refill Amount: £37.50 - Completed", "Completed Task"},
                {"Bonus Money reset to zero", "In Progress"},
                {"PPB: AddSubscription: Request completed", "Completed Task"},
                {"Service Order Completed", "Completed Task"},
        };

        List<HashMap<String, String>> listEventChangeBundle = new ArrayList<>();

        for (int i = 0; i < eventData.length; i++) {
            HashMap<String, String> event = new HashMap<String, String>();
            event.put("Description", eventData[i][0]);
            event.put("Status", eventData[i][1]);
            listEventChangeBundle.add(event);
        }
        return listEventChangeBundle;
    }

    public static List<HashMap<String, String>> dataForEventServiceOrderCompleted() {
        List<HashMap<String, String>> listEventChangeBundle = new ArrayList<>();
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Description", "Service Order Completed");
        event.put("Status", "Completed Task");
        listEventChangeBundle.add(event);

        return listEventChangeBundle;
    }

}
