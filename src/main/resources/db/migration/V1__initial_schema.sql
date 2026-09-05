-- ============================================================================
-- Flyway Migration V1: Initial Schema for Coresto Restaurant Platform
-- PostgreSQL 16+ compatible DDL with strict relational constraints and indexes
-- ============================================================================

-- ─── 1. Tenancy & Base Assets ────────────────────────────────────────────────

CREATE TABLE media_assets (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    url                TEXT NOT NULL,
    mime_type          VARCHAR(255),
    original_filename  VARCHAR(255),
    byte_size          BIGINT,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_media_assets PRIMARY KEY (id)
);

CREATE TABLE organisations (
    id                 UUID NOT NULL,
    name               VARCHAR(255) NOT NULL,
    alias              VARCHAR(255) NOT NULL UNIQUE,
    enabled            BOOLEAN NOT NULL DEFAULT TRUE,
    type               VARCHAR(50),
    status             VARCHAR(50),
    logo_id            UUID,
    country            VARCHAR(255),
    timezone           VARCHAR(255),
    default_currency   VARCHAR(10),
    default_locale     VARCHAR(10),
    tax_id             VARCHAR(255),
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_organisations PRIMARY KEY (id)
);

CREATE TABLE organisation_domains (
    organisation_id    UUID NOT NULL,
    domain             VARCHAR(255) NOT NULL,
    CONSTRAINT pk_organisation_domains PRIMARY KEY (organisation_id, domain)
);

CREATE TABLE organisation_settings (
    organisation_id        UUID NOT NULL,
    auto_accept_orders     BOOLEAN NOT NULL DEFAULT FALSE,
    tax_inclusive_pricing  BOOLEAN NOT NULL DEFAULT TRUE,
    pay_before_prepare     BOOLEAN NOT NULL DEFAULT FALSE,
    tipping_enabled        BOOLEAN NOT NULL DEFAULT FALSE,
    guest_notes_enabled    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ,
    CONSTRAINT pk_organisation_settings PRIMARY KEY (organisation_id)
);

CREATE TABLE organisation_service_modes (
    organisation_id    UUID NOT NULL,
    service_mode       VARCHAR(50) NOT NULL,
    CONSTRAINT pk_organisation_service_modes PRIMARY KEY (organisation_id, service_mode)
);

CREATE TABLE branches (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    name               VARCHAR(255) NOT NULL,
    line1              VARCHAR(255),
    line2              VARCHAR(255),
    city               VARCHAR(255),
    region             VARCHAR(255),
    postal_code        VARCHAR(255),
    country            VARCHAR(255),
    latitude           NUMERIC(38, 2),
    longitude          NUMERIC(38, 2),
    phone              VARCHAR(255),
    email              VARCHAR(255),
    timezone           VARCHAR(255),
    status             VARCHAR(50),
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_branches PRIMARY KEY (id),
    CONSTRAINT uk_branches_org_name UNIQUE (organisation_id, name)
);

CREATE TABLE branch_service_modes (
    branch_id          UUID NOT NULL,
    service_mode       VARCHAR(50) NOT NULL,
    CONSTRAINT pk_branch_service_modes PRIMARY KEY (branch_id, service_mode)
);

CREATE TABLE opening_hours (
    id                 UUID NOT NULL,
    branch_id          UUID NOT NULL,
    day_of_week        VARCHAR(10) NOT NULL,
    open_time          TIME(0),
    close_time         TIME(0),
    closed             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_opening_hours PRIMARY KEY (id),
    CONSTRAINT uk_opening_hours_branch_day UNIQUE (branch_id, day_of_week)
);

CREATE TABLE dining_tables (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    branch_id          UUID NOT NULL,
    code               VARCHAR(255) NOT NULL,
    zone_name          VARCHAR(255),
    seats              INTEGER NOT NULL,
    status             VARCHAR(50),
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_dining_tables PRIMARY KEY (id),
    CONSTRAINT uk_dining_tables_branch_code UNIQUE (branch_id, code)
);

CREATE TABLE qr_codes (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    branch_id          UUID NOT NULL,
    table_id           UUID,
    menu_id            UUID,
    public_token       VARCHAR(255) NOT NULL UNIQUE,
    target_type        VARCHAR(50) NOT NULL,
    active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_qr_codes PRIMARY KEY (id)
);

-- ─── 2. Identity & Memberships ────────────────────────────────────────────────

CREATE TABLE users (
    id                 UUID NOT NULL,
    email              VARCHAR(255) NOT NULL,
    email_normalized   VARCHAR(255) NOT NULL,
    phone              VARCHAR(255),
    first_name         VARCHAR(255),
    last_name          VARCHAR(255),
    avatar_url         VARCHAR(255),
    status             VARCHAR(50) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email_normalized UNIQUE (email_normalized)
);

CREATE TABLE organisation_members (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    user_id            UUID NOT NULL,
    role               VARCHAR(50) NOT NULL,
    status             VARCHAR(50) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_organisation_members PRIMARY KEY (id),
    CONSTRAINT uk_organisation_members_org_user UNIQUE (organisation_id, user_id)
);

CREATE TABLE member_branch_access (
    id                 UUID NOT NULL,
    member_id          UUID NOT NULL,
    branch_id          UUID NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_member_branch_access PRIMARY KEY (id),
    CONSTRAINT uk_member_branch_access_member_branch UNIQUE (member_id, branch_id)
);

CREATE TABLE invitations (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    email              VARCHAR(255) NOT NULL,
    role               VARCHAR(50) NOT NULL,
    token              VARCHAR(255) NOT NULL,
    expires_at         TIMESTAMPTZ NOT NULL,
    status             VARCHAR(50) NOT NULL,
    invited_by         UUID NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_invitations PRIMARY KEY (id),
    CONSTRAINT uk_invitations_token UNIQUE (token)
);

CREATE TABLE invitation_branches (
    id                 UUID NOT NULL,
    invitation_id      UUID NOT NULL,
    branch_id          UUID NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_invitation_branches PRIMARY KEY (id),
    CONSTRAINT uk_invitation_branches_invitation_branch UNIQUE (invitation_id, branch_id)
);

-- ─── 3. Platform Plans & Subscriptions ─────────────────────────────────────────

CREATE TABLE plans (
    id                 UUID NOT NULL,
    code               VARCHAR(255) NOT NULL UNIQUE,
    name               VARCHAR(255) NOT NULL,
    max_branches       INTEGER,
    max_menus          INTEGER,
    max_products       INTEGER,
    active             BOOLEAN DEFAULT TRUE,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_plans PRIMARY KEY (id)
);

CREATE TABLE subscriptions (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL UNIQUE,
    plan_id            UUID NOT NULL,
    status             VARCHAR(50),
    current_period_end TIMESTAMPTZ,
    trial_ends_at      TIMESTAMPTZ,
    provider_ref       VARCHAR(255),
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_subscriptions PRIMARY KEY (id)
);

-- ─── 4. Catalog Management ───────────────────────────────────────────────────

CREATE TABLE menus (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    status             VARCHAR(50),
    start_time         TIME(0),
    end_time           TIME(0),
    display_order      INTEGER,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_menus PRIMARY KEY (id)
);

CREATE TABLE menu_days (
    menu_id            UUID NOT NULL,
    day_of_week        VARCHAR(20) NOT NULL,
    CONSTRAINT pk_menu_days PRIMARY KEY (menu_id, day_of_week)
);

CREATE TABLE menu_branches (
    id                 UUID NOT NULL,
    menu_id            UUID NOT NULL,
    branch_id          UUID NOT NULL,
    active             BOOLEAN DEFAULT TRUE,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_menu_branches PRIMARY KEY (id),
    CONSTRAINT uk_menu_branches UNIQUE (menu_id, branch_id)
);

CREATE TABLE categories (
    id                 UUID NOT NULL,
    menu_id            UUID NOT NULL,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    image_id           UUID,
    display_order      INTEGER,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE ingredients (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    name               VARCHAR(255) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_ingredients PRIMARY KEY (id),
    CONSTRAINT uk_ingredients_org_name UNIQUE (organisation_id, name)
);

CREATE TABLE products (
    id                  UUID NOT NULL,
    organisation_id     UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    sku                 VARCHAR(255),
    base_price_amount   NUMERIC(12, 3) NOT NULL,
    base_price_currency VARCHAR(10) NOT NULL,
    tax_rate            NUMERIC(5, 2),
    image_id            UUID,
    status              VARCHAR(50),
    prep_minutes        INTEGER,
    featured            BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE product_allergens (
    product_id          UUID NOT NULL,
    allergen            VARCHAR(50) NOT NULL,
    CONSTRAINT pk_product_allergens PRIMARY KEY (product_id, allergen)
);

CREATE TABLE product_dietary_tags (
    product_id          UUID NOT NULL,
    dietary_tag         VARCHAR(50) NOT NULL,
    CONSTRAINT pk_product_dietary_tags PRIMARY KEY (product_id, dietary_tag)
);

CREATE TABLE product_variants (
    id                      UUID NOT NULL,
    product_id              UUID NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    sku                     VARCHAR(255),
    price_override_amount   NUMERIC(12, 3),
    price_override_currency VARCHAR(10),
    default_variant         BOOLEAN DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ,
    CONSTRAINT pk_product_variants PRIMARY KEY (id),
    CONSTRAINT uk_product_variants_prod_name UNIQUE (product_id, name)
);

CREATE TABLE product_ingredients (
    id                 UUID NOT NULL,
    product_id         UUID NOT NULL,
    ingredient_id      UUID NOT NULL,
    kind               VARCHAR(50),
    removable          BOOLEAN DEFAULT FALSE,
    display_order      INTEGER,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_product_ingredients PRIMARY KEY (id),
    CONSTRAINT uk_product_ingredients UNIQUE (product_id, ingredient_id)
);

CREATE TABLE product_categories (
    id                 UUID NOT NULL,
    product_id         UUID NOT NULL,
    category_id        UUID NOT NULL,
    display_order      INTEGER,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_product_categories PRIMARY KEY (id),
    CONSTRAINT uk_product_categories UNIQUE (product_id, category_id)
);

CREATE TABLE product_branch_availability (
    id                 UUID NOT NULL,
    product_id         UUID NOT NULL,
    branch_id          UUID NOT NULL,
    status             VARCHAR(50),
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_product_branch_availability PRIMARY KEY (id),
    CONSTRAINT uk_product_branch_availability UNIQUE (product_id, branch_id)
);

CREATE TABLE modifier_groups (
    id                 UUID NOT NULL,
    product_id         UUID NOT NULL,
    name               VARCHAR(255) NOT NULL,
    selection_type     VARCHAR(50),
    min_select         INTEGER,
    max_select         INTEGER,
    required           BOOLEAN DEFAULT FALSE,
    display_order      INTEGER,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_modifier_groups PRIMARY KEY (id)
);

CREATE TABLE modifier_options (
    id                   UUID NOT NULL,
    group_id             UUID NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    extra_price_amount   NUMERIC(12, 3),
    extra_price_currency VARCHAR(10),
    linked_ingredient_id UUID,
    default_selected     BOOLEAN DEFAULT FALSE,
    available            BOOLEAN DEFAULT TRUE,
    display_order        INTEGER,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ,
    CONSTRAINT pk_modifier_options PRIMARY KEY (id),
    CONSTRAINT uk_modifier_options_group_name UNIQUE (group_id, name)
);

CREATE TABLE translations (
    id                 UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    entity_type        VARCHAR(255) NOT NULL,
    entity_id          UUID NOT NULL,
    field              VARCHAR(255) NOT NULL,
    locale             VARCHAR(10) NOT NULL,
    value              TEXT NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_translations PRIMARY KEY (id),
    CONSTRAINT uk_translations UNIQUE (entity_type, entity_id, field, locale)
);

-- ─── 5. Ordering & Guest Flow ─────────────────────────────────────────────────

CREATE TABLE guest_devices (
    id                 UUID NOT NULL,
    public_token       VARCHAR(255) NOT NULL UNIQUE,
    phone_e164         VARCHAR(255) UNIQUE,
    email_normalized   VARCHAR(255) UNIQUE,
    fingerprint_hash   VARCHAR(255),
    user_agent         TEXT,
    language           VARCHAR(255),
    last_seen_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_guest_devices PRIMARY KEY (id)
);

CREATE TABLE dining_sessions (
    id                 UUID NOT NULL,
    guest_device_id    UUID NOT NULL,
    organisation_id    UUID NOT NULL,
    branch_id          UUID NOT NULL,
    table_id           UUID,
    qr_code_id         UUID,
    started_at         TIMESTAMPTZ,
    expires_at         TIMESTAMPTZ,
    status             VARCHAR(50),
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_dining_sessions PRIMARY KEY (id)
);

CREATE TABLE carts (
    id                 UUID NOT NULL,
    session_id         UUID NOT NULL UNIQUE,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_carts PRIMARY KEY (id)
);

CREATE TABLE cart_items (
    id                 UUID NOT NULL,
    cart_id            UUID NOT NULL,
    product_id         UUID NOT NULL,
    variant_id         UUID,
    quantity           INTEGER NOT NULL,
    notes              TEXT,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_cart_items PRIMARY KEY (id)
);

CREATE TABLE cart_item_modifiers (
    id                 UUID NOT NULL,
    cart_item_id       UUID NOT NULL,
    modifier_option_id UUID NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_cart_item_modifiers PRIMARY KEY (id),
    CONSTRAINT uk_cart_item_modifiers UNIQUE (cart_item_id, modifier_option_id)
);

CREATE TABLE cart_removed_ingredients (
    id                 UUID NOT NULL,
    cart_item_id       UUID NOT NULL,
    ingredient_id      UUID NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_cart_removed_ingredients PRIMARY KEY (id),
    CONSTRAINT uk_cart_removed_ingredients UNIQUE (cart_item_id, ingredient_id)
);

CREATE TABLE orders (
    id                  UUID NOT NULL,
    organisation_id     UUID NOT NULL,
    branch_id           UUID NOT NULL,
    table_id            UUID,
    session_id          UUID,
    guest_device_id     UUID NOT NULL,
    assigned_worker_id  UUID,
    public_number       VARCHAR(255) NOT NULL,
    service_mode        VARCHAR(50),
    status              VARCHAR(50),
    notes               TEXT,
    subtotal_amount     NUMERIC(12, 3),
    subtotal_currency   VARCHAR(10),
    tax_amount          NUMERIC(12, 3),
    tax_currency        VARCHAR(10),
    discount_amount     NUMERIC(12, 3),
    discount_currency   VARCHAR(10),
    tip_amount          NUMERIC(12, 3),
    tip_currency        VARCHAR(10),
    total_amount        NUMERIC(12, 3),
    total_currency      VARCHAR(10),
    placed_at           TIMESTAMPTZ,
    estimated_ready_at  TIMESTAMPTZ,
    cancellation_reason TEXT,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uk_orders_branch_public_number UNIQUE (branch_id, public_number)
);

CREATE TABLE order_items (
    id                   UUID NOT NULL,
    order_id             UUID NOT NULL,
    product_id           UUID,
    variant_id           UUID,
    product_name         VARCHAR(255) NOT NULL,
    variant_name         VARCHAR(255),
    item_status          VARCHAR(50),
    quantity             INTEGER,
    unit_price_amount    NUMERIC(12, 3) NOT NULL,
    unit_price_currency  VARCHAR(10) NOT NULL,
    line_total_amount    NUMERIC(12, 3) NOT NULL,
    line_total_currency  VARCHAR(10) NOT NULL,
    notes                TEXT,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ,
    CONSTRAINT pk_order_items PRIMARY KEY (id)
);

CREATE TABLE order_item_modifiers (
    id                            UUID NOT NULL,
    order_item_id                 UUID NOT NULL,
    modifier_option_id            UUID,
    name_snapshot                 VARCHAR(255) NOT NULL,
    extra_price_snapshot_amount   NUMERIC(12, 3),
    extra_price_snapshot_currency VARCHAR(10),
    created_at                    TIMESTAMPTZ NOT NULL,
    updated_at                    TIMESTAMPTZ,
    CONSTRAINT pk_order_item_modifiers PRIMARY KEY (id)
);

CREATE TABLE order_item_removed_ingredients (
    id                   UUID NOT NULL,
    order_item_id        UUID NOT NULL,
    ingredient_id        UUID,
    name_snapshot        VARCHAR(255) NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ,
    CONSTRAINT pk_order_item_removed_ingredients PRIMARY KEY (id)
);

CREATE TABLE payments (
    id                 UUID NOT NULL,
    order_id           UUID NOT NULL,
    amount             NUMERIC(12, 3),
    currency           VARCHAR(10),
    method             VARCHAR(50),
    provider           VARCHAR(50),
    provider_ref       VARCHAR(255),
    status             VARCHAR(50),
    paid_at            TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

-- ─── 6. Platform Audit Logs ───────────────────────────────────────────────────

CREATE TABLE audit_logs (
    id                 UUID NOT NULL,
    organisation_id    UUID,
    actor_user_id      UUID,
    guest_device_id    UUID,
    action             VARCHAR(50),
    entity_type        VARCHAR(255),
    entity_id          UUID,
    payload            JSONB,
    created_at         TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

-- ─── 7. Indexes ───────────────────────────────────────────────────────────────

CREATE INDEX idx_audit_logs_org_created ON audit_logs (organisation_id, created_at DESC);
CREATE INDEX idx_orders_branch_status_placed ON orders (branch_id, status, placed_at DESC);
CREATE INDEX idx_orders_guest_device_placed ON orders (guest_device_id, placed_at DESC);
CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_qr_codes_public_token ON qr_codes (public_token);

-- ─── 8. Foreign Key Constraints ───────────────────────────────────────────────

-- Organisations & Media
ALTER TABLE media_assets ADD CONSTRAINT fk_media_assets_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE organisations ADD CONSTRAINT fk_organisations_logo
    FOREIGN KEY (logo_id) REFERENCES media_assets (id);

ALTER TABLE organisation_domains ADD CONSTRAINT fk_org_domains_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id) ON DELETE CASCADE;

ALTER TABLE organisation_settings ADD CONSTRAINT fk_org_settings_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id) ON DELETE CASCADE;

ALTER TABLE organisation_service_modes ADD CONSTRAINT fk_org_service_modes_org
    FOREIGN KEY (organisation_id) REFERENCES organisation_settings (organisation_id) ON DELETE CASCADE;

-- Branches
ALTER TABLE branches ADD CONSTRAINT fk_branches_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE branch_service_modes ADD CONSTRAINT fk_branch_service_modes_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE CASCADE;

ALTER TABLE opening_hours ADD CONSTRAINT fk_opening_hours_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE CASCADE;

ALTER TABLE dining_tables ADD CONSTRAINT fk_dining_tables_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id);

ALTER TABLE dining_tables ADD CONSTRAINT fk_dining_tables_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

-- Memberships & Identity
ALTER TABLE organisation_members ADD CONSTRAINT fk_org_members_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE organisation_members ADD CONSTRAINT fk_org_members_user
    FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE member_branch_access ADD CONSTRAINT fk_member_branch_access_member
    FOREIGN KEY (member_id) REFERENCES organisation_members (id) ON DELETE CASCADE;

ALTER TABLE member_branch_access ADD CONSTRAINT fk_member_branch_access_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE CASCADE;

ALTER TABLE invitations ADD CONSTRAINT fk_invitations_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE invitations ADD CONSTRAINT fk_invitations_invited_by
    FOREIGN KEY (invited_by) REFERENCES users (id);

ALTER TABLE invitation_branches ADD CONSTRAINT fk_invitation_branches_invitation
    FOREIGN KEY (invitation_id) REFERENCES invitations (id) ON DELETE CASCADE;

ALTER TABLE invitation_branches ADD CONSTRAINT fk_invitation_branches_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE CASCADE;

-- Subscriptions
ALTER TABLE subscriptions ADD CONSTRAINT fk_subscriptions_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE subscriptions ADD CONSTRAINT fk_subscriptions_plan
    FOREIGN KEY (plan_id) REFERENCES plans (id);

-- Menus & Categories
ALTER TABLE menus ADD CONSTRAINT fk_menus_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE menu_days ADD CONSTRAINT fk_menu_days_menu
    FOREIGN KEY (menu_id) REFERENCES menus (id) ON DELETE CASCADE;

ALTER TABLE menu_branches ADD CONSTRAINT fk_menu_branches_menu
    FOREIGN KEY (menu_id) REFERENCES menus (id);

ALTER TABLE menu_branches ADD CONSTRAINT fk_menu_branches_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id);

ALTER TABLE categories ADD CONSTRAINT fk_categories_menu
    FOREIGN KEY (menu_id) REFERENCES menus (id);

ALTER TABLE categories ADD CONSTRAINT fk_categories_image
    FOREIGN KEY (image_id) REFERENCES media_assets (id);

-- Products & Variants
ALTER TABLE products ADD CONSTRAINT fk_products_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE products ADD CONSTRAINT fk_products_image
    FOREIGN KEY (image_id) REFERENCES media_assets (id);

ALTER TABLE product_allergens ADD CONSTRAINT fk_product_allergens_product
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

ALTER TABLE product_dietary_tags ADD CONSTRAINT fk_product_dietary_tags_product
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

ALTER TABLE product_variants ADD CONSTRAINT fk_product_variants_product
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

ALTER TABLE ingredients ADD CONSTRAINT fk_ingredients_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE product_ingredients ADD CONSTRAINT fk_product_ingredients_product
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

ALTER TABLE product_ingredients ADD CONSTRAINT fk_product_ingredients_ingredient
    FOREIGN KEY (ingredient_id) REFERENCES ingredients (id);

ALTER TABLE product_categories ADD CONSTRAINT fk_product_categories_product
    FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE product_categories ADD CONSTRAINT fk_product_categories_category
    FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE product_branch_availability ADD CONSTRAINT fk_product_branch_avail_product
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

ALTER TABLE product_branch_availability ADD CONSTRAINT fk_product_branch_avail_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id);

-- Modifiers
ALTER TABLE modifier_groups ADD CONSTRAINT fk_modifier_groups_product
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

ALTER TABLE modifier_options ADD CONSTRAINT fk_modifier_options_group
    FOREIGN KEY (group_id) REFERENCES modifier_groups (id) ON DELETE CASCADE;

ALTER TABLE modifier_options ADD CONSTRAINT fk_modifier_options_ingredient
    FOREIGN KEY (linked_ingredient_id) REFERENCES ingredients (id);

-- Translations & QR
ALTER TABLE translations ADD CONSTRAINT fk_translations_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE qr_codes ADD CONSTRAINT fk_qr_codes_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE qr_codes ADD CONSTRAINT fk_qr_codes_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id);

ALTER TABLE qr_codes ADD CONSTRAINT fk_qr_codes_table
    FOREIGN KEY (table_id) REFERENCES dining_tables (id);

ALTER TABLE qr_codes ADD CONSTRAINT fk_qr_codes_menu
    FOREIGN KEY (menu_id) REFERENCES menus (id);

-- Dining Sessions & Carts
ALTER TABLE dining_sessions ADD CONSTRAINT fk_dining_sessions_device
    FOREIGN KEY (guest_device_id) REFERENCES guest_devices (id);

ALTER TABLE dining_sessions ADD CONSTRAINT fk_dining_sessions_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE dining_sessions ADD CONSTRAINT fk_dining_sessions_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id);

ALTER TABLE dining_sessions ADD CONSTRAINT fk_dining_sessions_table
    FOREIGN KEY (table_id) REFERENCES dining_tables (id);

ALTER TABLE dining_sessions ADD CONSTRAINT fk_dining_sessions_qr
    FOREIGN KEY (qr_code_id) REFERENCES qr_codes (id);

ALTER TABLE carts ADD CONSTRAINT fk_carts_session
    FOREIGN KEY (session_id) REFERENCES dining_sessions (id) ON DELETE CASCADE;

ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart
    FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE;

ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_product
    FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_variant
    FOREIGN KEY (variant_id) REFERENCES product_variants (id);

ALTER TABLE cart_item_modifiers ADD CONSTRAINT fk_cart_item_mod_item
    FOREIGN KEY (cart_item_id) REFERENCES cart_items (id) ON DELETE CASCADE;

ALTER TABLE cart_item_modifiers ADD CONSTRAINT fk_cart_item_mod_option
    FOREIGN KEY (modifier_option_id) REFERENCES modifier_options (id);

ALTER TABLE cart_removed_ingredients ADD CONSTRAINT fk_cart_rem_ing_item
    FOREIGN KEY (cart_item_id) REFERENCES cart_items (id) ON DELETE CASCADE;

ALTER TABLE cart_removed_ingredients ADD CONSTRAINT fk_cart_rem_ing_ingredient
    FOREIGN KEY (ingredient_id) REFERENCES ingredients (id);

-- Orders, Items, Payments
ALTER TABLE orders ADD CONSTRAINT fk_orders_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE orders ADD CONSTRAINT fk_orders_branch
    FOREIGN KEY (branch_id) REFERENCES branches (id);

ALTER TABLE orders ADD CONSTRAINT fk_orders_table
    FOREIGN KEY (table_id) REFERENCES dining_tables (id);

ALTER TABLE orders ADD CONSTRAINT fk_orders_session
    FOREIGN KEY (session_id) REFERENCES dining_sessions (id);

ALTER TABLE orders ADD CONSTRAINT fk_orders_device
    FOREIGN KEY (guest_device_id) REFERENCES guest_devices (id);

ALTER TABLE orders ADD CONSTRAINT fk_orders_assigned_worker
    FOREIGN KEY (assigned_worker_id) REFERENCES users (id);

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_variant
    FOREIGN KEY (variant_id) REFERENCES product_variants (id);

ALTER TABLE order_item_modifiers ADD CONSTRAINT fk_order_item_mod_item
    FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE CASCADE;

ALTER TABLE order_item_modifiers ADD CONSTRAINT fk_order_item_mod_option
    FOREIGN KEY (modifier_option_id) REFERENCES modifier_options (id);

ALTER TABLE order_item_removed_ingredients ADD CONSTRAINT fk_order_rem_ing_item
    FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE CASCADE;

ALTER TABLE order_item_removed_ingredients ADD CONSTRAINT fk_order_rem_ing_ingredient
    FOREIGN KEY (ingredient_id) REFERENCES ingredients (id);

ALTER TABLE payments ADD CONSTRAINT fk_payments_order
    FOREIGN KEY (order_id) REFERENCES orders (id);

-- Audit Logs
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_org
    FOREIGN KEY (organisation_id) REFERENCES organisations (id);

ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_user
    FOREIGN KEY (actor_user_id) REFERENCES users (id);

ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_device
    FOREIGN KEY (guest_device_id) REFERENCES guest_devices (id);
