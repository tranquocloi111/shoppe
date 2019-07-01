package framework.utils;

import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonReader {

    public static JSONObject readJson(String filename) {
        try {
            FileReader reader = new FileReader(filename);
            JSONParser jsonParser = new JSONParser();
            return (JSONObject) jsonParser.parse(reader);
        }catch (Exception ex){
            Log.error(ex.getMessage());
        }
        return null;
    }


    public static List<JSONObject> getListServiceOrder(String jsonFile){
        JSONObject jsonObject = readJson(jsonFile);
        JSONArray object = (JSONArray) jsonObject.get("items");
        List<JSONObject> lis = new ArrayList<JSONObject>();
        for (int i = 0; i < object.size() ; i++) {
            lis.add((JSONObject) ((JSONArray) object).get(i));
        }

        return lis;
    }

    public static void main(String[] args) throws Exception {
        JSONObject jsonObject = readJson("C:\\GIT\\TM\\hub_testauto\\src\\test\\resources\\json\\serviceOrder.json");
        JSONArray items = (JSONArray) jsonObject.get("items");
        List<HashMap<String, String>> ser = new ArrayList<>();
        HashMap<String, String> dsd = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            dsd.put("Date", String.valueOf(((JSONObject)items.get(i)).get("Date")));
            dsd.put("Status", String.valueOf(((JSONObject)items.get(i)).get("Status")));
            dsd.put("Type", String.valueOf(((JSONObject)items.get(i)).get("Type")));

            ser.add(dsd);
        }
        System.out.println(ser);
    }

}
