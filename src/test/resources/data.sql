insert into users (email, is_enabled, name, password, bio, registered_at, image_id) values ( 'existing@example.com', true, 'Existing User', '$2a$10$/GLlHCWOjT9ToaBnBrqJiO7AvI0sUC.fpAIgaWkWSqz0zAMnn5Lb.', 'I am test user', null, null );
insert into users (email, is_enabled, name, password, bio, registered_at, image_id) values ( 'second@example.com', true, 'Second User', '$2a$10$/GLlHCWOjT9ToaBnBrqJiO7AvI0sUC.fpAIgaWkWSqz0zAMnn5Lb.', 'I am a second test user', null, null );
insert into users (email, is_enabled, name, password, bio, registered_at, image_id) values ( 'unconfirmed@example.com', false, 'Unconfirmed User', '$2a$10$/GLlHCWOjT9ToaBnBrqJiO7AvI0sUC.fpAIgaWkWSqz0zAMnn5Lb.', 'I am an unconfirmed user', null, null );

insert into image(image_id, hash, image_name, image_url) VALUES (99, null, 'test_image', 'test_url');
insert into recipe(cooking_time_minutes, description, difficulty, title, category_id, image_id, user_id) VALUES (10, 'It is a test dish', 'EASY', 'Test dish', 1, 99, 2);

