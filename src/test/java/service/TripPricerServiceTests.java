package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tourGuide.model.Provider;
import tourGuide.service.TripPricerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TripPricerServiceTests {

    public static MockWebServer mockBackEnd;
    private TripPricerService tripPricerService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    public void setUpEachMethod()
    {
        String baseUrl = String.format("http://localhost:%s",
                mockBackEnd.getPort());

        tripPricerService = new TripPricerService(baseUrl);

    }

    @Test
    void getPriceTest() throws Exception
    {
        List<Provider> providerList = new ArrayList<>();

        providerList.add(new Provider(UUID.randomUUID(),"Provider A",25.2));
        providerList.add(new Provider(UUID.randomUUID(),"Provider B",5.8));


        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(providerList))
                .addHeader("Content-Type", "application/json"));

        List<Provider> providerListResult = tripPricerService.getPrice("apiKey",UUID.randomUUID(),1,3,2,10);

        assertThat(providerListResult.size()).isEqualTo(2);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertThat(recordedRequest.getPath()).contains(TripPricerService.priceEndpoint);

    }

    @Test
    public void getProviderName() throws Exception
    {

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString("My Provider"))
                .addHeader("Content-Type", "application/json"));

        String providerName = tripPricerService.getProviderName("apiKey",20);

        assertThat(providerName).isNotBlank();

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertThat(recordedRequest.getPath()).contains(TripPricerService.providerNameEndpoint);

    }
}
