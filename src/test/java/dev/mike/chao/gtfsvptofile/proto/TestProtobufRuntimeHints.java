package dev.mike.chao.gtfsvptofile.proto;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

public class TestProtobufRuntimeHints {

  @Test
  void shouldRegisterHints() {
    RuntimeHints hints = new RuntimeHints();
    new ProtobufRuntimeHints().registerHints(hints, getClass().getClassLoader());
  }
}
