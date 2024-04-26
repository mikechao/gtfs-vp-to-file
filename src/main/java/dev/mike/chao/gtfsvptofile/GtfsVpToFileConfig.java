package dev.mike.chao.gtfsvptofile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class GtfsVpToFileConfig {
	
	public static final String TEMP = "TEMP";
	public static final String ALL = "ALL";

	@Value("${gtfs.vp.feed.url}")
	private String gtfsVehiclePositionURL;
	
	@Value("${gtfs.vp.route.ids:" + ALL + "}")
	private String gtfsRouteIds;
	
	@Value("${gtfs.vp.file:" + TEMP + "}")
	private String filePath;
	
	@Bean
	public VehiclePositionService vehiclePositionService() {
		return new VehiclePositionServiceImpl(gtfsVehiclePositionURL, handler(), gtfsRouteIds);
	}
	
	@Bean
	public VehiclePositionHandler handler() {
		return new VehiclePositionToFileHandler(filePath);
	}
}
