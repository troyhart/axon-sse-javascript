package com.example.axonssejavascript;

import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventLogger.class);

  @EventHandler
  public void on(Object event) {
    LOGGER.debug("Event Published: {}", event);
  }
}
