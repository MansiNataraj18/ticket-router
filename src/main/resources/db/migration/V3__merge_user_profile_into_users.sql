-- Collapses user_profile into users: tickets are now owned directly by a
-- users row instead of a separate user_profile row.

-- Step 1: add the new column (nullable for now; backfilled below, then locked to NOT NULL).
ALTER TABLE ticket ADD COLUMN user_id BIGINT;

-- Step 2: some user_profile rows predate real login/signup and have no
-- matching users row (e.g. typed-in usernames from before Spring Security
-- was wired up). Create a disabled placeholder account for each of these so
-- their historical tickets keep a valid owner. These accounts can never log
-- in (enabled = false), and Spring Security checks the enabled flag before
-- ever comparing passwords, so the placeholder password value is never
-- actually used to authenticate.
INSERT INTO users (username, password, full_name, role, enabled)
SELECT up.name, 'DISABLED-NO-LOGIN-PLACEHOLDER', up.name, 'USER', false
FROM user_profile up
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE u.username = up.name
);

-- Step 3: backfill ticket.user_id from the users row matching each ticket's
-- current user_profile name.
UPDATE ticket t
SET user_id = u.id
FROM user_profile up
JOIN users u ON u.username = up.name
WHERE t.user_profile_id = up.id;

-- Step 4: every ticket now has a user_id (guaranteed by steps 2-3); enforce
-- it going forward.
ALTER TABLE ticket ALTER COLUMN user_id SET NOT NULL;

-- Step 5: swap the foreign key from user_profile to users.
ALTER TABLE ticket DROP CONSTRAINT fk_ticket_user_profile;
ALTER TABLE ticket DROP COLUMN user_profile_id;

ALTER TABLE ticket
    ADD CONSTRAINT fk_ticket_user
    FOREIGN KEY (user_id)
    REFERENCES users (id);

-- Step 6: user_profile is no longer needed.
DROP TABLE user_profile;
