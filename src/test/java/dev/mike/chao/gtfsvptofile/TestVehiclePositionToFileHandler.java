package dev.mike.chao.gtfsvptofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.OccupancyStatus;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestVehiclePositionToFileHandler {

  private MeterRegistry meterRegistry;
  private FileHelperImpl mockFileHelper;

  @BeforeEach
  public void setup() {
    meterRegistry = new SimpleMeterRegistry();
    Metrics.globalRegistry.add(meterRegistry);
    mockFileHelper = mock(FileHelperImpl.class);
  }

  @AfterEach
  public void tearDown() {
    meterRegistry.clear();
    Metrics.globalRegistry.clear();
  }

  private VehiclePosition createVehiclePosition() {
    return VehiclePosition.newBuilder()
        .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
            .setRouteId("3710020")
            .setStartTime("11:55:00")
            .setStartDate("20240508")
            .setScheduleRelationship(ScheduleRelationship.SCHEDULED)
            .setRouteId("NL")
            .build())
        .setPosition(GtfsRealtime.Position.newBuilder()
            .setLatitude(37.76919f)
            .setLongitude(-122.17404f)
            .setBearing(339.0f)
            .setSpeed(0.0f)
            .build())
        .setCurrentStopSequence(1)
        .setCurrentStatus(VehicleStopStatus.STOPPED_AT)
        .setTimestamp(1715194230)
        .setStopId("2710")
        .setVehicle(GtfsRealtime.VehicleDescriptor.newBuilder()
            .setId("6114")
            .build())
        .setOccupancyStatus(OccupancyStatus.EMPTY)
        .build();
  }

  @Test
  public void testWithValidFile(@TempDir Path tempDir) {
    Path tempPath = tempDir.resolve("output.txt");
    File tempFile = tempPath.toFile();
    VehiclePositionToFileHandler vpToFile = new VehiclePositionToFileHandler(mockFileHelper);
    vpToFile.setMeterRegistry(meterRegistry);
    Mockito.when(mockFileHelper.isFileExist()).thenReturn(true);
    Mockito.when(mockFileHelper.getPath()).thenReturn(tempPath);
    Mockito.when(mockFileHelper.getFile()).thenReturn(tempFile);
    vpToFile.init();

    VehiclePosition vp1 = createVehiclePosition();

    Collection<VehiclePosition> vps = new ArrayList<>();
    vps.add(vp1);
    vps.add(vp1);

    vpToFile.handle(vps);

    var writesCounter = meterRegistry.find("vehicle.position.writes").counter();
    assertTrue(writesCounter != null,
        () -> "There should be a counter named vehicle.position.writes in the MeterRegistry");
    assertEquals(1.0, writesCounter.count());

    var fileSizeCounter = meterRegistry.find("vehicle.position.file.size").counter();
    assertNotNull(fileSizeCounter,
        () -> "There should be a counter named vehicle.position.file.size in the MeterRegistry");
    assertTrue(fileSizeCounter.count() > 0, () -> "The fileSizeCounter count should be greater than 0");

    vpToFile.destroy();
  }

  @Test
  public void testWithInvalidFile() {
    VehiclePositionToFileHandler vpToFile = new VehiclePositionToFileHandler(mockFileHelper);
    vpToFile.setMeterRegistry(meterRegistry);
    Mockito.when(mockFileHelper.isFileExist()).thenReturn(false);
    vpToFile.init();

    VehiclePosition vp1 = createVehiclePosition();
    vpToFile.handle(Collections.singletonList(vp1));
    var writesCounter = meterRegistry.find("vehicle.position.writes").counter();
    assertTrue(writesCounter != null,
        () -> "There should be a counter named vehicle.position.writes in the MeterRegistry");
    assertEquals(0.0, writesCounter.count());

    var fileSizeCounter = meterRegistry.find("vehicle.position.file.size").counter();
    assertNotNull(fileSizeCounter,
        () -> "There should be a counter named vehicle.position.file.size in the MeterRegistry");
    assertTrue(fileSizeCounter.count() == 0, () -> "The fileSizeCounter count should be 0");
    vpToFile.destroy();
  }
}
