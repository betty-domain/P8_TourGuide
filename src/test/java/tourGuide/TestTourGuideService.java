package tourGuide;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.AttractionTourGuide;
import tourGuide.model.LocationTourGuide;
import tourGuide.model.NearbyAttractionDto;
import tourGuide.model.UserCurrentLocationDto;
import tourGuide.model.VisitedLocationTourGuide;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TestTourGuideService {

    private TourGuideService tourGuideService;

    private GpsUtilService gpsUtilServiceMock = Mockito.mock(GpsUtilService.class);

    private RewardsService rewardsServiceMock = Mockito.mock(RewardsService.class);

    @BeforeEach
    public void setUp() {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService = new TourGuideService(gpsUtilServiceMock, rewardsServiceMock, new TripPricerService());
        //tourGuideService.tracker.startTracking();
    }

    @Test
    public void getUserLocationWithVisitedLocationHistory() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        VisitedLocationTourGuide visitedLocation = new VisitedLocationTourGuide(user.getUserId(), new LocationTourGuide(25.5, 42.6), Date.from(Instant.now()));
        user.addToVisitedLocations(visitedLocation);

        tourGuideService.addUser(user);
        assertNotNull(tourGuideService.getUserLocation(user.getUserName()));
        verify(gpsUtilServiceMock, Mockito.times(0)).getUserLocation(any());

    }

    @Test
    public void getUserLocationWithoutVisitedLocationHistory() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        VisitedLocationTourGuide visitedLocation = new VisitedLocationTourGuide(user.getUserId(), new LocationTourGuide(25.5, 42.6), Date.from(Instant.now()));
        tourGuideService.addUser(user);

        when(gpsUtilServiceMock.getUserLocation(user.getUserId())).thenReturn(Mono.just(visitedLocation));
        assertNotNull(tourGuideService.getUserLocation(user.getUserName()));
        verify(gpsUtilServiceMock, Mockito.times(1)).getUserLocation(user.getUserId());

    }

    @Test
    public void addUser() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrievedUser = tourGuideService.getUser(user.getUserName());
        User retrievedUser2 = tourGuideService.getUser(user2.getUserName());

        //tourGuideService.tracker.stopTracking();

        assertEquals(user, retrievedUser);
        assertEquals(user2, retrievedUser2);
    }

    @Test
    public void getAllUsers() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        //tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        VisitedLocationTourGuide visitedLocationMock = new VisitedLocationTourGuide(user.getUserId(), new LocationTourGuide(25.5, 42.6), Date.from(Instant.now()));
        when(gpsUtilServiceMock.getUserLocation(user.getUserId())).thenReturn(Mono.just(visitedLocationMock));

        VisitedLocationTourGuide visitedLocationTourGuide = tourGuideService.trackUserLocation(user);

        assertEquals(user.getUserId(), visitedLocationTourGuide.getUserId());
        assertNotNull(visitedLocationTourGuide.getLocationTourGuide());
    }

    @Test
    public void getNearbyAttractions() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocationTourGuide visitedLocationTourGuide = new VisitedLocationTourGuide(user.getUserId(), new LocationTourGuide(25.5, 42.6), Date.from(Instant.now()));

        AttractionTourGuide attractionTourGuide1 = new AttractionTourGuide("att1", "city", "", 15.5, 20.5);
        AttractionTourGuide attractionTourGuide2 = new AttractionTourGuide("att2", "city", "", 25.5, 40.5);

        when(gpsUtilServiceMock.getUserLocation(user.getUserId())).thenReturn(Mono.just(visitedLocationTourGuide));
        when(gpsUtilServiceMock.getAttractions()).thenReturn(Flux.just(attractionTourGuide1, attractionTourGuide2));
        when(rewardsServiceMock.getDistance(any(), any())).thenReturn(25.48);

        NearbyAttractionDto nearbyAttractionDto = tourGuideService.getNearByAttractions(visitedLocationTourGuide);

        //tourGuideService.tracker.stopTracking();

        assertEquals(2, nearbyAttractionDto.getClosestAttractionsList().size());
    }

    @Test
    public void getAllCurrentUserLocations() {
        int nbUsers = tourGuideService.getAllUsers().size();

        List<UserCurrentLocationDto> userCurrentLocationDtoList = tourGuideService.getAllUsersCurrentLocation();

        assertEquals(userCurrentLocationDtoList.size(), nbUsers);
    }

    @Test
    public void getTripDeals() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        UserPreferences userPreferences = new UserPreferences(1, "USD", 5, 2, 2, 3);
        user.setUserPreferences(userPreferences);

        tourGuideService.addUser(user);

        List<Provider> providers = tourGuideService.getTripDeals(user.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(5, providers.size());
    }

}
