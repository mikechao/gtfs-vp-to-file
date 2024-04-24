package dev.mike.chao.gtfsvptofile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class VehiclePositionServiceConfig {

	@Value("${gtfs.vp.feed.url}")
	private String gtfsVehiclePositionURL;
	
	@Value("${gtfs.vp.route.ids}")
	private String gtfsRouteIds;
	
	@Bean
	public VehiclePositionService vehiclePositionService() {
		return new VehiclePositionServiceImpl(gtfsVehiclePositionURL, handler(), gtfsRouteIds);
	}
	
	@Bean
	public VehiclePositionHandler handler() {
		return new VehiclePositionToFileHandler();
	}
}
