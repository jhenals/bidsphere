# Backend Architecture version 1
                    BidSphere Backend
                           │
             ┌─────────────┴─────────────┐
             │       Spring Boot         │
             │                           │
     ┌───────▼───────┐           ┌───────▼───────┐
     │ Auction       │           │ User          │
     │ Module        │           │ Module        │
     └───────┬───────┘           └───────────────┘
             │
     ┌───────▼───────┐
     │ Bidding       │
     │ Module        │
     └───────┬───────┘
             │
     ┌───────▼───────┐
     │ Payment       │
     │ Module        │
     └───────────────┘