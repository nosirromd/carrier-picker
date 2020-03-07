import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static utils.JsonCarrierMatcher.hasCarrierOf;

public class PhaseThreeTests {

    private static final String FAST_SPENNY_RULE = "{\"carrier\" : \"FSTELECOM\", \"destinationAddress\" : 37, \"price\" : 1000.0, \"latency\" : 1.0}";
    private static final String SLOW_CHEAP_RULE = "{\"carrier\" : \"SCTELECOM\", \"destinationAddress\" : 37, \"price\" : 1.0, \"latency\" : 1000.0}";
    private static final String AVERAGE_RULE = "{\"carrier\" : \"AVTELECOM\", \"destinationAddress\" : 37, \"price\" : 100.0, \"latency\" : 100.0}";

    private static final String FAST_SPENNY_MESSAGE = "{\"destinationAddress\" : 377802438701, \"sourceAddress\" : 118118, \"message\" : \"Thank you for your donation\", \"priceMultiplier\" : 1.0, \"latencyMultiplier\" : 1000.0}";
    private static final String SLOW_CHEAP_MESSAGE = "{\"destinationAddress\" : 377802438701, \"sourceAddress\" : 118118, \"message\" : \"Thank you for your donation\", \"priceMultiplier\" : 1000.0, \"latencyMultiplier\" : 1.0}";
    private static final String AVERAGE_MESSAGE = "{\"destinationAddress\" : 377802438701, \"sourceAddress\" : 118118, \"message\" : \"Thank you for your donation\", \"priceMultiplier\" : 1.0, \"latencyMultiplier\" : 1.0}";

    private static final String CHEAP_MESSAGE = "{\"destinationAddress\" : 377802438701, \"sourceAddress\" : 118118, \"message\" : \"Thank you for your donation\"}";

    private static final String NO_PRICE_MULTIPLIER_MESSAGE = "{\"destinationAddress\" : 377802438701, \"sourceAddress\" : 118118, \"message\" : \"Thank you for your donation\", \"latencyMultiplier\" : 1.0}";
    private static final String NO_LATENCY_MULTIPLIER_MESSAGE = "{\"destinationAddress\" : 377802438701, \"sourceAddress\" : 118118, \"message\" : \"Thank you for your donation\", \"priceMultiplier\" : 1.0}";



    @Test
    public void whenTwoEquivalentRulesAreMatchedReturnRuleThatMinimizesCost() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(FAST_SPENNY_RULE);
        carrierPicker.readCarrierRule(SLOW_CHEAP_RULE);
        carrierPicker.readCarrierRule(AVERAGE_RULE);

        String fastExpensiveResponse = carrierPicker.pickCarrier(FAST_SPENNY_MESSAGE);
        String slowCheapResponse = carrierPicker.pickCarrier(SLOW_CHEAP_MESSAGE);
        String averageResponse = carrierPicker.pickCarrier(AVERAGE_MESSAGE);

        assertThat(fastExpensiveResponse, hasCarrierOf("FSTELECOM"));
        assertThat(slowCheapResponse, hasCarrierOf("SCTELECOM"));
        assertThat(averageResponse, hasCarrierOf("AVTELECOM"));

    }

    @Test
    public void whenMultipliersArentSpecifiedReturnCheapestRule() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(FAST_SPENNY_RULE);
        carrierPicker.readCarrierRule(SLOW_CHEAP_RULE);

        String cheapResponse = carrierPicker.pickCarrier(CHEAP_MESSAGE);

        assertThat(cheapResponse, hasCarrierOf("SCTELECOM"));

    }

    @Test
    public void defaultPriceMultiplierIsOne() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(AVERAGE_RULE);
        carrierPicker.readCarrierRule(SLOW_CHEAP_RULE);

        String noPriceResponse = carrierPicker.pickCarrier(NO_PRICE_MULTIPLIER_MESSAGE);

        assertThat(noPriceResponse, hasCarrierOf("AVTELECOM"));
    }

    @Test
    public void defaultLatencyMultiplierIsZero() throws Exception {

        CarrierPicker carrierPicker = new CarrierPicker();

        carrierPicker.readCarrierRule(AVERAGE_RULE);
        carrierPicker.readCarrierRule(FAST_SPENNY_RULE);

        String noLatencyResponse = carrierPicker.pickCarrier(NO_LATENCY_MULTIPLIER_MESSAGE);

        assertThat(noLatencyResponse, hasCarrierOf("AVTELECOM"));
    }

}
