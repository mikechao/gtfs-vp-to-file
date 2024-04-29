package dev.mike.chao.gtfsvptofile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;

import dev.mike.chao.gtfsvptofile.proto.ProtobufRuntimeHints;

@Configuration
@ImportRuntimeHints(ProtobufRuntimeHints.class)
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
	
	/*
	 * Use VehiclePositionServiceImpl instead of the interface VehiclePositionService
	 * for native image inspection of @PostConstruct methods as the interface contains
	 * no such info
	 */
	@Bean
	public VehiclePositionServiceImpl vehiclePositionService() {
		return new VehiclePositionServiceImpl(gtfsVehiclePositionURL, handler(), gtfsRouteIds);
	}
	
	/*
	 * Use VehiclePositionToFileHandler instead of the interface VehiclePositionHandler
	 * for native image
	 */
	@Bean
	public VehiclePositionToFileHandler handler() {
		return new VehiclePositionToFileHandler(filePath);
	}
}
