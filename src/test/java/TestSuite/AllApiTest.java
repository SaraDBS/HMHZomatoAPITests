package TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ApiTests.CitiesEndpointTests;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        CitiesEndpointTests.class
})
public class AllApiTest {
}