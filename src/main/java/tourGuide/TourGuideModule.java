package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;

@Configuration
public class TourGuideModule {


    @Bean
    public TripPricerService getTripPricerService()
    {
        return  new TripPricerService();
    }

	@Bean
	public GpsUtilService getGpsUtilService() {
		return new GpsUtilService();
	}

    @Bean
    public RewardCentralService getRewardCentralService() {
        return new RewardCentralService();
    }


    @Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtilService(), getRewardCentralService());
	}


    @Bean
    public TourGuideService getTourGuideService()
    {
        TourGuideService tourGuideService =  new TourGuideService(getGpsUtilService(),getRewardsService(), getTripPricerService());
        tourGuideService.tracker.startTracking();
        return tourGuideService;
    }


}
