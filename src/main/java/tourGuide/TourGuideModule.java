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
    public TourGuideService getTourGuideService()
    {
        return  new TourGuideService(getGpsUtilService(),getRewardsService(), getTripPricerService());
    }

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
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtilService(), getRewardCentralService());
	}
	
	@Bean
	public RewardCentralService getRewardCentralService() {
		return new RewardCentralService();
	}
	
}
