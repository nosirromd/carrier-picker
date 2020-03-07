import org.junit.Test;

import static org.junit.Assert.assertThat;
import static utils.JsonCarrierMatcher.hasCarrierOf;

public class PhaseOneTests {

    private static final String O2_RULE = "{\"carrier\" : \"O2\", \"destinationAddress\" : 44}";
    private static final String VERIZON_RULE = "{\"carrier\" : \"VERIZON\", \"destinationAddress\" : 1}";
    private static final String SPAM_RULE = "{\"carrier\" : \"SPAM\", \"destinationAddress\" : 90, \"message\" : \"spam\"}";

    private static final String O2_MESSAGE = "{\"destinationAddress\" : 447590490505, \"sourceAddress\" : 447984048491, \"message\" : \"hey\"}";
    private static final String VERIZON_MESSAGE = "{\"destinationAddress\" : 17590490223, \"sourceAddress\" : 447984048491, \"message\" : \"this is spam\"}";

    @Test
    public void whenNoRulesAreLoadedMessageIsRejected() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        String response = carrierPicker.pickCarrier(O2_MESSAGE);

        assertThat(response, hasCarrierOf("UNKNOWN"));
    }

    @Test
    public void whenMessageMatchesRuleMessageRoutedAccordingToRule() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(O2_RULE);
        String response = carrierPicker.pickCarrier(O2_MESSAGE);

        assertThat(response, hasCarrierOf("O2"));
    }

    @Test
    public void whenMessageDoesNotMatchRuleCarrierIsUnknown() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(O2_RULE);
        String response = carrierPicker.pickCarrier(VERIZON_MESSAGE);

        assertThat(response, hasCarrierOf("UNKNOWN"));
    }

    @Test
    public void whenMultipleRulesAreLoadedMessagesMatchCorrectRule() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(O2_RULE);
        carrierPicker.readCarrierRule(VERIZON_RULE);
        String verizonResponse = carrierPicker.pickCarrier(VERIZON_MESSAGE);
        String o2Response = carrierPicker.pickCarrier(O2_MESSAGE);

        assertThat(verizonResponse, hasCarrierOf("VERIZON"));
        assertThat(o2Response, hasCarrierOf("O2"));
    }

    @Test
    public void whenOneFieldOfRuleDoesNotMatchMessageThenMessageIsNotMatched() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(SPAM_RULE);
        String spamResponse = carrierPicker.pickCarrier(VERIZON_MESSAGE);
        String notSpamResponse = carrierPicker.pickCarrier(O2_MESSAGE);

        assertThat(spamResponse, hasCarrierOf("SPAM"));
        assertThat(notSpamResponse, hasCarrierOf("UNKNOWN"));

    }

}
