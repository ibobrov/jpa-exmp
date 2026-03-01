CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS shipments
(
    shipment_id UUID PRIMARY KEY,
    number      TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS picking_tasks
(
    picking_task_id       UUID PRIMARY KEY,
    task_number           TEXT        NOT NULL,
    status                TEXT        NOT NULL,
    performer_id          UUID        NULL,
    warehouse_id          UUID        NOT NULL,
    priority              TEXT        NOT NULL,
    picking_deadline_date TIMESTAMPTZ NOT NULL,
    started_at            TIMESTAMPTZ NULL,
    picking_zone_id       UUID        NOT NULL,
    buffers               TEXT[]      NULL,
    creator_id            UUID        NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL,
    sort_method_id        UUID        NOT NULL,
    completed_at          TIMESTAMPTZ NULL,
    sub_mode              TEXT        NULL
);

CREATE TABLE IF NOT EXISTS picking_task_bodies
(
    picking_task_body_id  UUID PRIMARY KEY,
    picking_task_id       UUID        NOT NULL REFERENCES picking_tasks (picking_task_id),
    goods_id              UUID        NOT NULL,
    shipment_id           UUID        NOT NULL REFERENCES shipments (shipment_id),
    picking_cell_id       UUID        NOT NULL,
    quantity              INT         NOT NULL,
    cancelled_quantity    INT         NOT NULL DEFAULT 0,
    shortage_reason       TEXT        NULL,
    updated_at            TIMESTAMPTZ NULL,
    handling_unit_barcode TEXT        NULL,
    stock_quality         TEXT        NOT NULL,
    is_nlo                BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS picking_task_handling_units
(
    picking_task_handling_unit_id UUID PRIMARY KEY,
    picking_task_id               UUID        NOT NULL REFERENCES picking_tasks (picking_task_id),
    target_cell_id                UUID        NULL,
    status                        TEXT        NOT NULL,
    barcode                       TEXT        NOT NULL,
    created_at                    TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS picking_task_handling_unit_bodies
(
    id                            UUID PRIMARY KEY,
    picking_task_id               UUID        NOT NULL REFERENCES picking_tasks (picking_task_id),
    picking_task_handling_unit_id UUID        NOT NULL REFERENCES picking_task_handling_units (picking_task_handling_unit_id),
    picking_task_body_id          UUID        NOT NULL REFERENCES picking_task_bodies (picking_task_body_id),
    quantity                      INT         NOT NULL,
    created_at                    TIMESTAMPTZ NOT NULL
);

/* JPA demo table (оставим как пример JPA слоя) */
CREATE TABLE IF NOT EXISTS customers
(
    id         BIGSERIAL PRIMARY KEY,
    email      TEXT        NOT NULL UNIQUE,
    full_name  TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);