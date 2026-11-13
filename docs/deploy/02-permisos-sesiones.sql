-- Supabase: ejecutar tras 01-sesiones.sql, con el propietario de las tablas.
-- No exponer ecouvg_private en la Data API de Supabase.
BEGIN;
REVOKE ALL ON SCHEMA ecouvg_private FROM PUBLIC, anon, authenticated;
REVOKE ALL ON ALL TABLES IN SCHEMA ecouvg_private FROM PUBLIC, anon, authenticated;
COMMIT;
-- Si DB_USERNAME no es el propietario (p. ej. postgres), conceder manualmente
-- USAGE en este esquema y SELECT/INSERT/UPDATE/DELETE en sus tablas al rol JDBC.
-- No conceder acceso a anon o authenticated: contienen sesiones serializadas.
