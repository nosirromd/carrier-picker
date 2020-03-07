
public class CarrierPicker {

    private static final String UNKNOWN_RULE = "{\"carrier\" : \"UNKNOWN\", \"destinationAddress\" : 44}";

    public void readCarrierRule(String ruleJson) {

    }

    public String pickCarrier(String messageJson) {
        return UNKNOWN_RULE;
    }

}
