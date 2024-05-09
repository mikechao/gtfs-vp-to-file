package dev.mike.chao.gtfsvptofile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.OccupancyStatus;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;

public class TestVehiclePositionServiceImpl {

  @Test
  public void testWithBadURL() {
    VehiclePositionHandler mockHandler = mock(VehiclePositionHandler.class);
    ApplicationExit mockApplicationExit = mock(ApplicationExit.class);
    String interestedRouteIds = GtfsVpToFileConfig.ALL;
    String feedURL = "No good";

    VehiclePositionServiceImpl vp = new VehiclePositionServiceImpl(feedURL, mockHandler, mockApplicationExit,
        interestedRouteIds);
    vp.init();
    vp.update();

    verify(mockHandler, times(0)).handle(null);
    verify(mockApplicationExit, times(1)).exit(1);
  }

  @Test
  public void testWithErrorCreatingFeedMessage() {
    VehiclePositionHandler mockHandler = mock(VehiclePositionHandler.class);
    ApplicationExit mockApplicationExit = mock(ApplicationExit.class);
    String interestedRouteIds = "1,2,3,4,5";
    String feedURL = "http://www.google.com";

    VehiclePositionServiceImpl vp = new VehiclePositionServiceImpl(feedURL, mockHandler, mockApplicationExit,
        interestedRouteIds);
    vp.init();
    try (MockedStatic<FeedMessage> mockedFeedMessage = Mockito.mockStatic(FeedMessage.class)) {
      mockedFeedMessage.when(() -> FeedMessage.parseFrom(Mockito.any(InputStream.class)))
          .thenThrow(new IOException("Test!"));
      vp.update();
    }
    verify(mockHandler, times(0)).handle(null);
    verify(mockApplicationExit, times(1)).exit(1);
  }

  @Test
  public void testHappyPath() {
    VehiclePositionHandler mockHandler = mock(VehiclePositionHandler.class);
    ApplicationExit mockApplicationExit = mock(ApplicationExit.class);
    String interestedRouteIds = "1,2,3,4,5";
    String feedURL = "http://www.google.com";

    VehiclePositionServiceImpl vp = new VehiclePositionServiceImpl(feedURL, mockHandler, mockApplicationExit,
        interestedRouteIds);
    vp.init();
    FeedMessage fm = createFeedMessage();
    try (MockedStatic<FeedMessage> mockedFeedMessage = Mockito.mockStatic(FeedMessage.class)) {
      mockedFeedMessage.when(() -> FeedMessage.parseFrom(Mockito.any(InputStream.class))).thenReturn(fm);
      vp.update();
    }
    Collection<VehiclePosition> expected = new ArrayList<>();
    expected.add(createVehiclePositionRoute1());
    verifyNoInteractions(mockApplicationExit);
    verify(mockHandler, times(1)).handle(expected);
  }

  private FeedMessage createFeedMessage() {
    FeedHeader header = GtfsRealtime.FeedHeader.newBuilder()
        .setIncrementality(Incrementality.FULL_DATASET)
        .setGtfsRealtimeVersion("2.0")
        .setTimestamp(1715293476)
        .build();
    return GtfsRealtime.FeedMessage.newBuilder()
        .setHeader(header)
        .addEntity(createFeedEntity(createVehiclePositionRoute1()))
        .build();
  }

  int id = 0;

  private FeedEntity createFeedEntity(VehiclePosition vp) {
    id++;
    return GtfsRealtime.FeedEntity.newBuilder()
        .setId(String.valueOf(id))
        .setVehicle(vp)
        .build();
  }

  private VehiclePosition createVehiclePositionRoute1() {
    return VehiclePosition.newBuilder()
        .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
            .setRouteId("3710020")
            .setStartTime("11:55:00")
            .setStartDate("20240508")
            .setScheduleRelationship(ScheduleRelationship.SCHEDULED)
            .setRouteId("1")
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
}
