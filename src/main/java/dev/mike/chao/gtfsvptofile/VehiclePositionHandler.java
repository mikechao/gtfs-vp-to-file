package dev.mike.chao.gtfsvptofile;

import java.util.Collection;

import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

public interface VehiclePositionHandler {
	
	public void handle(Collection<VehiclePosition> vehiclePositions);
}
