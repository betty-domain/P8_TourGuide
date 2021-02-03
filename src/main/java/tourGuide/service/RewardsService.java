package tourGuide.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private static final int DEFAULT_PROXIMITY_BUFFER = 10;
    private int proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    private static final int ATTRACTION_PROXIMITY_RANGE = 200;
    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;

    public RewardsService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    }

    public void calculateRewards(User user) {

        //copy list of userLocations to avoid change of elements in this list during iterations on the list
        List<VisitedLocation> userLocationsCopied = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions = gpsUtilService.getAttractions();

        //utilisation de parallelStream pour accélérer le traitement
        userLocationsCopied.parallelStream().forEach(visitedLocation ->
                {
                    attractions.parallelStream().forEach(attraction -> {
                        if (nearAttraction(visitedLocation, attraction)) {
                            user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user.getUserId())));
                        }
                    });
                }
        );
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return (getDistance(attraction, location) <= ATTRACTION_PROXIMITY_RANGE);
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return (getDistance(attraction, visitedLocation.location) <= proximityBuffer);
    }

    public int getRewardPoints(Attraction attraction, UUID userId) {
        return rewardCentralService.getAttractionRewardPoints(attraction.attractionId, userId);
    }


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
