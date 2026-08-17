# ADR-001: Use Redis for Distributed Bid Locking

## Status

Accepted

## Context

Multiple users can submit bids for the same auction simultaneously.

A JVM-level lock such as `synchronized` only protects the critical section
inside a single application instance. It does not provide coordination when
multiple instances of the backend are running.

## Decision

Use Redis-based distributed locking to synchronize bid processing for
individual auctions.

The lock will be associated with the auction ID:

`auction:{auctionId}:lock`

## Alternatives Considered

### JVM synchronized locks

Rejected because they do not work across multiple backend instances.

### Database pessimistic locking

Considered, but rejected for the primary locking mechanism because it
increases database contention.

### Redis distributed locking

Selected because Redis provides fast access and allows coordination across
multiple application instances.

## Consequences

### Positive

- Prevents concurrent bid updates from corrupting auction state.
- Works across multiple backend instances.
- Low latency.

### Negative

- Introduces Redis as an infrastructure dependency.
- Requires handling lock expiration and failure scenarios.
- Requires careful implementation to avoid unsafe lock ownership.

## Related

- ADR-003: Auction Consistency Strategy
- `docs/concurrency.md`