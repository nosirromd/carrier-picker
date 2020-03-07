package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonCarrierMatcher extends BaseMatcher<String> {

    private final String expectedCarrier;

    private String lastMatch;

    private JsonCarrierMatcher(String expectedCarrier) {
        this.expectedCarrier = expectedCarrier;
    }

    public static Matcher<String> hasCarrierOf(String carrier) {
        return new JsonCarrierMatcher(carrier);
    }

    @Override
    public boolean matches(Object item) {
        try{
            String rawJson = (String) item;
            lastMatch = rawJson;
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<HashMap<String, Object>>() {};
            Map<String, Object> map = objectMapper.readValue(rawJson, typeRef);

            String responseCarrier = (String) map.get("carrier");

            if(expectedCarrier.equals(responseCarrier)) {
                return true;
            }
            else {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a JSON response where carrier equals " + expectedCarrier);
    }
}
