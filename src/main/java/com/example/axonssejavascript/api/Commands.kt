package com.example.axonssejavascript.api.commands

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreatePackage(
    @TargetAggregateIdentifier
    val id: String,
    val type: String,
    val description: String
)

data class CorrectPackage(
    @TargetAggregateIdentifier
    val id: String,
    val type: String?,
    val description: String?
)
