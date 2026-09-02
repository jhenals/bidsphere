package com.bidsphere.bid.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bidsphere.bid.entity.Bid;
import com.bidsphere.bid.service.BidService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    /**
     * X-Bidder-Id is temporary development-only authentication. Replace it with
     * the authenticated principal's ID when Spring Security is introduced.
     */
    @PostMapping
    public ResponseEntity<BidResponse> placeBid(
            @PathVariable UUID auctionId,
            @RequestHeader("X-Bidder-Id") UUID bidderId,
            @RequestHeader("Idempotency-Key") UUID requestId,
            @Valid @RequestBody PlaceBidRequest request) {
        Bid bid = bidService.placeBid(auctionId, bidderId, request.amount(), requestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(BidResponse.from(bid, auctionId));
    }
}
