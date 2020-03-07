import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static utils.JsonCarrierMatcher.hasCarrierOf;

public class PhaseTwoTests {

    private static final String O2_RULE = "{\"carrier\" : \"O2\", \"destinationAddress\" : 44}";
    private static final String VODAFONE_RULE = "{\"carrier\" : \"VODAFONE\", \"destinationAddress\" : 5, \"sourceAddress\" : 447}";
    private static final String CHRIS_RULE = "{\"carrier\" : \"CHRISTELECOM\", \"destinationAddress\" : 5, \"sourceAddress\" : 447984048491}";
    private static final String SPENNY_RULE = "{\"carrier\" : \"SPENNYTELECOM\", \"destinationAddress\" : 5, \"sourceAddress\" : 447, \"price\" : 1000.0}";

    private static final String NINETY_NINE_RULE =  "{\"carrier\" : \"99TELECOM\", \"destinationAddress\" : 44, \"price\": 99.0}";
    private static final String ONE_ZERO_ONE_RULE =  "{\"carrier\" : \"101TELECOM\", \"destinationAddress\" : 44, \"price\": 101.0}";

    private static final String AUTH_MESSAGE = "{\"destinationAddress\" : 447590493305, \"sourceAddress\" : 447984048491, \"message\" : \"your verification code is SMS15KEWL\"}";

    @Test
    public void whenTwoRulesAreMatchedReturnRuleWithMostMatchedFields() throws Exception {
        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(O2_RULE);
        carrierPicker.readCarrierRule(VODAFONE_RULE);

        String vodafoneResponse = carrierPicker.pickCarrier(AUTH_MESSAGE);
        assertThat(vodafoneResponse, hasCarrierOf("VODAFONE"));

    }

    @Test
    public void whenTwoRulesMatchWithSameFieldCountReturnLongestMatch() throws Exception {
        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(O2_RULE);
        carrierPicker.readCarrierRule(VODAFONE_RULE);
        carrierPicker.readCarrierRule(CHRIS_RULE);

        String vodafoneResponse = carrierPicker.pickCarrier(AUTH_MESSAGE);
        assertThat(vodafoneResponse, hasCarrierOf("CHRISTELECOM"));

    }

    @Test
    public void whenTwoEquivalentRulesAreMatchedReturnTheCheapestRule() throws Exception {
        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(SPENNY_RULE);
        carrierPicker.readCarrierRule(VODAFONE_RULE);

        String vodafoneResponse = carrierPicker.pickCarrier(AUTH_MESSAGE);
        assertThat(vodafoneResponse, hasCarrierOf("VODAFONE"));

    }

    @Test
    public void whenNoPriceIsGivenDefaultPriceIs100() throws Exception {
        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(ONE_ZERO_ONE_RULE);
        carrierPicker.readCarrierRule(O2_RULE);

        String hundredResponse = carrierPicker.pickCarrier(AUTH_MESSAGE);

        carrierPicker.readCarrierRule(NINETY_NINE_RULE);

        String ninetynineResponse = carrierPicker.pickCarrier(AUTH_MESSAGE);

        assertThat(hundredResponse, hasCarrierOf("O2"));
        assertThat(ninetynineResponse, hasCarrierOf("99TELECOM"));
    }

}
