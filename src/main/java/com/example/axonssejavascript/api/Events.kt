package com.example.axonssejavascript.api.events

data class PackageCreated(
    val id: String,
    val type: String,
    val description: String
)

data class PackageTypeCorrected(
    val id: String,
    val type: String
)

data class PackageDescriptionCorrected(
    val id: String,
    val description: String
)
