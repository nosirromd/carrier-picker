import java.io.IOException;
import java.util.*;

//we are using a Jackson ObjectMapper to serialise/de-serialise JSON
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CarrierPicker {

    private static final String UNKNOWN_RULE = "{\"carrier\" : \"UNKNOWN\", \"destinationAddress\" : 44}";
    private ObjectMapper objectMapper;

    // instantiate an anonymous Type Reference class
    // this is needed to tell the Jackson object mapper what type of Java object to build from the JSON string
    private TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

    private ArrayList<Map<String, Object>> rulesList;
    private ArrayList<Boolean> ruleMatches;
    private ArrayList<Integer> ruleFieldLengths;
    private ArrayList<Integer> ruleFieldMatchCounts;
    private ArrayList<Double> rulePrices;
    private ArrayList<Double> priceLatencyProducts;

    private int  matchedLongestFieldLength;
    private double matchedCheapestPrice;
    private double matchedCheapestPriceLatencyProduct;
    private int matchedMost;

    public CarrierPicker() {

        //instantiate a object mapper from the object mapper class
        objectMapper = new ObjectMapper();
        rulesList = new ArrayList<Map<String, Object>>();

    }


    public void readCarrierRule(String ruleJson) {
        try {

            Map<String, Object> ruleMap = objectMapper.readValue(ruleJson, typeRef);

            System.out.println("new rule: " + ruleMap);

            //add the map to a list
            rulesList.add(ruleMap);

        }

        catch (IOException e) {

            e.printStackTrace();

        }
    }

    public String pickCarrier(String messageJson) {

        //return UNKNOWN rule if no rules in the rules map
        if (rulesList.size() == 0) {
            System.out.println("picked empty rules map: " + UNKNOWN_RULE);
            return UNKNOWN_RULE;
        }

        try {

            //creatematched rule dataset using message and rules list
            createMatchedRulesDataset(messageJson);

            //return UNKNOWN rule if if no rules are matched
            if (Collections.indexOfSubList(ruleMatches, Collections.singletonList(true)) == -1) {
                System.out.println("picked no rules matched: " + UNKNOWN_RULE);
                return UNKNOWN_RULE;
            }

            //serialise and return rule if the matched rules map contains only one rule
            if (Collections.frequency(ruleMatches, true) == 1)  {
                int i = Collections.indexOfSubList(ruleMatches, Collections.singletonList(true));
                System.out.println("picked only one rule: " + rulesList.get(i));
                return objectMapper.writeValueAsString(rulesList.get(i));
            }

            //serialise and return rule with the most matching fields if only a single rule exists
            if (Collections.frequency(ruleFieldMatchCounts, matchedMost) == 1) {
                int i = Collections.indexOfSubList(ruleFieldMatchCounts, Collections.singletonList(matchedMost));
                System.out.println("picked most matching fields: " + rulesList.get(i));
                return objectMapper.writeValueAsString(rulesList.get(i));
            }

            //serialise and return rule with the most matching characters (i.e. longest field/substring) if only a single rule exists
            if (Collections.frequency(ruleFieldLengths, matchedLongestFieldLength) == 1) {
                int i = Collections.indexOfSubList(ruleFieldLengths, Collections.singletonList(matchedLongestFieldLength));
                System.out.println("picked most matching characters: " + rulesList.get(i));
                return objectMapper.writeValueAsString(rulesList.get(i));
            }

            //serialise and return rule with the smallest latency/price product if only a single rule exists
            if (Collections.frequency(priceLatencyProducts, matchedCheapestPriceLatencyProduct) == 1) {
                int i = Collections.indexOfSubList(priceLatencyProducts, Collections.singletonList(matchedCheapestPriceLatencyProduct));
                System.out.println("picked smallest latency/price product: " + rulesList.get(i));
                return objectMapper.writeValueAsString(rulesList.get(i));
            }

            //serialise and return rule with the cheapest price if only a single rule exists and no rules have a latency field
            if (Collections.frequency(rulePrices, matchedCheapestPrice) == 1) {
                int i = Collections.indexOfSubList(rulePrices, Collections.singletonList(matchedCheapestPrice));
                System.out.println("picked cheapest price: " + rulesList.get(i));
                return objectMapper.writeValueAsString(rulesList.get(i));
            }

        }

        catch (IOException e) {
            e.printStackTrace();

        }

        //serialise and return UNKNOWN rule
        System.out.println("picked: " + UNKNOWN_RULE);
        return UNKNOWN_RULE;

    }

    private void createMatchedRulesDataset(String messageJson) throws IOException {

        //deserialise the message JSON
        Map<String, Object> messageMap = objectMapper.readValue(messageJson, typeRef);

        //System.out.println();
        System.out.println("message: " + messageMap);

        //create arrays
        ruleMatches = new ArrayList<Boolean>(Collections.nCopies(rulesList.size(), true));
        ruleFieldLengths = new ArrayList<Integer>(Collections.nCopies(rulesList.size(), 0));
        ruleFieldMatchCounts = new ArrayList<Integer>(Collections.nCopies(rulesList.size(), 0));
        rulePrices = new ArrayList<Double>(Collections.nCopies(rulesList.size(), 100.0));
        priceLatencyProducts = new ArrayList<Double>(Collections.nCopies(rulesList.size(), Double.MAX_VALUE));

        //compare each rule with the message:
        for (int i = 0; i < rulesList.size(); i++) {

            //compare each rule field with the corresponding message field:
            for (Map.Entry<String, Object> entry : rulesList.get(i).entrySet()) {

                String ruleKey =  entry.getKey();
                Object ruleValue =  entry.getValue();

                switch(ruleKey) {

                    case "carrier":
                        break;

                    case "destinationAddress":
                    case "sourceAddress":
                    case "message":

                        //if message destination address does contain rule destination address
                        if ((String.valueOf(messageMap.get(ruleKey)).contains(String.valueOf(ruleValue)))){

                            //rule field match count array [i]++
                            ruleFieldMatchCounts.set(i, ruleFieldMatchCounts.get(i) + 1);

                            //rule field length array[i] = rule field length (if rule field length is longer)
                            if (String.valueOf(ruleValue).length() > ruleFieldLengths.get(i))
                                ruleFieldLengths.set(i, String.valueOf(ruleValue).length());

                        } else {
                            ruleMatches.set(i, false);
                        }
                        break;

                    case "price":

                        //rule price array[i] = rule price
                        double rulePrice = (double) ruleValue;
                        rulePrices.set(i, rulePrice);
                        break;

                    case "latency":

                        //if message has latency and  price multipliers
                        double latencyMultiplier = 0.0;
                        double priceMultiplier = 1.0;
                        double rulePrice2 = 100.0;
                        double ruleLatency = (double) ruleValue;

                        if (messageMap.get("latencyMultiplier") != null )
                            latencyMultiplier = (double) messageMap.get("latencyMultiplier");

                        if (messageMap.get("priceMultiplier") != null)
                            priceMultiplier = (double) messageMap.get("priceMultiplier");

                        if (rulesList.get(i).get("price") != null) {
                            rulePrice2 = (double) rulesList.get(i).get("price");

                            //rule field match count array [i]++
                            ruleFieldMatchCounts.set(i, ruleFieldMatchCounts.get(i) + 1);

                            //price latency array[i]  = (priceMultiplier * price) + (latencyMultiplier * latency)
                            priceLatencyProducts.set(i,  (priceMultiplier * rulePrice2) + (latencyMultiplier * ruleLatency));

                        } else {
                            ruleMatches.set(i, false);
                        }
                        break;

                    default:
                        System.out.println("no match");
                        break;

                }//end switch

            }//end fieldLoop

        }//end ruleLoop

        //set [rule field length array][i] to zero where [rule matches arraylist][i] = false
        for (int i = 0; i < ruleMatches.size(); i++) {

            if (!ruleMatches.get(i)) {

                //set [rule price array][i] to MAXINT
                rulePrices.set(i, Double.MAX_VALUE);

            }
        }

        //if size non zero then set [matched rules longest field count] = [rule field length arraylist].max()
        if (ruleFieldLengths.size() > 0) matchedLongestFieldLength = Collections.max(ruleFieldLengths);

        //if size non zero then set [matched cheapest price] = [rule price arraylist].min()
        if (rulePrices.size() > 0) matchedCheapestPrice = Collections.min(rulePrices);

        //if size non zero then set [matched cheapest price latency product] = [price latency arraylist].min()
        if (priceLatencyProducts.size() > 0)  matchedCheapestPriceLatencyProduct = Collections.min(priceLatencyProducts);

        //if size non zero then set [matched most] = [rule field match count arraylist].max()
        if (ruleFieldMatchCounts.size() > 0)  matchedMost = Collections.max(ruleFieldMatchCounts);

    }//end method

}//end class

