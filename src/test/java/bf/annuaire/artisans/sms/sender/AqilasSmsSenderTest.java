package bf.annuaire.artisans.sms.sender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bf.annuaire.artisans.sms.aqilas.AqilasBulkMessage;
import bf.annuaire.artisans.sms.aqilas.AqilasBulkSmsRequest;
import bf.annuaire.artisans.sms.aqilas.AqilasSmsRequest;
import bf.annuaire.artisans.sms.aqilas.AqilasSmsResponse;
import bf.annuaire.artisans.sms.config.SmsProperties;
import bf.annuaire.artisans.sms.model.SmsPersonalizedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class AqilasSmsSenderTest {

    private SmsProperties properties;
    private RestClient client;
    private RestClient.RequestBodyUriSpec postSpec;
    private RestClient.RequestBodySpec bodySpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        properties = new SmsProperties();
        properties.setEnabled(true);
        properties.getAqilas().setApiToken("token-test");
        properties.getAqilas().setSenderId("FASOTUUMA");

        client = mock(RestClient.class);
        postSpec = mock(RestClient.RequestBodyUriSpec.class);
        bodySpec = mock(RestClient.RequestBodySpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri("/sms")).thenReturn(bodySpec);
        when(bodySpec.header("X-AUTH-TOKEN", "token-test")).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.body(any(AqilasSmsRequest.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void sendMany_postsBulkPayloadWithToArray() {
        when(responseSpec.body(AqilasSmsResponse.class))
                .thenReturn(new AqilasSmsResponse(true, "SMS ENVOYÉS", "bulk-1", 40, "XOF"));

        var sender = new AqilasSmsSender(client, properties, new ObjectMapper());
        sender.sendMany(List.of("+22670000000", "+22676000000"), "Bonjour test OTP");

        var expected = AqilasSmsRequest.immediate(
                "FASOTUUMA", "Bonjour test OTP", List.of("+22670000000", "+22676000000"));
        org.mockito.Mockito.verify(bodySpec).body(expected);
    }

    @Test
    void sendMany_rejectsShortMessage() {
        var sender = new AqilasSmsSender(client, properties, new ObjectMapper());
        assertThrows(IllegalArgumentException.class, () -> sender.sendMany(List.of("+22670000000"), "abc"));
    }

    @Test
    void sendMany_mapsInvalidContactError() {
        when(responseSpec.body(AqilasSmsResponse.class))
                .thenThrow(new RestClientResponseException(
                        "bad",
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        new HttpHeaders(),
                        "{\"message\":\"Contact +226000 invalide !\"}".getBytes(),
                        null));

        var sender = new AqilasSmsSender(client, properties, new ObjectMapper());
        var ex = assertThrows(
                IllegalStateException.class,
                () -> sender.sendMany(List.of("+22600000000"), "Code OTP 123456"));
        assertEquals("Numéro de téléphone invalide.", ex.getMessage());
    }

    @Test
    void sendPersonalized_postsBulkSmsPayload() {
        when(postSpec.uri("/bulksms")).thenReturn(bodySpec);
        when(bodySpec.body(any(AqilasBulkSmsRequest.class))).thenReturn(bodySpec);
        when(responseSpec.body(AqilasSmsResponse.class))
                .thenReturn(new AqilasSmsResponse(true, "SMS ENVOYÉS", "bulk-2", 40, "XOF"));

        var sender = new AqilasSmsSender(client, properties, new ObjectMapper());
        sender.sendPersonalized(List.of(
                new SmsPersonalizedMessage("+22670000000", "Bonjour Moussa, commande prête."),
                new SmsPersonalizedMessage("+22676000000", "Bonjour Aïcha, commande prête.")));

        var expected = new AqilasBulkSmsRequest(
                "FASOTUUMA",
                List.of(
                        new AqilasBulkMessage("+22670000000", "Bonjour Moussa, commande prête."),
                        new AqilasBulkMessage("+22676000000", "Bonjour Aïcha, commande prête.")));
        org.mockito.Mockito.verify(bodySpec).body(expected);
    }
}
