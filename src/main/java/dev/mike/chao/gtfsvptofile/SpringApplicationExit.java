package dev.mike.chao.gtfsvptofile;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpringApplicationExit implements ApplicationExit {

  private final ApplicationContext applicationContext;

  @Override
  public void exit(int statusCode) {
    int code = SpringApplication.exit(applicationContext, () -> statusCode);
    System.exit(code);
  }

}
