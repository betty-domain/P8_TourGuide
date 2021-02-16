package tourGuide.service;

import tourGuide.model.AttractionTourGuide;
import tourGuide.model.VisitedLocationTourGuide;

import java.util.List;
import java.util.UUID;

public interface IGpsUtilService {

    /**
     * getUserLocation for a given user
     * @param userId userId
     * @return get VisitedLocation for given user
     */
    VisitedLocationTourGuide getUserLocation(UUID userId);

    /**
     * get list of all Attractions
     * @return list of attractions
     */
    List<AttractionTourGuide> getAttractions();
}
