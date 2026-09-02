package com.bidsphere.bid.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.bidsphere.bid.entity.Bid;

public record BidResponse(UUID id, UUID auctionId, UUID bidderId, BigDecimal amount,
        OffsetDateTime placedAt, UUID requestId) {

    public static BidResponse from(Bid bid, UUID auctionId) {
        return new BidResponse(bid.getId(), auctionId, bid.getBidder().getId(), bid.getAmount(),
                bid.getPlacedAt(), bid.getRequestId());
    }
}
