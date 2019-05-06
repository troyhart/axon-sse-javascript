package com.example.axonssejavascript.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = PackageRecord.class) public interface PackageRecord {
  String getId();
  String getType();
  String getDescription();
}
