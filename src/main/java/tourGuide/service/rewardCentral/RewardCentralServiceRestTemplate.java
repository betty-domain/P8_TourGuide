package tourGuide.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

public class RewardCentralServiceRestTemplate implements IRewardCentralService {
    private final Logger logger = LoggerFactory.getLogger(RewardCentralServiceRestTemplate.class);

    private static final String defaultRewardsCentralRootUrl = "http://localhost:8102";

    public static final String attractionsRewardsEndpoint = "/rewardsPoints";

    /**
     * Constructor of service
     */
    public RewardCentralServiceRestTemplate() {
        this(defaultRewardsCentralRootUrl);
    }

    public RewardCentralServiceRestTemplate(String rewardsCentralRootUrl) {
    }

    /**
     * Get rewards Points for a user visiting an attraction
     *
     * @param attractionId attraction id
     * @param userId       user id
     * @return number of rewards points
     */
    public int getAttractionRewardPoints(UUID attractionId, UUID userId) {
        logger.debug("Call to rewardCentral.getAttractionRewardPoints(" + attractionId + "," + userId + ")");

        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(defaultRewardsCentralRootUrl + attractionsRewardsEndpoint)
                .queryParam("attractionId", attractionId)
                .queryParam("userId", userId);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(responseEntity.getBody(), Integer.class);
        } catch (IOException ioException) {
            logger.error("Error in getAttractionRewardsPoints : " + ioException.getMessage());
            return Integer.MIN_VALUE;
        }
    }

}
