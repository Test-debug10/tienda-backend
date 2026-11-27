-- Restricciones CHECK de la base tienda_gamer (PostgreSQL).
-- Hibernate las crea solo cuando la tabla es nueva; este script las agrega a una base que ya existía.
-- Se puede correr varias veces: borra cada restricción si existe y la vuelve a crear.
-- Uso en la instancia: sudo -u postgres psql -d tienda_gamer -f restricciones.sql

BEGIN;

ALTER TABLE productos DROP CONSTRAINT IF EXISTS ck_productos_precio;
ALTER TABLE productos ADD CONSTRAINT ck_productos_precio CHECK (precio > 0);
ALTER TABLE productos DROP CONSTRAINT IF EXISTS ck_productos_precio_oferta;
ALTER TABLE productos ADD CONSTRAINT ck_productos_precio_oferta CHECK (precio_oferta IS NULL OR precio_oferta > 0);
ALTER TABLE productos DROP CONSTRAINT IF EXISTS ck_productos_stock;
ALTER TABLE productos ADD CONSTRAINT ck_productos_stock CHECK (stock >= 0);
ALTER TABLE productos DROP CONSTRAINT IF EXISTS ck_productos_stock_critico;
ALTER TABLE productos ADD CONSTRAINT ck_productos_stock_critico CHECK (stock_critico >= 0);

ALTER TABLE detalle_orden DROP CONSTRAINT IF EXISTS ck_detalle_cantidad;
ALTER TABLE detalle_orden ADD CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0);
ALTER TABLE detalle_orden DROP CONSTRAINT IF EXISTS ck_detalle_precio;
ALTER TABLE detalle_orden ADD CONSTRAINT ck_detalle_precio CHECK (precio_unitario > 0);
ALTER TABLE detalle_orden DROP CONSTRAINT IF EXISTS ck_detalle_subtotal;
ALTER TABLE detalle_orden ADD CONSTRAINT ck_detalle_subtotal CHECK (subtotal = cantidad * precio_unitario);

ALTER TABLE ordenes DROP CONSTRAINT IF EXISTS ck_ordenes_total;
ALTER TABLE ordenes ADD CONSTRAINT ck_ordenes_total CHECK (total >= 0);

COMMIT;

-- Ver las restricciones de la base (CHECK, llaves primarias, foráneas y UNIQUE):
-- SELECT conrelid::regclass AS tabla, conname, pg_get_constraintdef(oid) FROM pg_constraint
-- WHERE connamespace = 'public'::regnamespace ORDER BY 1, 2;
