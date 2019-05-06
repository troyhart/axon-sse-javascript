package com.example.axonssejavascript.query;

import com.example.axonssejavascript.api.PackageRecord;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class MongoPackageRecord implements PackageRecord {
  @Id
  private String id;
  private String type;
  private String description;

  @SuppressWarnings("unused") // for persistence
  MongoPackageRecord() {
  }

  MongoPackageRecord(String id, String type, String description) {
    this.id = id;
    this.type = type;
    this.description = description;
  }

  @Override public String getId() {
    return id;
  }

  @Override public String getType() {
    return type;
  }

  @Override public String getDescription() {
    return description;
  }

  public MongoPackageRecord correctType(String type) {
    this.type = type;
    return this;
  }

  public MongoPackageRecord correctDescription(String description) {
    this.description = description;
    return this;
  }

  @Override public String toString() {
    return "MongoPackageRecord{" + "id='" + id + '\'' + ", type='" + type + '\'' + ", description='" + description
        + '\'' + '}';
  }
}
