package tourGuide.service;

import gpsUtil.GpsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.UUID;

/**
 * Reward Central service class
 */
@Service
public class RewardCentralService {
    private final Logger logger = LoggerFactory.getLogger(RewardCentralService.class);

    private final RewardCentral rewardCentral;

    /**
     * Constructor of service
     */
    public RewardCentralService()
    {
        rewardCentral = new RewardCentral();
    }

    /**
     * Get rewards Points for a user visiting an attraction
     * @param attractionId attraction id
     * @param userId user id
     * @return number of rewards points
     */
    public int getAttractionRewardPoints(UUID attractionId, UUID userId)
    {
        logger.debug("Call to rewardCentral.getAttractionRewardPoints("+ attractionId + "," + userId+ ")");
        return rewardCentral.getAttractionRewardPoints(attractionId,userId);
    }

}
