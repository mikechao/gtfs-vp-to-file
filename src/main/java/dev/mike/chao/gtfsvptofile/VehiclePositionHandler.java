package dev.mike.chao.gtfsvptofile;

import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

public interface VehiclePositionHandler {

	public void handle(VehiclePosition vehiclePosition);
}
