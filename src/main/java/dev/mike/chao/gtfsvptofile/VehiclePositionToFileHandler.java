package dev.mike.chao.gtfsvptofile;

import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VehiclePositionToFileHandler implements VehiclePositionHandler {

	@Override
	public void handle(VehiclePosition vehiclePosition) {
		log.info("VehiclePositionHandler handle {}", vehiclePosition);
	}

}
