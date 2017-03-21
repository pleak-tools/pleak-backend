USE pleak;

# email				| password
# ------------------|---------
# user1@example.com | user1
# user2@example.com | user2
# user3@example.com | user3
# test1@example.com | test1
# test2@example.com | test2
# test3@example.com | test3
# blocked@example.com | blocked

INSERT IGNORE INTO users (email, password) VALUES ('user1@example.com', '$2a$12$I0HXmm4nKy7VL0VmKzHG6u/nFwUl8U0tAYji/hl41fvc8XJAVm5LS');
INSERT IGNORE INTO users (email, password) VALUES ('user2@example.com', '$2a$12$fObC5ctlHISJyk2.A8G3yusBffnqS4cOx2s3hRaHIoHrHHQnni5qi');
INSERT IGNORE INTO users (email, password) VALUES ('user3@example.com', '$2a$12$jH/QaHlebWNomVv9w7qeLu1eEcg1kgjiCJXa.spKQEAVDKHyv74QG');

INSERT IGNORE INTO users (email, password) VALUES ('test1@example.com', '$2a$12$vJinkCwkzykPDDYRtJIWyeWABrlXckVsBTda.IU8A1NzMfjVjIeZG');
INSERT IGNORE INTO users (email, password) VALUES ('test2@example.com', '$2a$12$WapGwCGThBY9UDDgTj.tMOJzkbfOae0wqlFv5GDxWScNC/3E5ltN.');
INSERT IGNORE INTO users (email, password) VALUES ('test3@example.com', '$2a$12$Q/CNyytuuCmX3YC.oS2d/un42zs.STRa7Zqwbm1MryleA/6lEwqBy');

INSERT IGNORE INTO users (email, password, blocked) VALUES ('blocked@example.com', '$2a$12$Up3lOd/PRSLq649QbPr75O7aK/o1m4eaZP.Jx73tjh4iBhoco2KI2', 1);
INSERT IGNORE INTO users (email, password) VALUES ('changepassword1@example.com', '$2a$12$scdd8isaDYDZ6u3pvZKUeO7h5EJ5bmgxrlFHwEEoMYDh/ln.O4cVu');



# Create root directories for all users
DROP PROCEDURE IF EXISTS create_root_directories;
delimiter $$
CREATE PROCEDURE create_root_directories()
  BEGIN
    IF NOT EXISTS (select * from pobjects AS p JOIN users AS u ON p.user_id = u.id WHERE u.email = 'user1@example.com') THEN
      INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
      WHERE email = 'user1@example.com';
    END IF;
    IF NOT EXISTS (select * from pobjects AS p JOIN users AS u ON p.user_id = u.id WHERE u.email = 'user2@example.com') THEN
      INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
      WHERE email = 'user2@example.com';
    END IF;
    IF NOT EXISTS (select * from pobjects AS p JOIN users AS u ON p.user_id = u.id WHERE u.email = 'user3@example.com') THEN
      INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
      WHERE email = 'user3@example.com';
    END IF;
    IF NOT EXISTS (select * from pobjects AS p JOIN users AS u ON p.user_id = u.id WHERE u.email = 'test1@example.com') THEN
      INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
      WHERE email = 'test1@example.com';
    END IF;
    IF NOT EXISTS (select * from pobjects AS p JOIN users AS u ON p.user_id = u.id WHERE u.email = 'test2@example.com') THEN
      INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
      WHERE email = 'test2@example.com';
    END IF;
    IF NOT EXISTS (select * from pobjects AS p JOIN users AS u ON p.user_id = u.id WHERE u.email = 'test3@example.com') THEN
      INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
      WHERE email = 'test3@example.com';
    END IF;
    IF NOT EXISTS (select * from pobjects AS p JOIN users AS u ON p.user_id = u.id WHERE u.email = 'blocked@example.com') THEN
      INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
      WHERE email = 'blocked@example.com';
    END IF;
    IF NOT EXISTS (select * from pobjects AS p JOIN users AS u ON p.user_id = u.id WHERE u.email = 'changepassword1@example.com') THEN
      INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
      WHERE email = 'changepassword1@example.com';
    END IF;
  END
$$

call create_root_directories();