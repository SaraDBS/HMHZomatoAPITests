package ApiTests;

import com.jayway.restassured.path.json.JsonPath;
import java.util.*;

public class HelperTestMethods {

    public static JsonPath rawToJson(String response) {
        return new JsonPath(response);
    }

    public static ArrayList getCityIdList (JsonPath jp) {
        ArrayList cityIdList = jp.get("location_suggestions.id");
        return cityIdList;
    }


    public static ArrayList getCityNamesList (JsonPath jp) {
        ArrayList cityNamesList = jp.get("location_suggestions.name");
        return cityNamesList;
    }

    //Find No Duplicate Cities
    public static boolean findNoDuplicateCities (List<Integer> cityIdList) {
         for (int i=0; i< cityIdList.size(); i++) {
            if(Collections.frequency(cityIdList, cityIdList.get(i)) > 1){

                return false;
            }
        }
        return true;
    }
}
