import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

//we are using a Jackson ObjectMapper to serialise/de-serialise JSON
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CarrierPicker {

    private static final String UNKNOWN_RULE = "{\"carrier\" : \"UNKNOWN\", \"destinationAddress\" : 44}";
    private ObjectMapper objectMapper;
    // instantiate an anonymous Type Reference class
    // this is needed to tell the Jackson object mapper what type of Java object to build from the JSON string
    private TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
    private ArrayList<Map<String, Object>> rules;

    public CarrierPicker() {
        //instantiate a object mapper from the object mapper class
        this.objectMapper = new ObjectMapper();
        rules = new ArrayList<Map<String, Object>>();
    }

    public void readCarrierRule(String ruleJson) {
        try {
            Map<String, Object> ruleMap = objectMapper.readValue(ruleJson, typeRef);
            System.out.println("");
            System.out.println("readCarrierRule: new rule " + ruleMap);
            //add the map to a list
            rules.add(ruleMap);
            System.out.println("collection is now " + rules);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String pickCarrier(String messageJson) {
        try {
            Map<String, Object> messageMap = objectMapper.readValue(messageJson, typeRef);
            System.out.println("");
            System.out.println("pickCarrier: recvd this message " + messageMap);
            //Parse out the carrier value from the object
            String destinationAddressString = String.valueOf(messageMap.get("destinationAddress"));
            System.out.println("destinationAddressString is " + destinationAddressString);
            String messageString = (String) messageMap.get("message");
            System.out.println("messageString is " + messageString);

            // If every field in the carrier rule is contained within a field of the message
            // return a JSON object with carrier set to that carrier.
            for (Map<String, Object> ruleMap : rules) {
                System.out.println("matching against ruleMap " + ruleMap);
                if (destinationAddressString.contains(String.valueOf(ruleMap.get("destinationAddress")))) {
                    System.out.println("ruleMap.containsKey(\"message\")  " + ruleMap.containsKey("message"));
                    if ( ruleMap.containsKey("message")) {
                        if (messageString.contains((String) ruleMap.get("message"))) {
                            System.out.println("messageString.contains((String) ruleMap.get(\"message\")  " + messageString.contains((String) ruleMap.get("message")));
                            return objectMapper.writeValueAsString(ruleMap);
                        }
                    } else{
                        return objectMapper.writeValueAsString(ruleMap);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return UNKNOWN_RULE;
    }

}

