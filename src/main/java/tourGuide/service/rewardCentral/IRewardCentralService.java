package tourGuide.service;

import java.util.UUID;

public interface IRewardCentralService {
    /**
     * Get rewards Points for a user visiting an attraction
     * @param attractionId attraction id
     * @param userId user id
     * @return number of rewards points
     */
    int getAttractionRewardPoints(UUID attractionId, UUID userId);
}
