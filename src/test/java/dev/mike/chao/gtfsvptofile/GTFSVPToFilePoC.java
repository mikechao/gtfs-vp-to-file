package dev.mike.chao.gtfsvptofile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

public class GTFSVPToFilePoC {

	public static void main(String[] args) {
		VehiclePosition vp1 = VehiclePosition.newBuilder()
				.setTrip(TripDescriptor.newBuilder()
						.setTripId("3364020")
						.setStartTime("09:48:00")
						.setStartDate("20240424")
						.setScheduleRelationship(TripDescriptor.ScheduleRelationship.SCHEDULED)
						.setRouteId("51A"))
				.setPosition(Position.newBuilder()
						.setLatitude(37.784912f)
						.setLongitude(-122.276566f)
						.setBearing(357.0f)
						.setSpeed(13.85824f))
				.setCurrentStopSequence(22)
				.setCurrentStatus(VehiclePosition.VehicleStopStatus.IN_TRANSIT_TO)
				.setTimestamp(1713978787)
				.setStopId("2306")
				.setVehicle(VehicleDescriptor.newBuilder()
						.setId("1336"))
				.setOccupancyStatus(VehiclePosition.OccupancyStatus.EMPTY)
				.build();
		
		VehiclePosition vp2 = VehiclePosition.newBuilder()
				.setTrip(TripDescriptor.newBuilder()
						.setTripId("10998020")
						.setStartTime("10:09:00")
						.setStartDate("20240424")
						.setScheduleRelationship(TripDescriptor.ScheduleRelationship.SCHEDULED)
						.setRouteId("51B"))
				.setPosition(Position.newBuilder()
						.setLatitude(37.848377f)
						.setLongitude(-122.25217f)
						.setBearing(355.0f)
						.setSpeed(0.0f))
				.setCurrentStopSequence(3)
				.setCurrentStatus(VehiclePosition.VehicleStopStatus.INCOMING_AT)
				.setTimestamp(1713978861)
				.setStopId("5329")
				.setVehicle(VehicleDescriptor.newBuilder()
						.setId("1579"))
				.setOccupancyStatus(VehiclePosition.OccupancyStatus.EMPTY)
				.build();
		
		// writing
		List<VehiclePosition> vps = Arrays.asList(vp1, vp2);
		File tempFile = null;
		try {
			tempFile = File.createTempFile("vps", ".txt");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to create temp file");
		}
		if (tempFile != null) {
			tempFile.deleteOnExit();
		}
		try(FileOutputStream output = new FileOutputStream(tempFile, true)) {
			for (VehiclePosition vp : vps) {
				vp.writeDelimitedTo(output);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("File Not found");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException");
		}
		
		// reading
		List<VehiclePosition> readIn = new ArrayList<>();
		try (FileInputStream input = new FileInputStream(tempFile)) {
			while (input.available() != 0) {
				VehiclePosition vp = VehiclePosition.parseDelimitedFrom(input);
				if (vp == null) {
					break;
				}
				readIn.add(vp);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("File Not found");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException");
		}
		System.out.println("readIn.size().equals(2) is " + (readIn.size() == 2));
		System.out.println("readIn.get(0).equals(vp1) is " + readIn.get(0).equals(vp1));
		System.out.println("readIn.get(1).equals(vp2) is " + readIn.get(1).equals(vp2));
	}
}
