package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * GpsUtil Service class
 */
@Service
public class GpsUtilService {
    private final Logger logger = LoggerFactory.getLogger(GpsUtilService.class);

    private final GpsUtil gpsUtil;

    /**
     * Constructor that initialize a new gpsUtil class
     */
    public GpsUtilService()
    {
        gpsUtil = new GpsUtil();
    }

    /**
     * getUserLocation for a given user
     * @param userId userId
     * @return get VisitedLocation for given user
     */
    public VisitedLocation getUserLocation(UUID userId)
    {
        logger.debug("Call to gpsUtil.getUSerLocation(" + userId + ")");
        return gpsUtil.getUserLocation(userId);
    }

    /**
     * get list of all Attractions
     * @return list of attractions
     */
    public List<Attraction> getAttractions()
    {
        logger.debug("Call to gpsUtil.getAttractions()");
        return gpsUtil.getAttractions();
    }
}
