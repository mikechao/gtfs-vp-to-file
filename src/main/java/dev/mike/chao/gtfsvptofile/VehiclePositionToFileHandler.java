package dev.mike.chao.gtfsvptofile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class VehiclePositionToFileHandler implements VehiclePositionHandler {

	private final FileHelperImpl fileHelper;
	private FileOutputStream outputStream;
	private Path outputFilePath;
	private boolean canWrite = true;
	private Counter writeCounter;
	private Counter fileSizeCounter;
	private long fileSize = 0;

	// where String/key = vp.getVehicle().getId() + vp.getTimestamp()
	private Set<String> writtenVPKeys = new HashSet<>();

	@Autowired
	public void setMeterRegistry(MeterRegistry registry) {
		writeCounter = Counter.builder("vehicle.position.writes")
				.description("Counts the number of VehiclePosition objects written")
				.register(registry);

		fileSizeCounter = Counter.builder("vehicle.position.file.size")
				.description("The size of the output file")
				.baseUnit("bytes")
				.register(registry);
	}

	@PostConstruct
	public void init() {
		log.info("VehiclePositionToFileHandler @PostConstruct/init() start");
		if (fileHelper.isFileExist()) {
			outputFilePath = fileHelper.getPath();
			outputStream = createOutputStream(fileHelper.getFile());
			if (outputStream == null) {
				canWrite = false;
			}
		} else {
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
			writeCounter.increment(count);
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
			updateFileSizeCounter();
		}
		return writeCount;
	}

	private String getKey(VehiclePosition vp) {
		return vp.getVehicle().getId() + vp.getTimestamp();
	}

	private void updateFileSizeCounter() {
		try {
			Long size = Long.valueOf(Files.size(outputFilePath));
			double increment = size - fileSize;
			fileSizeCounter.increment(increment);
			fileSize = size;
		} catch (SecurityException se) {
			log.error("Failed to update file size counter", se);
		} catch (IOException e) {
			log.error("Failed to update file size counter", e);
		}
	}

	private FileOutputStream createOutputStream(File file) {
		if (file != null) {
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(file, true);
			} catch (FileNotFoundException e) {
				log.error("Failed to create FileOutputStream. FilePath {}", file, e);
			}
			return outputStream;
		}
		return null;
	}

}
