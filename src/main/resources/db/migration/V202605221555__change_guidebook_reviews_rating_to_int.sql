ALTER TABLE guidebook_reviews
    ALTER COLUMN rating TYPE INTEGER USING rating::INTEGER;