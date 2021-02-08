package tourGuide;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.AttractionTourGuide;
import tourGuide.model.VisitedLocationTourGuide;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRewardsService {

    private RewardsService rewardsService;

    private GpsUtilService gpsUtilService;

    @BeforeEach
    public void setUp()
    {
        gpsUtilService = new GpsUtilService();

        rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());

    }

	@Test
	public void userGetRewards() {

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, new TripPricerService());
		tourGuideService.tracker.startTracking();
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		AttractionTourGuide attractionTourGuide = gpsUtilService.getAttractions().collectList().block().get(0);
		user.addToVisitedLocations(new VisitedLocationTourGuide(user.getUserId(), attractionTourGuide, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertTrue(userRewards.size() == 1);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		AttractionTourGuide attractionTourGuide = gpsUtilService.getAttractions().collectList().block().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attractionTourGuide, attractionTourGuide));
	}

	@Test
	public void nearAllAttractions() {

		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, new TripPricerService());
		tourGuideService.tracker.startTracking();
		
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0).getUserName());


		assertEquals(gpsUtilService.getAttractions().collectList().block().size(), userRewards.size());
	}
	
}
