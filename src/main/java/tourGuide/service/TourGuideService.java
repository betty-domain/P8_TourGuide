package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.AttractionClosestDto;
import tourGuide.model.NearbyAttractionDto;
import tourGuide.model.UserCurrentLocationDto;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

/**
 * TourGuide Service
 */
@Service
public class TourGuideService {
    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtilService gpsUtilService;
    private final RewardsService rewardsService;
    private final TripPricerService tripPricerService;
    public final Tracker tracker;
    boolean testMode = true;

    /**
     * Constructor with necessary service
     *
     * @param gpsUtilService    gpsUtil Service
     * @param rewardsService    rewards Service
     * @param tripPricerService tripPricer service
     * @param startTracker true if tracker must be started
     */
    public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService, TripPricerService tripPricerService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardsService = rewardsService;
        this.tripPricerService = tripPricerService;
        Locale.setDefault(Locale.US);
        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);

        addShutDownHook();
    }

    /**
     * Get userReward list
     *
     * @param username user
     * @return list of userReward
     */
    public List<UserReward> getUserRewards(String username) {
        return this.getUser(username).getUserRewards();
    }

    /**
     * get last user visited location
     *
     * @param username username
     * @return last user visited location
     */
    public VisitedLocation getUserLocation(String username) {
        User user = this.getUser(username);
        return (!user.getVisitedLocations().isEmpty()) ?
                user.getLastVisitedLocation() :
                trackUserLocation(user);
    }

    /**
     * get user by username
     *
     * @param userName username
     * @return user
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * get ALl users
     *
     * @return list of users
     */
    public List<User> getAllUsers() {
        return internalUserMap.values().parallelStream().collect(Collectors.toList());
    }

    /**
     * add user
     *
     * @param user user to add
     */
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    /**
     * get trip deals for user
     *
     * @param username username
     * @return provider list for trip deals
     */
    public List<Provider> getTripDeals(String username) {

        User user = this.getUser(username);

        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = tripPricerService.getPrice(TRIP_PRICER_API_KEY, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    /**
     * Track user location
     *
     * @param user user to track
     * @return last visitedLocation
     */
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    /**
     * Track user location for list of users
     * @param userList list of user to track
     */
    public void trackUserLocationForUserList(List<User> userList)
    {
        logger.debug("Track user location for user list : nbUsers = " + userList.size());
        ExecutorService trackLocationExecutorService = Executors.newFixedThreadPool(1000);

        userList.parallelStream().forEach(user -> {
            Runnable runnableTask = () -> {

                trackUserLocation(user);
            };
            trackLocationExecutorService.execute(runnableTask);
        });

        trackLocationExecutorService.shutdown();

        try {
            boolean executorHasFinished = trackLocationExecutorService.awaitTermination(15, TimeUnit.MINUTES);
            if (!executorHasFinished) {
                logger.error("trackUserLocationforUserList does not finish in 15 minutes elapsed time");
                trackLocationExecutorService.shutdownNow();
            } else {
                logger.debug("trackUserLocationforUserList finished before the 15 minutes elapsed time");
            }
        } catch (InterruptedException interruptedException) {
            logger.error("executorService was Interrupted : " + interruptedException.getLocalizedMessage());
            trackLocationExecutorService.shutdownNow();
        }
    }

    //TODO à supprimer après revue avec Alexandre du bon fonctionnement avec Runnable
    /**
     * Track user location
     *
     * @param user user to track
     * @return last visitedLocation
     */
    public CompletableFuture<VisitedLocation> trackUserLocationNew(User user) {

        ExecutorService executorService = Executors.newFixedThreadPool(1000);

        return CompletableFuture.supplyAsync(() ->
        {
            return gpsUtilService.getUserLocation(user.getUserId());
        }, executorService).
                thenApply(visitedLocation -> {
                            user.addToVisitedLocations(visitedLocation);
                            rewardsService.calculateRewards(user);
                            return visitedLocation;
                        }
                );
    }

    /**
     * Return Top 5 attractions nearest to user
     *
     * @param visitedLocation visited location
     * @return top 5 attractions nearest to user last visited location
     */
    public NearbyAttractionDto getNearByAttractions(VisitedLocation visitedLocation) {
        List<AttractionClosestDto> attractionClosestDtoList = new ArrayList<>();

        List<Attraction> attractionList = gpsUtilService.getAttractions();

        attractionList.parallelStream().forEach(attraction ->
                {
                    AttractionClosestDto attractionClosestDto = new AttractionClosestDto(attraction.attractionName, attraction,
                            rewardsService.getDistance(attraction, visitedLocation.location), rewardsService.getRewardPoints(attraction, visitedLocation.userId));

                    attractionClosestDtoList.add(attractionClosestDto);
                }
        );

        NearbyAttractionDto nearbyAttractionDto = new NearbyAttractionDto();
        nearbyAttractionDto.setUserLocation(visitedLocation.location);
        nearbyAttractionDto.setClosestAttractionsList(attractionClosestDtoList.parallelStream().sorted(Comparator.comparingDouble(AttractionClosestDto::getDistanceUserToAttraction)).limit(5).collect(Collectors.toList()));

        return nearbyAttractionDto;
    }

    /**
     * Get a list of every user's most recent location
     * @return list of all user's last visited location
     */
    public List<UserCurrentLocationDto> getAllUsersCurrentLocation()
    {
        List<User> userList = this.getAllUsers();


        List<UserCurrentLocationDto> userCurrentLocationDtos = new CopyOnWriteArrayList<>();
        userList.parallelStream().forEach(user -> {
            userCurrentLocationDtos.add(new UserCurrentLocationDto(user.getUserId().toString(),user.getLastVisitedLocation().location));
        });

        return  userCurrentLocationDtos;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String TRIP_PRICER_API_KEY = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    /**
     * Initialize internal users for testing purposes
     */
    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);
            generateUserPreference(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    /**
     * Generate userLocation history for testing purposes
     *
     * @param user
     */
    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private void generateUserPreference(User user) {
        user.setUserPreferences(new UserPreferences(1,"USD",5,2,2,3));
    }

    /**
     * Generate Random Longitude
     *
     * @return longitude
     */
    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * Generate Random Latitude
     *
     * @return latitude
     */
    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * Generate Random Time
     *
     * @return time
     */
    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
