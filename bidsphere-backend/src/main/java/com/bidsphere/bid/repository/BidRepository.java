package com.bidsphere.bid.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bidsphere.bid.entity.Bid;

public interface BidRepository extends JpaRepository<Bid, UUID> {
    Optional<Bid> findByAuctionIdAndRequestId(UUID auctionId, UUID requestId);
}
