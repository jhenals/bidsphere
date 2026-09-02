package com.bidsphere.bid.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidsphere.auction.entity.Auction;
import com.bidsphere.auction.repository.AuctionRepository;
import com.bidsphere.bid.entity.Bid;
import com.bidsphere.bid.repository.BidRepository;
import com.bidsphere.user.entity.User;
import com.bidsphere.user.repository.UserRepository;

@Service
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    public BidService(AuctionRepository auctionRepository, BidRepository bidRepository,
            UserRepository userRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Bid placeBid(UUID auctionId, UUID bidderId, BigDecimal amount, UUID requestId) {
        Bid existing = bidRepository.findByAuctionIdAndRequestId(auctionId, requestId).orElse(null);
        if (existing != null) return existing;

        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));
        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new IllegalArgumentException("Bidder not found: " + bidderId));

        OffsetDateTime now = OffsetDateTime.now();
        if (auction.getSeller().getId().equals(bidderId)) {
            throw new IllegalStateException("Auction sellers cannot bid on their own auction");
        }
        if (!bidder.getStatus().equals(User.Status.ACTIVE)) {
            throw new IllegalStateException("Bidder is not active");
        }
        if (!auction.acceptsBidsAt(now)) {
            throw new IllegalStateException("Auction is not accepting bids");
        }
        BigDecimal minimum = auction.getCurrentPrice().add(auction.getBidIncrement());
        if (amount.compareTo(minimum) < 0) {
            throw new IllegalArgumentException("Bid must be at least " + minimum);
        }

        Bid bid = bidRepository.save(new Bid(auction, bidder, amount, requestId));
        auction.acceptBid(bid.getId(), bidderId, amount);
        auctionRepository.save(auction);
        return bid;
    }
}
