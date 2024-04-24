package dev.mike.chao.gtfsvptofile;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VehiclePositionServiceImpl implements VehiclePositionService {
	
	private final String feedURL;
	private final VehiclePositionHandler vpHandler;
	private final String interestedRouteIds;
	private Set<String> routeIds = new HashSet<>();
	private boolean isAllRoutes = false;
	
	@Autowired
    private ApplicationContext appContext;
	
	public VehiclePositionServiceImpl(String feedURL, VehiclePositionHandler vehiclePositionHandler, String routeIds) {
		this.feedURL = feedURL;
		this.vpHandler = vehiclePositionHandler;
		this.interestedRouteIds = routeIds;
	}
	
	@PostConstruct
	public void init() {
		if (interestedRouteIds == "ALL") {
			isAllRoutes = true;
		} else {
			routeIds.addAll(Arrays.asList(interestedRouteIds.split(",")));
		}
	}

	@Override
	@Scheduled(fixedDelay = 5, initialDelay = 0, timeUnit = TimeUnit.SECONDS)
	public void update() {
		log.info("Updating Vehicle Positions");
		Predicate<VehiclePosition> hasPosition = vp -> {
			return vp.hasPosition();
		};
		Predicate<VehiclePosition> hasTrip = vp -> {
			return vp.hasTrip();
		};
		Predicate<VehiclePosition> filterByRouteId = vp -> {
			return isAllRoutes ? true : routeIds.contains(vp.getTrip().getRouteId());
		};
		FeedMessage feedMessage = createFeedMessage();
		if (feedMessage != null) {
			feedMessage.getEntityList().stream()
        		.filter(FeedEntity::hasVehicle)
        		.map(FeedEntity::getVehicle)
        		.filter(hasPosition.and(hasTrip).and(filterByRouteId))
        		.forEach(vpHandler::handle);
		}
		log.info("Finished updating vehicle positions");
	}
	
	private FeedMessage createFeedMessage() {
		FeedMessage feedMessage = null;
		
    	try {
			feedMessage = FeedMessage.parseFrom(createURL().openStream());
		} catch (IOException e) {
			log.error("Failed to create FeedMessage", e);
			int code = SpringApplication.exit(appContext, () -> 1);
			System.exit(code);
		}

		return feedMessage;
	}
	
	private URL createURL() {
		URL url = null;
		try {
			url = UriComponentsBuilder.fromUriString(feedURL).build().toUri().toURL();
		} catch (Exception e) {
			log.error("Failed to create url from {}", feedURL, e);
			int code = SpringApplication.exit(appContext, () -> 1);
			System.exit(code);
		}
		return url;
	}

}
