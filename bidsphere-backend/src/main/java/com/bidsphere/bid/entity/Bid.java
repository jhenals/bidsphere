package com.bidsphere.bid.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.bidsphere.auction.entity.Auction;
import com.bidsphere.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "bids", uniqueConstraints = @UniqueConstraint(name = "uq_bids_auction_request", columnNames = { "auction_id", "request_id" }))
public class Bid {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "placed_at", nullable = false, updatable = false)
    private OffsetDateTime placedAt;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    protected Bid() { }

    public Bid(Auction auction, User bidder, BigDecimal amount, UUID requestId) {
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.requestId = requestId;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        placedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public Auction getAuction() { return auction; }
    public User getBidder() { return bidder; }
    public BigDecimal getAmount() { return amount; }
    public OffsetDateTime getPlacedAt() { return placedAt; }
    public UUID getRequestId() { return requestId; }
}
