package tourGuide;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.NearbyAttractionDto;
import tourGuide.model.UserCurrentLocationDto;
import tourGuide.model.VisitedLocationTourGuide;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestTourGuideService {


    private TourGuideService tourGuideService;



    @BeforeEach
    public void setUp()
    {
        GpsUtilService gpsUtilService = new GpsUtilService();

        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService = new TourGuideService(gpsUtilService, rewardsService,new TripPricerService());
        tourGuideService.tracker.startTracking();
    }

	@Test
	public void getUserLocation() {

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);
		/*VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();*/

		assertNotNull(tourGuideService.getUserLocation(user.getUserName()));

	}
	
	@Test
	public void addUser() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void trackUser() {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocationTourGuide visitedLocationTourGuide = tourGuideService.trackUserLocation(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocationTourGuide.getUserId());
	}

	@Test
	public void getNearbyAttractions() {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocationTourGuide visitedLocationTourGuide = tourGuideService.trackUserLocation(user);
		
		NearbyAttractionDto nearbyAttractionDto = tourGuideService.getNearByAttractions(visitedLocationTourGuide);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, nearbyAttractionDto.getClosestAttractionsList().size());
	}

	@Test
    public void getAllCurrentUserLocations()
    {
        int nbUsers = tourGuideService.getAllUsers().size();

        List<UserCurrentLocationDto> userCurrentLocationDtoList = tourGuideService.getAllUsersCurrentLocation();

        assertEquals(userCurrentLocationDtoList.size(),nbUsers);
    }

	@Test
	public void getTripDeals() {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        UserPreferences userPreferences = new UserPreferences(1,"USD",5,2,2,3);
        user.setUserPreferences(userPreferences);

        tourGuideService.addUser(user);

		List<Provider> providers = tourGuideService.getTripDeals(user.getUserName());
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}
	
	
}
