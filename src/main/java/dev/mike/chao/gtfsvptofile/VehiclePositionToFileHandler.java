package dev.mike.chao.gtfsvptofile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class VehiclePositionToFileHandler implements VehiclePositionHandler {
	
	private final String filePath;
	private FileOutputStream outputStream;
	private boolean canWrite = true;
	
	// where String/key = vp.getVehicle().getId() + vp.getTimestamp()
	private Set<String> writtenVPKeys = new HashSet<>();
	
	@PostConstruct
	public void init() {
		log.info("VehiclePositionToFileHandler @PostConstruct/init() start");
		outputStream = createOutputStream();
		if (outputStream == null) {
			canWrite = false;
		}
		log.info("VehiclePositionToFileHandler @PostConstruct/init() finish");
	}
	
	@PreDestroy
	public void destroy() {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				log.error("Failed to close FileOutputStream", e);
			}
		}
	}
	
	@Override
	public void handle(Collection<VehiclePosition> vehiclePositions) {
		if (canWrite) {
			log.info("Starting to write VehiclePositions");
			int count = writeToOutputStream(vehiclePositions);
			log.info("Finished writing {} VehiclePositions", count);
		} else {
			log.info("canWrite is false vehiclePositions.size() {}", vehiclePositions.size());
		}
	}
	
	private int writeToOutputStream(Collection<VehiclePosition> vps) {
		int writeCount = 0;
		Predicate<VehiclePosition> filterByVPKey = vp -> {
			String key = getKey(vp);
			if (!writtenVPKeys.contains(key)) {
				writtenVPKeys.add(key);
				return true;
			}
			return false;
		};
		Collection<VehiclePosition> toWrite = vps.stream().filter(filterByVPKey).collect(Collectors.toList());
		for (VehiclePosition writeMe : toWrite) {
			try {
				writeMe.writeDelimitedTo(outputStream);
				writeCount++;
			} catch (IOException e) {
				log.error("Failed to write VehiclePosition {}", writeMe, e);
				break;
			}
		}
		return writeCount;
	}
	
	private String getKey(VehiclePosition vp) {
		return vp.getVehicle().getId() + vp.getTimestamp();
	}
	
	private FileOutputStream createOutputStream() {
		File file = filePath.equals(GtfsVpToFileConfig.TEMP) ? createTempFile() : createFile();
		if (file != null) {
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(file, true);
			} catch (FileNotFoundException e) {
				log.error("Failed to create FileOutputStream. FilePath {}", filePath, e);
			}
			return outputStream;
		}
		return null;
	}
	
	private File createTempFile() {
		File temp = null;
		try {
			temp = File.createTempFile("vps", ".txt");
			log.info("Create file at {}", temp.getAbsolutePath());
		} catch (IOException e) {
			log.error("Failed to create temp file", e);
		}
		return temp;
	}
	
	private File createFile() {
		Path outputFilePath = Paths.get(filePath);
		try {
			Files.createFile(outputFilePath);
			log.warn("Create file at {}", outputFilePath.toString());
		} catch (FileAlreadyExistsException fileExists) {
			log.warn("File at {} already exist new results will be appended", filePath);
			return outputFilePath.toFile();
		} catch (IOException e) {
			log.error("Failed to create file {}", filePath, e);
			return null;
		}
		return outputFilePath.toFile();
	}

}
