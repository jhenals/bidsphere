package com.bidsphere.auction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.bidsphere.auction.entity.Auction;

public record AuctionResponse(UUID id, UUID sellerId, String title, String description,
        String currency, BigDecimal startingPrice, BigDecimal currentPrice,
        BigDecimal bidIncrement, OffsetDateTime startsAt, OffsetDateTime endsAt,
        Auction.Status status, UUID currentBidId, UUID currentBidderId) {

    public static AuctionResponse from(Auction auction) {
        return new AuctionResponse(auction.getId(), auction.getSeller().getId(), auction.getTitle(),
                auction.getDescription(), auction.getCurrency(), auction.getStartingPrice(),
                auction.getCurrentPrice(), auction.getBidIncrement(), auction.getStartsAt(),
                auction.getEndsAt(), auction.getStatus(), auction.getCurrentBidId(),
                auction.getCurrentBidderId());
    }
}
