ALTER TABLE verification_tokens ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE password_reset_tokens ALTER COLUMN id SET DEFAULT gen_random_uuid();