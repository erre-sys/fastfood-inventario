-- Units are never deleted: they are inactivated. Who changed a unit and when, for auditing.
-- created_by and created_at keep their defaults for the standard units; the service fills them
-- for new units from the user in the token.
ALTER TABLE unit ADD COLUMN active     BOOLEAN      NOT NULL DEFAULT TRUE;
ALTER TABLE unit ADD COLUMN updated_by VARCHAR(100);
ALTER TABLE unit ADD COLUMN updated_at TIMESTAMPTZ;
