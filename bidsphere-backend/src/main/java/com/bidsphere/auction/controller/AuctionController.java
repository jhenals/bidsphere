package com.bidsphere.auction.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bidsphere.auction.dto.AuctionResponse;
import com.bidsphere.auction.dto.CreateAuctionRequest;
import com.bidsphere.auction.service.AuctionService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(
            @RequestHeader("X-Seller-Id") UUID sellerId,
            @Valid @RequestBody CreateAuctionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.create(sellerId, request));
    }

    @GetMapping("/{auctionId}")
    public AuctionResponse getAuction(@PathVariable UUID auctionId) {
        return auctionService.get(auctionId);
    }
}
