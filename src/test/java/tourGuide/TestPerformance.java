package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;
import tourGuide.user.User;

public class TestPerformance {

    /*
     * A note on performance improvements:
     *
     *     The number of users generated for the high volume tests can be easily adjusted via this method:
     *
     *     		InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     *     These tests can be modified to suit new solutions, just as long as the performance metrics
     *     at the end of the tests remains consistent.
     *
     *     These are performance metrics that we are trying to hit:
     *
     *     highVolumeTrackLocation: 100,000 users within 15 minutes:
     *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
     *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */
    private RewardsService rewardsService;

    private TourGuideService tourGuideService;

    private GpsUtilService gpsUtilService;

    @Before
    public void setUp() {
        gpsUtilService = new GpsUtilService();

        rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(100000);
        //TODO : voir s'il est possible de désactiver le tracker pour les tests de performance afin d'éviter de faire le même traitement 2 fois en parallèle
        tourGuideService = new TourGuideService(gpsUtilService, rewardsService, new TripPricerService());
        tourGuideService.tracker.stopTracking();
    }

    @Ignore
    @Test
    public void highVolumeTrackLocation() throws Exception {
        // Users should be incremented up to 100,000, and test finishes within 15 minutes

        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (User user : allUsers) {
            tourGuideService.trackUserLocation(user);
        }
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Ignore
    @Test
    public void highVolumeTrackLocationNewPerf() {
        // Users should be incremented up to 100,000, and test finishes within 15 minutes

        //TODO : voir s'il est possible de remonter cette instructions dans le test ?
        //tourGuideService.tracker.stopTracking();

        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        tourGuideService.trackUserLocationForUserList(allUsers);

        stopWatch.stop();


        System.out.println("highVolumeTrackLocationNewPerf: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Ignore
    @Test
    public void highVolumeGetRewards() {

        // Users should be incremented up to 100,000, and test finishes within 20 minutes
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Attraction attraction = gpsUtilService.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        allUsers.forEach(u -> rewardsService.calculateRewards(u));

        for (User user : allUsers) {
            System.out.println("nb userRewards : " + user.getUserRewards().size());
            assertTrue(user.getUserRewards().size() > 0);
        }
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Ignore
    @Test
    public void highVolumeGetRewardsNewPerf() throws Exception {

        // Users should be incremented up to 100,000, and test finishes within 20 minutes
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("Start StopWatch");

        //tourGuideService.tracker.stopTracking();

        Attraction attraction = gpsUtilService.getAttractions().get(0);
        List<User> allUsers =  tourGuideService.getAllUsers();

        allUsers.parallelStream().forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        rewardsService.calculateRewardsForUserList(allUsers);

        allUsers.parallelStream().forEach(user ->  {
            assertTrue(user.getUserRewards().size() > 0);
        });

        stopWatch.stop();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
}
