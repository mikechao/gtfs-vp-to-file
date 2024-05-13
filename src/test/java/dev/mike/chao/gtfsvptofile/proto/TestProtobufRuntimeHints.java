package dev.mike.chao.gtfsvptofile.proto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.ReflectionHintsPredicates;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

public class TestProtobufRuntimeHints {

  @Test
  void shouldRegisterHints() {
    RuntimeHints hints = new RuntimeHints();
    new ProtobufRuntimeHints().registerHints(hints, getClass().getClassLoader());
    ReflectionHintsPredicates.TypeHintPredicate vehiclePositionHint = RuntimeHintsPredicates.reflection()
        .onType(VehiclePosition.class);
    assertTrue(vehiclePositionHint.test(hints), "VehiclePosition.class should be registered");
    ReflectionHintsPredicates.TypeHintPredicate feedMessageHint = RuntimeHintsPredicates.reflection()
        .onType(FeedMessage.class);
    assertTrue(feedMessageHint.test(hints), "FeedMessage.class should be registered");
    ReflectionHintsPredicates.TypeHintPredicate feedEntityHint = RuntimeHintsPredicates.reflection()
        .onType(FeedEntity.class);
    assertTrue(feedEntityHint.test(hints), "FeedEntity.class should be registered");
  }
}
