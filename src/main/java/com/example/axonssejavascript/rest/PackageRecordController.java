package com.example.axonssejavascript.rest;

import com.example.axonssejavascript.ControllerAdviceSupport;
import com.example.axonssejavascript.DeferredResults;
import com.example.axonssejavascript.api.PackageRecord;
import com.example.axonssejavascript.api.commands.CorrectPackage;
import com.example.axonssejavascript.api.commands.CreatePackage;
import com.example.axonssejavascript.api.queries.PackageRecordById;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController() @RequestMapping(path = "/packages") public class PackageRecordController
    implements ControllerAdviceSupport {
  private static final Logger LOGGER = LoggerFactory.getLogger(PackageRecordController.class);

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;


  @Autowired public PackageRecordController(CommandGateway commandGateway, QueryGateway queryGateway) {
    this.commandGateway = commandGateway;
    this.queryGateway = queryGateway;
  }


  @Override public Logger logger() {
    return LOGGER;
  }


  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
  public DeferredResult<Map<String, String>> create(@RequestBody PackageDto request) {
    Assert.hasText(request.getType(), "type required");
    Assert.hasText(request.getDescription(), "description required");

    CreatePackage command =
        new CreatePackage(UUID.randomUUID().toString(), request.getType(), request.getDescription());

    return DeferredResults
        .from(commandGateway.send(command).thenApply(pkgId -> Collections.singletonMap("id", pkgId.toString())));
  }


  @PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
  public DeferredResult<Void> correct(@PathVariable String id, @RequestBody PackageDto request) {

    CorrectPackage command = new CorrectPackage(id, request.getType(), request.getDescription());

    return DeferredResults.from(commandGateway.send(command));
  }


  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public DeferredResult<PackageRecord> find(@PathVariable String id) {

    PackageRecordById query = new PackageRecordById(id);
    return DeferredResults
        .from(queryGateway.query(query, ResponseTypes.instanceOf(PackageRecord.class))
            .whenComplete(DeferredResults.completeQuery(query)));
  }

  @GetMapping(path = "/{id}/subscription", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
  public Flux<ServerSentEvent<?>> subscribe(@PathVariable String id) {

    return Flux.merge(getPackageRecordFlux(id), getHeardbeatFlux())
        .log()
        .doOnError(error -> LOGGER.warn("Subscription query wrapper error", error))
        .doFinally(signalType ->
            LOGGER.debug("END SUBSCRIPTION QUERY WRAPPER WITH HEARTBEAT; signalType -> {}", signalType));
  }

  private Flux<ServerSentEvent<Object>> getHeardbeatFlux() {
    return Flux.interval(Duration.ofSeconds(20))
        .map(i -> ServerSentEvent.builder().event("ping").build())
        .doFinally(signalType -> LOGGER.debug("END HEARTBEAT; signalType -> {}", signalType));
  }

  private Flux<ServerSentEvent<PackageRecord>> getPackageRecordFlux(String id) {

    SubscriptionQueryResult<PackageRecord, PackageRecord> sqr = queryGateway
        .subscriptionQuery(new PackageRecordById(id), ResponseTypes.instanceOf(PackageRecord.class),
            ResponseTypes.instanceOf(PackageRecord.class));

    return Flux.<PackageRecord>create(emitter -> {

      sqr.initialResult()
          .doOnError(error -> LOGGER.warn("Initial result error", error))
          .doFinally(signalType -> LOGGER.debug("END INITIAL RESULT; signalType -> {}", signalType))
          .subscribe(emitter::next);

      sqr.updates()
          // Buffer burst updates (happens when one command raises multiple events that each update the query model)
          // Pick the last one!
          .buffer(Duration.ofMillis(500)).map(packageRecordList -> packageRecordList.get(packageRecordList.size() - 1))
          .doOnError(error -> LOGGER.warn("Updates result error", error))
          .doFinally(signalType -> LOGGER.debug("END UPDATES; signalType -> {}", signalType))
          .doOnComplete(emitter::complete)
          .subscribe(emitter::next);

    }).doOnError(error -> LOGGER.warn("Model subscription error", error))
        .map(data -> ServerSentEvent.<PackageRecord>builder().data(data).event("message").build())
        .doFinally(signalType -> LOGGER.debug("END MODEL SUBSCRIPTION; signalType -> {}", signalType));
  }
}
