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
            System.out.println(">>>readCarrierRule: new rule " + ruleMap);
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

            //deserialise the message JSON
            Map<String, Object> messageMap = objectMapper.readValue(messageJson, typeRef);
            System.out.println("");
            System.out.println(">>>pickCarrier: recvd this message " + messageMap);
            Object messageValue;

            // if message doesn't contain a price, then add a deafult price
            if (messageMap.get("price") == null) { //then
                messageMap.put((String) "price", (Object) 100.0 );
            }

            HashMap<String, Object> cheapestMatchedRuleTrackerMap = new HashMap<>();
            cheapestMatchedRuleTrackerMap.put("price", (double) Double.MAX_VALUE);

            //for each rule in the rule collecton:
            for (Map<String, Object> ruleMap : rules) {

                String ruleKey;
                Object ruleValue;
                int ruleMisatchCount = 0;

                //for each key in the rule:
                for (Map.Entry<String, Object> entry : ruleMap.entrySet()) {
                    ruleKey = (String) entry.getKey();
                    ruleValue = (Object) entry.getValue();


                    //if rule key is not carrier key
                    if (ruleKey.compareTo("carrier") != 0 ) { //then

                        //if message does not contain the rule key
                        messageValue = messageMap.get((Object) ruleKey);
                        if (messageValue == null) { //then
                            //goto next rule
                            break;
                        }
                        else  //the message does contain the rule key
                        {
                            String messageKey = new String((String) ruleKey);

                            // is rule dest address a substring of message dest address?
                            if (messageKey.compareTo("destinationAddress")==0) {
                                String ruleDestinationAddressString = String.valueOf(ruleMap.get("destinationAddress"));
                                String messageDestinationAddressString = String.valueOf(messageMap.get("destinationAddress"));
                                System.out.println("ruleDestinationAddressString is " + ruleDestinationAddressString);
                                System.out.println("messageDestinationAddressString is " + messageDestinationAddressString);

                                //if message destination address does not contain rule destination address
                                System.out.println("messageDestinationAddressString.contains(ruleDestinationAddressString)  "
                                        + messageDestinationAddressString.contains(ruleDestinationAddressString));
                                if (messageDestinationAddressString.contains(ruleDestinationAddressString)==false) { //then
                                    ruleMisatchCount++;
                                    break; //goto next rule
                                }
                            }

                            // is rule message a substring of message message?
                            if (messageKey.compareTo("message")==0) {
                                String messageString = (String) messageMap.get("message");
                                System.out.println("messageString is " + messageString);

                                //if message value does not contain rule value
                                if (messageString.contains((String) ruleMap.get(ruleKey))== false) { //then
                                    System.out.println("messageString.contains((String) ruleMap.get(ruleKey)  "
                                            + messageString.contains((String) ruleMap.get(ruleKey)));

                                    //goto next rule
                                    ruleMisatchCount++;
                                    break; //goto next rule

                                } //if message value does not contain rule value
                            } // is rule message a substring of message message?

                            // track the cheapest rule here
                            if (messageKey.compareTo("price")==0) {

                                //update the cheapest matched rule tracker
                                if ((double) ruleMap.get(ruleKey) < (double) cheapestMatchedRuleTrackerMap.get(ruleKey)) { //then
                                    System.out.println("ruleMap.get(ruleKey) < cheapestMatchedRuleTrackerMap.get(ruleKey)  "
                                            + ((double) ruleMap.get(ruleKey) < (double) cheapestMatchedRuleTrackerMap.get(ruleKey)));

                                    //update the cheapest rule through a shallow copy
                                    cheapestMatchedRuleTrackerMap.putAll(ruleMap);
                                    System.out.println("cheapestMatchedRuleTrackerMap is now  "
                                            + cheapestMatchedRuleTrackerMap);

                                } //update the cheapest matched rule tracker
                            } // track the cheapest rule here
                        } //the message does contain the rule key
                    } //if rule key is not carrier key
                } //end for each key

                // return rule if it has no mismatches
                if (ruleMisatchCount == 0) {
                    System.out.println(">return  " + objectMapper.writeValueAsString(ruleMap));
                    return objectMapper.writeValueAsString(ruleMap);
                } //return the rule as it matches
            }//for each rule...
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(">return UNKNOWN_RULE");
        return UNKNOWN_RULE;
    }

}

