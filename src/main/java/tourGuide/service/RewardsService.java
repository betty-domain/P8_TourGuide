package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.model.AttractionTourGuide;
import tourGuide.model.LocationTourGuide;
import tourGuide.model.VisitedLocationTourGuide;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
        List<VisitedLocationTourGuide> userLocationsCopied = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        logger.debug("User nb visitedLocation : " + userLocationsCopied.size());
        List<AttractionTourGuide> attractionTourGuides = new CopyOnWriteArrayList<>(gpsUtilService.getAttractions());

        //utilisation de parallelStream pour accélérer le traitement
        userLocationsCopied.parallelStream().forEach(visitedLocation ->
                {
                    attractionTourGuides.parallelStream().forEach(attraction -> {
                        if (user.getUserRewards().parallelStream().noneMatch(r -> r.getAttractionTourGuide().getAttractionName().equals(attraction.getAttractionName()))) {
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

        ExecutorService rewardsExecutorService = Executors.newFixedThreadPool(1500);

        userList.parallelStream().forEach(user -> {
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

    //TODO : à supprimer après revue avec Alexandre du bon fonctionnement de Runnable
    public void calculateRewardsNew(User user) {

        ExecutorService rewardsExecutorService = Executors.newFixedThreadPool(1500);


        //copy list of userLocations to avoid change of elements in this list during iterations on the list
        List<VisitedLocationTourGuide> userLocationsCopied = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        List<AttractionTourGuide> attractionTourGuides = new CopyOnWriteArrayList<>(gpsUtilService.getAttractions());

        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();

        logger.debug("User nb visitedLocation : " + userLocationsCopied.size());

        //utilisation de parallelStream pour accélérer le traitement
        for (VisitedLocationTourGuide visitedLocationTourGuide : userLocationsCopied) {
            for (AttractionTourGuide attractionTourGuide : attractionTourGuides) {
                if (user.getUserRewards().parallelStream().noneMatch(r -> r.getAttractionTourGuide().getAttractionName().equals(attractionTourGuide.getAttractionName()))) {
                    if (nearAttraction(visitedLocationTourGuide, attractionTourGuide)) {
                        completableFutures.add(
                                CompletableFuture.runAsync(() -> {
                                    user.addUserReward(new UserReward(visitedLocationTourGuide, attractionTourGuide, this.getRewardPoints(attractionTourGuide, user.getUserId())));
                                }, rewardsExecutorService)
                        );
                    }
                }
            }
        }

        completableFutures.parallelStream().forEach(voidCompletableFuture -> voidCompletableFuture.join());

    }


    /**
     * Check if a given location is in Attraction Proximity
     *
     * @param attractionTourGuide attraction
     * @param locationTourGuide   location
     * @return true if location is in Attraction Proximity, false otherwise
     */
    public boolean isWithinAttractionProximity(AttractionTourGuide attractionTourGuide, LocationTourGuide locationTourGuide) {
        return (getDistance(attractionTourGuide, locationTourGuide) <= ATTRACTION_PROXIMITY_RANGE);
    }

    private boolean nearAttraction(VisitedLocationTourGuide visitedLocationTourGuide, AttractionTourGuide attractionTourGuide) {
        return (getDistance(attractionTourGuide, visitedLocationTourGuide.getLocationTourGuide()) <= proximityBuffer);
    }

    /**
     * Return rewardsPoints when user visited an attraction
     *
     * @param attractionTourGuide attraction
     * @param userId     userId
     * @return number of rewards points
     */
    public int getRewardPoints(AttractionTourGuide attractionTourGuide, UUID userId) {
        return rewardCentralService.getAttractionRewardPoints(attractionTourGuide.getAttractionId(), userId);
    }

    /**
     * Calculate distance between two locations
     *
     * @param loc1 location 1
     * @param loc2 location 2
     * @return distance between location
     */
    public double getDistance(LocationTourGuide loc1, LocationTourGuide loc2) {
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lon1 = Math.toRadians(loc1.getLongitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lon2 = Math.toRadians(loc2.getLongitude());

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
