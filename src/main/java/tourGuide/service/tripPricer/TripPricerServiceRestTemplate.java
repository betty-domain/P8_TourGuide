package tourGuide.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tourGuide.model.Provider;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * TripPricer Service class
 */
public class TripPricerServiceRestTemplate implements ITripPricerService {

    private final Logger logger = LoggerFactory.getLogger(TripPricerServiceRestTemplate.class);

    private static final String defaultTripPricerRootUrl = "http://localhost:8103";

    public static final String priceEndpoint = "/price";

    public static final String providerNameEndpoint = "/providerName";

    /**
     * Constructor of TripPricer Service
     */
    public TripPricerServiceRestTemplate() {
        this(defaultTripPricerRootUrl);
    }

    public TripPricerServiceRestTemplate(String tripPricerRootUrl) {

    }

    /**
     * get provider list of trips with calculated price for given parameters
     *
     * @param apiKey        apiKey
     * @param attractionId  attraction id
     * @param adults        number of adults
     * @param children      number of children
     * @param nightsStay    number of nights stayed
     * @param rewardsPoints number of rewardsPoints
     * @return provider list
     */
    public List<Provider> getPrice(String apiKey, UUID attractionId, int adults, int children, int nightsStay, int rewardsPoints) {
        logger.debug("Call to tripPricer.getPrice");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(defaultTripPricerRootUrl + priceEndpoint)
                .queryParam("apiKey", apiKey)
                .queryParam("attractionId", attractionId)
                .queryParam("adults", adults)
                .queryParam("children", children)
                .queryParam("nightsStay", nightsStay)
                .queryParam("rewardsPoints", rewardsPoints);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody(), new TypeReference<List<Provider>>() {
            });
        } catch (IOException ioException) {
            logger.error("Error in getPrice : " + ioException.getMessage());
            return null;
        }
    }

    /**
     * getProvider name
     *
     * @param apiKey apiKey
     * @param adults number of Adults
     * @return provider name
     */
    public String getProviderName(String apiKey, int adults) {
        logger.debug("Call to tripPricer.getProviderName");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(defaultTripPricerRootUrl + providerNameEndpoint)
                .queryParam("apiKey", apiKey)
                .queryParam("adults", adults);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody(), String.class);
        } catch (IOException ioException) {
            logger.error("Error in getProviderName : " + ioException.getMessage());
            return null;
        }

    }

}
