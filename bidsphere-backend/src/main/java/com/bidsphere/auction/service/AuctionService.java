package com.bidsphere.auction.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidsphere.auction.dto.AuctionResponse;
import com.bidsphere.auction.dto.CreateAuctionRequest;
import com.bidsphere.auction.entity.Auction;
import com.bidsphere.auction.repository.AuctionRepository;
import com.bidsphere.user.entity.User;
import com.bidsphere.user.repository.UserRepository;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    public AuctionService(AuctionRepository auctionRepository, UserRepository userRepository) {
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuctionResponse create(UUID sellerId, CreateAuctionRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerId));
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException("Auction end must be after its start");
        }
        Auction auction = new Auction(seller, request.title(), request.description(), request.currency(),
                request.startingPrice(), request.bidIncrement(), request.startsAt(), request.endsAt());
        return AuctionResponse.from(auctionRepository.save(auction));
    }

    @Transactional(readOnly = true)
    public AuctionResponse get(UUID auctionId) {
        return AuctionResponse.from(auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId)));
    }

    public static OffsetDateTime now() { return OffsetDateTime.now(); }
}
