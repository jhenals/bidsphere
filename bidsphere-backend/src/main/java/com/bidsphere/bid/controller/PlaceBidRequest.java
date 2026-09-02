package com.bidsphere.bid.controller;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PlaceBidRequest(@NotNull @DecimalMin("0.0001") BigDecimal amount) { }
