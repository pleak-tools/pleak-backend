ALTER TABLE pobjects ADD modified_by INT;
ALTER TABLE pobjects
    ADD CONSTRAINT fk_pobjects_users2 FOREIGN KEY (modified_by) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;