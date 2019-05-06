package com.example.axonssejavascript.query;

import com.example.axonssejavascript.api.PackageRecord;
import com.example.axonssejavascript.api.events.PackageCreated;
import com.example.axonssejavascript.api.events.PackageDescriptionCorrected;
import com.example.axonssejavascript.api.events.PackageTypeCorrected;
import com.example.axonssejavascript.api.queries.PackageRecordById;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoPackageRecordProjector {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoPackageRecordProjector.class);

  private MongoPackageRecordRepository repository;
  private QueryUpdateEmitter emitter;

  @Autowired
  public MongoPackageRecordProjector(MongoPackageRecordRepository repository, QueryUpdateEmitter emitter) {
    this.repository = repository;
    this.emitter = emitter;
  }

  @QueryHandler
  public PackageRecord handle(PackageRecordById query) {
    LOGGER.debug("handle({})", query);
    return find(query);
  }

  @EventHandler
  public void on(PackageCreated event) {
    LOGGER.debug("on({})", event);
    save(new MongoPackageRecord(event.getId(), event.getType(), event.getDescription()));
  }

  @EventHandler
  public void on(PackageTypeCorrected event) {
    LOGGER.debug("on({})", event);
    MongoPackageRecord record = find(new PackageRecordById(event.getId()));
    if (record!=null) {
      save(record.correctType(event.getType()));
    } else {
      LOGGER.warn("[PROJECTION FAILURE; UNKNOWN PACKAGE RECORD] event: {}", event);
    }
  }

  @EventHandler
  public void on(PackageDescriptionCorrected event) {
    LOGGER.debug("on({})", event);
    MongoPackageRecord record = find(new PackageRecordById(event.getId()));
    if (record!=null) {
      save(record.correctDescription(event.getDescription()));
    } else {
      LOGGER.warn("[PROJECTION FAILURE; UNKNOWN PACKAGE RECORD] event: {}", event);
    }
  }

  private MongoPackageRecord find(PackageRecordById query) {
    Optional<MongoPackageRecord> packageRecordOptional = repository.findById(query.getId());
    LOGGER.debug("found({})", packageRecordOptional);
    return packageRecordOptional.isPresent() ? packageRecordOptional.get() : null;
  }

  private void save(MongoPackageRecord record) {
    repository.save(record);
    emitter.emit(PackageRecordById.class, query -> query.getId().equals(record.getId()), record);
    LOGGER.debug("saved({})", record);
  }
}
