CREATE TABLE actions (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE file_permissions (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    file_id INT NOT NULL,
    user_id INT NOT NULL,
    action_id INT NOT NULL,
    FOREIGN KEY (file_id) REFERENCES files(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (action_id) REFERENCES actions(id),
    UNIQUE(file_id, user_id)
);

INSERT INTO actions (title) VALUES ('view');
INSERT INTO actions (title) VALUES ('edit');
