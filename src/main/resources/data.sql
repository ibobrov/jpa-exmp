-- JPA demo data
INSERT INTO customers(email, full_name)
VALUES ('alice@example.com', 'Alice Johnson')
ON CONFLICT (email) DO NOTHING;

INSERT INTO customers(email, full_name)
VALUES ('bob@example.com', 'Bob Smith')
ON CONFLICT (email) DO NOTHING;

-- Minimal picking task data (всё на константах)
INSERT INTO shipments(shipment_id, number)
VALUES ('22222222-2222-2222-2222-222222222222', 'SHP-0001')
ON CONFLICT (shipment_id) DO NOTHING;

INSERT INTO picking_tasks(picking_task_id, task_number, status, performer_id, warehouse_id,
                          priority, picking_deadline_date, started_at, picking_zone_id, buffers,
                          creator_id, created_at, sort_method_id, completed_at, sub_mode)
VALUES ('11111111-1111-1111-1111-111111111111', 'TASK-0001', 'IN_PROGRESS', NULL,
        '55555555-5555-5555-5555-555555555555',
        'HIGH', now() + interval '1 day', now(), '33333333-3333-3333-3333-333333333333', ARRAY ['BUF-A','BUF-B'],
        '66666666-6666-6666-6666-666666666666', now(), '44444444-4444-4444-4444-444444444444', NULL, 'UTILIZATION')
ON CONFLICT (picking_task_id) DO NOTHING;

INSERT INTO picking_task_bodies(picking_task_body_id, picking_task_id, goods_id, shipment_id, picking_cell_id,
                                quantity, cancelled_quantity, shortage_reason, updated_at, handling_unit_barcode,
                                stock_quality, is_nlo)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111',
        '10101010-1010-1010-1010-101010101010',
        '22222222-2222-2222-2222-222222222222', '90909090-9090-9090-9090-909090909090', 5, 0, NULL, now(), NULL, 'GOOD',
        false),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111',
        '20202020-2020-2020-2020-202020202020',
        '22222222-2222-2222-2222-222222222222', '80808080-8080-8080-8080-808080808080', 3, 1, NULL, now(), NULL, 'GOOD',
        false)
ON CONFLICT (picking_task_body_id) DO NOTHING;

INSERT INTO picking_task_handling_units(picking_task_handling_unit_id, picking_task_id, target_cell_id, status, barcode,
                                        created_at)
VALUES ('77777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111',
        '70707070-7070-7070-7070-707070707070', 'CREATED', 'HU-0001', now())
ON CONFLICT (picking_task_handling_unit_id) DO NOTHING;

-- id для picking_task_handling_unit_bodies тоже задаём константой
INSERT INTO picking_task_handling_unit_bodies(id, picking_task_id, picking_task_handling_unit_id, picking_task_body_id,
                                              quantity, created_at)
VALUES ('99999999-9999-9999-9999-999999999999',
        '11111111-1111-1111-1111-111111111111',
        '77777777-7777-7777-7777-777777777777',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        2,
        now())
ON CONFLICT (id) DO NOTHING;