package framework.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {
    public static Object readJson(String filename) {
        try {
            FileReader reader = new FileReader(filename);
            JSONParser jsonParser = new JSONParser();
            return jsonParser.parse(reader);
        }catch (Exception ex){
            Log.error(ex.getMessage());
        }
        return null;
    }


    public static List<JSONObject> getListServiceOrder(String jsonFile){
        JSONObject jsonObject = (JSONObject) readJson(jsonFile);
        Object object = jsonObject.get("items") ;
        List<JSONObject> lis = new ArrayList<JSONObject>();
        for (int i = 0; i < ((JSONArray) object).size() ; i++) {
            lis.add((JSONObject) ((JSONArray) object).get(i));
        }

        return lis;
    }

    public static void main(String[] args) throws Exception {
        JSONObject jsonObject = (JSONObject) readJson("C:\\GIT\\TM\\hub_testauto\\src\\test\\resources\\json\\serviceOrder.json");
        //((JSONObject) ((JSONArray) object).get(0)).get("name")
        Object object = jsonObject.get("items") ;
        List<JSONObject> lis = new ArrayList<JSONObject>();
        for (int i = 0; i < ((JSONArray) object).size() ; i++) {
            lis.add((JSONObject) ((JSONArray) object).get(i));
        }

        for (int i = 0; i < lis.size(); i++) {
            System.out.println(lis.get(i).get("Status") + "-----------" +  lis.get(i).get("Type"));
        }



    }

}
