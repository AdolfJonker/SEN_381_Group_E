-- NFR004: status_history is append-only at the database level (PostgreSQL only).
-- Demo/H2 skips this folder; see Technical Debt TD-006.

ALTER TABLE status_history ALTER COLUMN from_status DROP NOT NULL;

CREATE OR REPLACE FUNCTION prevent_status_history_mutation()
RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'status_history is append-only (NFR004)';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_status_history_append_only
  BEFORE UPDATE OR DELETE ON status_history
  FOR EACH ROW
  EXECUTE FUNCTION prevent_status_history_mutation();
