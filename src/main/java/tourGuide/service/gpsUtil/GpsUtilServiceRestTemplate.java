package tourGuide.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tourGuide.model.AttractionTourGuide;
import tourGuide.model.VisitedLocationTourGuide;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * GpsUtil Service class
 */
public class GpsUtilServiceRestTemplate implements IGpsUtilService {
    private final Logger logger = LoggerFactory.getLogger(GpsUtilServiceRestTemplate.class);

    private static final String defaultGpsUtilRootUrl = "http://localhost:8101";

    public static final String attractionsEndpoint = "/attractions";

    public static final String userLocationEndpoint = "/userLocation";

    /**
     * getUserLocation for a given user
     *
     * @param userId userId
     * @return get VisitedLocation for given user
     */
    public VisitedLocationTourGuide getUserLocation(UUID userId) {
        logger.debug("Call to gpsUtilService.getUserLocation(" + userId + ")");
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(defaultGpsUtilRootUrl + userLocationEndpoint).
                queryParam("userId", userId);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(responseEntity.getBody(), VisitedLocationTourGuide.class);
        } catch (IOException ioException) {
            logger.error("Error in getUserLocation : " + ioException.getMessage());
            return null;
        }
    }

    /**
     * get list of all Attractions
     *
     * @return list of attractions
     */
    public List<AttractionTourGuide> getAttractions() {

        logger.debug("Call to gpsUtilService.getAttractions()");

        RestTemplate restTemplate = new RestTemplate();
        StringBuilder str = new StringBuilder();
        str.append(defaultGpsUtilRootUrl);
        str.append(attractionsEndpoint);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(str.toString(), String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<AttractionTourGuide>>() {
            });
        }
        catch (IOException ioException)
        {
            logger.error("Error in getAttractions : " + ioException.getMessage());
            return null;
        }
    }
}
