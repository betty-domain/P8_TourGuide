package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

/**
 * TripPricer Service class
 */
@Service
public class TripPricerService {

    private final Logger logger = LoggerFactory.getLogger(TripPricerService.class);

    private final TripPricer tripPricer;

    /**
     * Constructor of TripPricer Service
     */
    public TripPricerService()
    {
        tripPricer = new TripPricer();
    }

    /**
     * get provider list of trips with calculated price for given parameters
     * @param apiKey apiKey
     * @param attractionId attraction id
     * @param adults number of adults
     * @param children number of children
     * @param nightsStay number of nights stayed
     * @param rewardsPoints number of rewardsPoints
     * @return provider list
     */
    public List<Provider> getPrice(String apiKey, UUID attractionId, int adults, int children, int nightsStay, int rewardsPoints)
    {
        logger.debug("Call to tripPricer.getPrice");
        return  tripPricer.getPrice(apiKey,attractionId,adults, children, nightsStay, rewardsPoints);
    }

    /**
     * getProvider name
     * @param apiKey apiKey
     * @param adults number of Adults
     * @return provider name
     */
    public String getProviderName(String apiKey, int adults)
    {
        logger.debug("Call to tripPricer.getProviderName");
        return tripPricer.getProviderName(apiKey,adults);
    }

}
