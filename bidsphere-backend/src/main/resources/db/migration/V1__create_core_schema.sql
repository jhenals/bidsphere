CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_app_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),
    CONSTRAINT ck_app_users_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT ck_app_users_display_name_not_blank CHECK (btrim(display_name) <> '')
);

CREATE UNIQUE INDEX uq_app_users_email_lower ON app_users (lower(email));

CREATE TABLE auctions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL REFERENCES app_users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    currency CHAR(3) NOT NULL,
    starting_price NUMERIC(19,4) NOT NULL,
    current_price NUMERIC(19,4) NOT NULL,
    bid_increment NUMERIC(19,4) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_auctions_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'LIVE', 'ENDED', 'CANCELLED', 'SETTLED')),
    CONSTRAINT ck_auctions_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT ck_auctions_currency_uppercase CHECK (currency = upper(currency)),
    CONSTRAINT ck_auctions_starting_price_non_negative CHECK (starting_price >= 0),
    CONSTRAINT ck_auctions_current_price_valid CHECK (current_price >= starting_price),
    CONSTRAINT ck_auctions_bid_increment_positive CHECK (bid_increment > 0),
    CONSTRAINT ck_auctions_duration_valid CHECK (ends_at > starts_at)
);

CREATE INDEX ix_auctions_seller_created_at ON auctions (seller_id, created_at DESC);
CREATE INDEX ix_auctions_status_ends_at ON auctions (status, ends_at);

CREATE TABLE bids (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id UUID NOT NULL REFERENCES auctions(id),
    bidder_id UUID NOT NULL REFERENCES app_users(id),
    amount NUMERIC(19,4) NOT NULL,
    placed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_id UUID NOT NULL,
    CONSTRAINT ck_bids_amount_positive CHECK (amount > 0),
    CONSTRAINT uq_bids_auction_request UNIQUE (auction_id, request_id)
);

CREATE INDEX ix_bids_auction_amount_desc ON bids (auction_id, amount DESC, placed_at ASC);
CREATE INDEX ix_bids_bidder_placed_at ON bids (bidder_id, placed_at DESC);

ALTER TABLE auctions
    ADD COLUMN current_bid_id UUID NULL REFERENCES bids(id),
    ADD COLUMN current_bidder_id UUID NULL REFERENCES app_users(id);

CREATE INDEX ix_auctions_current_bidder_id ON auctions (current_bidder_id);

CREATE FUNCTION set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_app_users_set_updated_at
    BEFORE UPDATE ON app_users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_auctions_set_updated_at
    BEFORE UPDATE ON auctions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id UUID NOT NULL UNIQUE REFERENCES auctions(id),
    payer_id UUID NOT NULL REFERENCES app_users(id),
    payee_id UUID NOT NULL REFERENCES app_users(id),
    amount NUMERIC(19,4) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider VARCHAR(50),
    provider_payment_id VARCHAR(255),
    idempotency_key UUID NOT NULL UNIQUE,
    failure_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'REFUNDED', 'CANCELLED')),
    CONSTRAINT ck_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_payments_currency_uppercase CHECK (currency = upper(currency)),
    CONSTRAINT ck_payments_distinct_parties CHECK (payer_id <> payee_id)
);

CREATE UNIQUE INDEX uq_payments_provider_payment ON payments (provider, provider_payment_id)
    WHERE provider_payment_id IS NOT NULL;
CREATE INDEX ix_payments_payer_created_at ON payments (payer_id, created_at DESC);

CREATE TRIGGER trg_payments_set_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_outbox_events_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    CONSTRAINT ck_outbox_events_attempts_non_negative CHECK (attempts >= 0)
);

CREATE INDEX ix_outbox_events_dispatch ON outbox_events (status, available_at, created_at)
    WHERE status = 'PENDING';
CREATE INDEX ix_outbox_events_aggregate ON outbox_events (aggregate_type, aggregate_id, created_at);

CREATE TABLE processed_events (
    consumer_name VARCHAR(100) NOT NULL,
    event_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (consumer_name, event_id)
);
