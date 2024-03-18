insert into users (email, is_enabled, name, password, bio, view_count, is_deleted) values ( 'existing@example.com', true, 'Existing User', '$2a$10$/GLlHCWOjT9ToaBnBrqJiO7AvI0sUC.fpAIgaWkWSqz0zAMnn5Lb.', 'I am test user', 0, false);
insert into users (email, is_enabled, name, password, bio, view_count, is_deleted) values ( 'second@example.com', true, 'Second User', '$2a$10$/GLlHCWOjT9ToaBnBrqJiO7AvI0sUC.fpAIgaWkWSqz0zAMnn5Lb.', 'I am a second test user', 0, false);
insert into users (email, is_enabled, name, password, bio, view_count, is_deleted) values ( 'unconfirmed@example.com', false, 'Unconfirmed User', '$2a$10$/GLlHCWOjT9ToaBnBrqJiO7AvI0sUC.fpAIgaWkWSqz0zAMnn5Lb.', 'I am an unconfirmed user', 0, false);

insert into image(image_id, hash, image_name, image_url) VALUES (99, null, 'test_image', 'test_url');
insert into recipe(cooking_time_minutes, description, difficulty, title, category_id, image_id, user_id, view_count) VALUES (10, 'It is a test dish', 'EASY', 'Test dish', 1, 99, 2, 0);

insert into comment(comment_id, text, user_id, parent_comment_id, recipe_id) VALUES (10, 'Some comment for tests', 1, null, 1 );

