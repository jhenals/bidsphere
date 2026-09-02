package com.bidsphere.auction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAuctionRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 10000) String description,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull @DecimalMin("0.00") BigDecimal startingPrice,
        @NotNull @DecimalMin("0.0001") BigDecimal bidIncrement,
        @NotNull OffsetDateTime startsAt,
        @NotNull @Future OffsetDateTime endsAt) { }
