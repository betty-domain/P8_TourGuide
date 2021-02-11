package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Rewards Service
 */
@Service
public class RewardsService {

    private final Logger logger = LoggerFactory.getLogger(RewardsService.class);

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private static final int DEFAULT_PROXIMITY_BUFFER = 10;
    private int proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    private static final int ATTRACTION_PROXIMITY_RANGE = 200;
    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;
    private ExecutorService executorService;

    /**
     * Constructor with necessary services
     *
     * @param gpsUtilService       gpsUtilService
     * @param rewardCentralService rewardCentralService
     */
    public RewardsService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Set proximity Buffer
     *
     * @param proximityBuffer proximity Buffer to set
     */
    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    /**
     * Method to set Proximity Buffer to default value
     */
    public void setDefaultProximityBuffer() {
        proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    }

    /**
     * Calculate rewards for user
     *
     * @param user user
     */
    public void calculateRewards(User user) {

        //copy list of userLocations to avoid change of elements in this list during iterations on the list
        List<VisitedLocation> userLocationsCopied = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        logger.debug("User nb visitedLocation : " + userLocationsCopied.size());
        List<Attraction> attractions = new CopyOnWriteArrayList<>(gpsUtilService.getAttractions());

        userLocationsCopied.stream().forEach(visitedLocation ->
                {
                    attractions.stream().forEach(attraction -> {
                        if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                            if (nearAttraction(visitedLocation, attraction)) {
                                user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user.getUserId())));
                            }
                        }
                    });
                }
        );
    }

    /**
     * Calculate rewards for user list
     *
     * @param userList user List
     */
    public void calculateRewardsForUserList(List<User> userList) {
        logger.debug("Calculate Rewards for user list : nbUsers = " + userList.size());

        ExecutorService rewardsExecutorService = Executors.newFixedThreadPool(1000);

        userList.stream().forEach(user -> {
            Runnable runnableTask = () -> {

                calculateRewards(user);
            };
            rewardsExecutorService.execute(runnableTask);
        });

        rewardsExecutorService.shutdown();

        try {
            boolean executorHasFinished = rewardsExecutorService.awaitTermination(25, TimeUnit.MINUTES);
            if (!executorHasFinished) {
                logger.error("calculateRewards does not finish in 20 minutes elapsed time");
                rewardsExecutorService.shutdownNow();
            } else {
                logger.debug("calculateRewards finished before the 20 minutes elapsed time");
            }
        } catch (InterruptedException interruptedException) {
            logger.error("executorService was interrupted : " + interruptedException.getLocalizedMessage());
            rewardsExecutorService.shutdownNow();
        }
    }

    /**
     * Check if a given location is in Attraction Proximity
     *
     * @param attraction attraction
     * @param location   location
     * @return true if location is in Attraction Proximity, false otherwise
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return (getDistance(attraction, location) <= ATTRACTION_PROXIMITY_RANGE);
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return (getDistance(attraction, visitedLocation.location) <= proximityBuffer);
    }

    /**
     * Return rewardsPoints when user visited an attraction
     *
     * @param attraction attraction
     * @param userId     userId
     * @return number of rewards points
     */
    public int getRewardPoints(Attraction attraction, UUID userId) {
        return rewardCentralService.getAttractionRewardPoints(attraction.attractionId, userId);
    }

    /**
     * Calculate distance between two locations
     *
     * @param loc1 location 1
     * @param loc2 location 2
     * @return distance between location
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
