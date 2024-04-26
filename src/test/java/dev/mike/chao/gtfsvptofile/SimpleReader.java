package dev.mike.chao.gtfsvptofile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

public class SimpleReader {

	public static void main(String[] args) {
		if (args.length == 0 || args.length > 1) {
			System.out.println("Expect 1 argument. The file to read");
		}
		String path = args[0];
		File file = createFile(path);
		int readCount = 0;
		Set<String> byKeys = new HashSet<>();
		Set<VehiclePosition> byVehiclePositoinSet = new HashSet<>();
		try (FileInputStream input = new FileInputStream(file)) {
			while (input.available() > 0) {
				VehiclePosition vp = VehiclePosition.parseDelimitedFrom(input);
				if (vp == null) {
					break;
				}
				readCount++;
				byKeys.add(getKey(vp));
				byVehiclePositoinSet.add(vp);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("readCount: " + readCount);
		System.out.println("byKeys.size(): " + byKeys.size());
		System.out.println("byVehiclePositionSet.size(): " + byVehiclePositoinSet.size());
	}
	
	private static File createFile(String path) {
		File file = new File(path);
		return file;
	}
	
	private static String getKey(VehiclePosition vp) {
		return vp.getVehicle().getId() + vp.getTimestamp();
	}
}
