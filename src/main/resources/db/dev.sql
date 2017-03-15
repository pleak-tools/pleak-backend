USE pleak;

# email				| password
# ------------------|---------
# user1@example.com | user1
# user2@example.com | user2
# user3@example.com | user3
# test1@example.com | test1
# test2@example.com | test2
# test3@example.com | test3
# blocked@example.com | test3

INSERT IGNORE INTO users (email, password) VALUES ('user1@example.com', '$2a$12$I0HXmm4nKy7VL0VmKzHG6u/nFwUl8U0tAYji/hl41fvc8XJAVm5LS');
INSERT IGNORE INTO users (email, password) VALUES ('user2@example.com', '$2a$12$fObC5ctlHISJyk2.A8G3yusBffnqS4cOx2s3hRaHIoHrHHQnni5qi');
INSERT IGNORE INTO users (email, password) VALUES ('user3@example.com', '$2a$12$jH/QaHlebWNomVv9w7qeLu1eEcg1kgjiCJXa.spKQEAVDKHyv74QG');

INSERT IGNORE INTO users (email, password) VALUES ('test1@example.com', '$2a$12$vJinkCwkzykPDDYRtJIWyeWABrlXckVsBTda.IU8A1NzMfjVjIeZG');
INSERT IGNORE INTO users (email, password) VALUES ('test2@example.com', '$2a$12$WapGwCGThBY9UDDgTj.tMOJzkbfOae0wqlFv5GDxWScNC/3E5ltN.');
INSERT IGNORE INTO users (email, password) VALUES ('test3@example.com', '$2a$12$Q/CNyytuuCmX3YC.oS2d/un42zs.STRa7Zqwbm1MryleA/6lEwqBy');

INSERT IGNORE INTO users (email, password, blocked) VALUES ('blocked@example.com', '$2a$12$Q/CNyytuuCmX3YC.oS2d/un42zs.STRa7Zqwbm1MryleA/6lEwqBy', 1);

# Create root directories for all users
INSERT INTO pobjects (title, user_id, type_id) SELECT DISTINCT 'root', id, 2 FROM users
	WHERE email = 'user1@example.com' OR
		  email = 'user2@example.com' OR
		  email = 'user3@example.com' OR
		  email = 'test1@example.com' OR
		  email = 'test2@example.com' OR
		  email = 'test3@example.com' OR
		  email = 'blocked@example.com';