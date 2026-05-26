-- Add admin_hidden column to guidebooks for admin-driven hiding
ALTER TABLE guidebooks
    ADD COLUMN admin_hidden BOOLEAN NOT NULL DEFAULT FALSE;
