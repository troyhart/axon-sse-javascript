package com.example.axonssejavascript.command;


import com.example.axonssejavascript.api.commands.CorrectPackage;
import com.example.axonssejavascript.api.commands.CreatePackage;
import com.example.axonssejavascript.api.events.PackageCreated;
import com.example.axonssejavascript.api.events.PackageDescriptionCorrected;
import com.example.axonssejavascript.api.events.PackageTypeCorrected;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.StringUtils;

@Aggregate
public class PackageRecordAggregate {

  @AggregateIdentifier private String id;

  public PackageRecordAggregate() {
  }

  @CommandHandler
  public PackageRecordAggregate(CreatePackage command) {
    AggregateLifecycle.apply(new PackageCreated(command.getId(), command.getType(), command.getDescription()));
  }

  @CommandHandler void handle(CorrectPackage command) {
    if (StringUtils.hasText(command.getDescription())) {
      AggregateLifecycle.apply(new PackageDescriptionCorrected(command.getId(), command.getDescription()));
    }
    if (StringUtils.hasText(command.getType())) {
      AggregateLifecycle.apply(new PackageTypeCorrected(command.getId(), command.getType()));
    }
  }

  @EventSourcingHandler void on(PackageCreated event) {
    id = event.getId();
  }

  @EventSourcingHandler void on(PackageTypeCorrected event) {
    // no-op
  }

  @EventSourcingHandler void on(PackageDescriptionCorrected event) {
    // no-op
  }
}
