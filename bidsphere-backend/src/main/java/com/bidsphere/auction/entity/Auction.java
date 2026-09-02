package com.bidsphere.auction.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.bidsphere.user.entity.User;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "auctions")
public class Auction {

    public enum Status { DRAFT, SCHEDULED, LIVE, ENDED, CANCELLED, SETTLED }

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, columnDefinition = "char(3)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;

    @Column(name = "starting_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal startingPrice;

    @Column(name = "current_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "bid_increment", nullable = false, precision = 19, scale = 4)
    private BigDecimal bidIncrement;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "current_bid_id")
    private UUID currentBidId;

    @Column(name = "current_bidder_id")
    private UUID currentBidderId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Auction() { }

    public Auction(User seller, String title, String description, String currency,
            BigDecimal startingPrice, BigDecimal bidIncrement,
            OffsetDateTime startsAt, OffsetDateTime endsAt) {
        this.seller = seller;
        this.title = title;
        this.description = description;
        this.currency = currency.toUpperCase();
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.bidIncrement = bidIncrement;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = startsAt.isAfter(OffsetDateTime.now()) ? Status.SCHEDULED : Status.LIVE;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() { updatedAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public User getSeller() { return seller; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCurrency() { return currency; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getBidIncrement() { return bidIncrement; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public Status getStatus() { return status; }
    public UUID getCurrentBidId() { return currentBidId; }
    public UUID getCurrentBidderId() { return currentBidderId; }

    public boolean acceptsBidsAt(OffsetDateTime now) {
        return (status == Status.LIVE || (status == Status.SCHEDULED && !now.isBefore(startsAt)))
                && now.isBefore(endsAt);
    }

    public void acceptBid(UUID bidId, UUID bidderId, BigDecimal amount) {
        currentBidId = bidId;
        currentBidderId = bidderId;
        currentPrice = amount;
        if (status == Status.SCHEDULED) status = Status.LIVE;
    }
}
