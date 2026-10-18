-- Allow the initial status-history event to have no previous status.
ALTER TABLE status_history
    ALTER COLUMN from_status DROP NOT NULL;
