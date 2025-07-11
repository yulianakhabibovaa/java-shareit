INSERT INTO users (name, email) SELECT 'Первый', 'user1@mail.ru'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user1@mail.ru');
INSERT INTO users (name, email) SELECT 'Второй', 'user2@mail.ru'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user2@mail.ru');
INSERT INTO users (name, email) SELECT 'Третий', 'user3@mail.ru'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user3@mail.ru');
INSERT INTO users (name, email) SELECT 'Четвертый', 'user4@mail.ru'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user4@mail.ru');

INSERT INTO items (name,description,is_available,owner_id) VALUES ('Ручка','Ручка шариковая',false,1);
INSERT INTO items (name,description,is_available,owner_id) VALUES ('Карандаш','Карандаш грифельный',true,2);
INSERT INTO items (name,description,is_available,owner_id) VALUES ('Тетрадь','Тетрадь в клеточку',true,2);
INSERT INTO items (name,description,is_available,owner_id) VALUES ('Нож','Нож канцелярский',false,4);

INSERT INTO bookings (booker_id,item_id,status,start_date,end_date) VALUES (1,2,'WAITING', '2025-06-04 20:09:19.000','2025-06-05 20:09:19.000');
INSERT INTO bookings (booker_id,item_id,status,start_date,end_date) VALUES (2,1,'APPROVED','2025-06-04 20:09:19.000','2025-06-04 20:19:19.000');
INSERT INTO bookings (booker_id,item_id,status,start_date,end_date) VALUES (3,4,'APPROVED','2025-06-04 20:09:19.000','2026-07-05 20:09:19.000');
INSERT INTO bookings (booker_id,item_id,status,start_date,end_date) VALUES (4,1,'APPROVED','2026-06-04 20:09:19.000','2026-07-05 20:09:19.000');