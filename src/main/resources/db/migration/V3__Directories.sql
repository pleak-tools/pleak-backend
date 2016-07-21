ALTER TABLE file_permissions
    RENAME permissions;

# Make files table into pobjects table (pleak objects) so we can store files and directories in the same table
ALTER TABLE files
    RENAME pobjects;
ALTER TABLE permissions
    DROP FOREIGN KEY permissions_ibfk_1;
ALTER TABLE permissions
    CHANGE COLUMN `file_id` `pobject_id` INT NOT NULL;
ALTER TABLE permissions
    ADD CONSTRAINT fk_permissions_pobjects FOREIGN KEY (pobject_id) REFERENCES pobjects(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;

CREATE TABLE pobject_types (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    UNIQUE (title)
);

INSERT INTO pobject_types VALUES(1, 'file');
INSERT INTO pobject_types VALUES(2, 'directory');

# Set all current files to type=1 (file)
ALTER TABLE pobjects
    ADD type_id INT NOT NULL;
UPDATE pobjects SET type_id = 1;
ALTER TABLE pobjects
    ADD CONSTRAINT fk_pobjects_pobject_types FOREIGN KEY (type_id) REFERENCES pobject_types(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;

# Create root directories for all users
ALTER TABLE pobjects ADD parent_id INT;
INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users;
ALTER TABLE pobjects
    ADD CONSTRAINT fk_pobjects_pobjects FOREIGN KEY (parent_id) REFERENCES pobjects(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE;

# Set existing files to root directories
UPDATE pobjects t
    JOIN pobjects s
    ON (t.user_id = s.user_id and t.title <> 'root' and s.title = 'root')
    SET t.parent_id = s.id;

# Rename and add on delete/update cascade to other foreign keys
ALTER TABLE pobjects DROP FOREIGN KEY pobjects_ibfk_1;
ALTER TABLE pobjects
    ADD CONSTRAINT fk_pobjects_users FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;

ALTER TABLE permissions DROP FOREIGN KEY permissions_ibfk_2;
ALTER TABLE permissions
    ADD CONSTRAINT fk_permissions_users FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;

ALTER TABLE permissions DROP FOREIGN KEY permissions_ibfk_3;
ALTER TABLE permissions
    ADD CONSTRAINT fk_permissions_actions FOREIGN KEY (action_id) REFERENCES actions(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE;

# Add unique constraint to actions so we can safely query by title
ALTER TABLE actions ADD UNIQUE (title);
