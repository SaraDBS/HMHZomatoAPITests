package ApiTests;

import com.jayway.restassured.path.json.JsonPath;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runners.MethodSorters;
import java.util.*;
import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) //For Ascending order test execution
public class CitiesEndpointTests {

    private static final String API_KEY = "abea29999195037528215a6626526a4f";


    @Before
    public void setup() {
        RestAssured.baseURI = "https://developers.zomato.com/api/v2.1/";
        RestAssured.basePath = "/cities";

    }

    @Test
    public void citiesResponseCodeTest() {
        //Verify the status code of an authenticated request without any query params. Verifies an empty array is returned instead of results.

        given().header("user-key", API_KEY)
                .when().log().all().get().
                then().statusCode(200).and().
                body("location_suggestions", Matchers.empty());
    }

    @Test
    public void invalidUserKeyTest() {

        // A user without a valid API key should not be able to access the cities endpoint
        given().header("user-key", new Random().nextLong())
                .when().log().all().get().
                then().statusCode(403).and()
                .body("status", equalTo("Forbidden"))
                .and().body("message", equalTo("Invalid API Key"));

    }

    @Test
    public void citiesNameQueryTest() {

        //Checks if city name requested is the first result in the response with correct fields information
        given().header("user-key", API_KEY)
                .queryParam("q", "Dublin")
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(200).and().contentType(ContentType.JSON)
                .and().body("location_suggestions[0].name", equalTo("Dublin"))
                .and().body("location_suggestions[0].country_name", equalTo("Ireland"))
                .and().body("status", equalTo("success"));

    }


    @Test
    public void responseFieldsCitiesTest() {

        //Checks if all response fields exist for all the returned records

        String response = given().header("user-key", API_KEY)
                .queryParam("q", "Berlin")
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(200).and().contentType(ContentType.JSON)
                .extract()
                .response().asString();
        JsonPath jsonPath = HelperTestMethods.rawToJson(response);
        ArrayList locationSuggestions = jsonPath.get("location_suggestions");
        int numberOfSuggestions = locationSuggestions.size();
        for (int i = 0; i < numberOfSuggestions; i++) {
            assertNotNull(jsonPath.get("location_suggestions[" + i + "].id"));
            assertNotNull(jsonPath.get("location_suggestions[" + i + "].name"));
            assertNotNull(jsonPath.get("location_suggestions[" + i + "].country_name"));
            assertNotNull(jsonPath.get("location_suggestions[" + i + "].country_flag_url"));
        }
    }

    @Test
    public void moreThanOneCityQueryTest() {

        //This endpoint only returns results for 2 cities at once, there is not validation when passing multiple city names but only results for 2 cities are returned
        List<String> citiesRequested = Arrays.asList("Paris", "Vienna", "Berlin");
        String response = given().header("user-key", API_KEY)
                .queryParam("q", citiesRequested)
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(200).and().contentType(ContentType.JSON)
                .extract()
                .response().asString();
        JsonPath jsonPath = HelperTestMethods.rawToJson(response);
        ArrayList citiesReturned = HelperTestMethods.getCityNamesList(jsonPath);
        assertTrue(CollectionUtils.intersection(citiesRequested, citiesReturned) != null);
        assertTrue(!citiesReturned.containsAll(citiesRequested));
    }

    @Test
    public void duplicatedCityNameTest() {

        //Verify that there is no duplicate cities even if user supplies a partial city name
        String response = given().header("user-key", API_KEY)
                .queryParam("q", "Ma")
                .when().log().all().get()
                .then().statusCode(200).extract().response().asString();
        assertTrue(HelperTestMethods.findNoDuplicateCities(HelperTestMethods.getCityIdList(HelperTestMethods.rawToJson(response))));
    }

    @Test
    public void countFieldCitiesTest() {

        // Verify that count field works as a limit for the number of records returned
        String response = given().header("user-key", API_KEY)
                .queryParam("q", "Berlin")
                .queryParam("count", 3)
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(200).and().contentType(ContentType.JSON)
                .extract()
                .response().asString();
        JsonPath jsonPath = HelperTestMethods.rawToJson(response);
        ArrayList locationSuggestions = jsonPath.get("location_suggestions");
        int numberOfSuggestions = locationSuggestions.size();
        assertEquals(3, numberOfSuggestions);
    }

    @Test
    public void coordinatesCitiesTest() {

        //Verify that valid coordinates should return only one result and a correct result
        String response = given().header("user-key", API_KEY)
                .queryParam("lat", 53.344101)
                .queryParam("lon", -6.267490)
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(200).and().contentType(ContentType.JSON)
                .and().body("location_suggestions[0].name", equalTo("Dublin"))
                .body("location_suggestions[0].country_name", equalTo("Ireland"))
                .extract()
                .response().asString();
        JsonPath jsonPath = HelperTestMethods.rawToJson(response);
        ArrayList locationSuggestions = jsonPath.get("location_suggestions");
        assertEquals(1, locationSuggestions.size());
    }

    @Test
    public void invalidCoordinatesCitiesTest() {

        //Verify that invalid coordinates will return a record with empty fields and id = 0
        given().header("user-key", API_KEY)
                .queryParam("lat", 0.123456)
                .queryParam("lon", 0.789101)
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(200).and().contentType(ContentType.JSON)
                .and().body("location_suggestions[0].id", equalTo(0))
                .body("location_suggestions[0].name", equalTo(""));

    }

    @Test
    public void citiesIdFieldsTest() {

        //Verify that passing multiple valid city_ids in a request should return all records related to those ids

        String response = given().header("user-key", API_KEY)
                .queryParam("city_ids", "91,292,4747")
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(200).and().contentType(ContentType.JSON)
                .and().body("location_suggestions[0].id", equalTo(91))
                .body("location_suggestions[1].id", equalTo(292))
                .body("location_suggestions[2].id", equalTo(4747))
                .extract()
                .response().asString();
        JsonPath jsonPath = HelperTestMethods.rawToJson(response);
        ArrayList locationSuggestions = jsonPath.get("location_suggestions");
        int numberOfSuggestions = locationSuggestions.size();
        assertEquals(3, numberOfSuggestions);
    }


    @Test
    public void invalidCitiesIdFieldsTest() {

        //Verify that passing invalid city_ids as query parameters will return a http 500 status code

        given().header("user-key", API_KEY)
                .queryParam("city_ids", "333,444,555")
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(500);
    }

    @Test
    public void multipleQueryParamsTest() {

        //Verify that if both coordinates and city names are passed as parameters the city name will override the coordinates

        String response = given().header("user-key", API_KEY)
                .queryParam("q", "Madrid")
                .queryParam("lat", 53.344101)
                .queryParam("lon", -6.267490)
                .and().contentType(ContentType.JSON)
                .when().log().all().get().then().statusCode(200).and().contentType(ContentType.JSON)
                .and().body("location_suggestions[0].name", containsString("Madrid"))
                .body("location_suggestions[0].country_name", equalTo("United States"))
                .extract()
                .response().asString();
        JsonPath jsonPath = HelperTestMethods.rawToJson(response);
        ArrayList locationSuggestions = jsonPath.get("location_suggestions");
        assertEquals(1, locationSuggestions.size());
    }


    @After
    public void afterTest() {
        //Reset Values
        Utils.resetBaseURI();
        Utils.resetBasePath();
    }

}

