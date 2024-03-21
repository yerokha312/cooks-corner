create table role
(
    role_id   identity,
    authority varchar(255),
    primary key (role_id)
);

INSERT INTO role (role_id, authority)
values (1, 'USER'),
       (2, 'ADMIN');

create table category
(
    category_id   identity,
    category_name varchar(255),
    primary key (category_id)
);

INSERT INTO category (category_id, category_name)
VALUES (1, 'breakfasts'),
       (2, 'soups'),
       (3, 'salads'),
       (4, 'main dishes'),
       (5, 'desserts'),
       (6, 'seafoods'),
       (7, 'beverages');

create table image
(
    image_id   identity,
    hash       varchar(255),
    image_name varchar(255),
    image_url  varchar(255),
    primary key (image_id)
);


create table users
(
    user_id       identity,
    email         varchar(255),
    is_enabled    boolean,
    name          varchar(255),
    password      varchar(255),
    bio           varchar(500),
    registered_at timestamp(6),
    view_count    bigint,
    image_id      bigint,
    is_deleted    boolean,
    primary key (user_id),
    constraint uk_6dotkott2kjsp8vw4d0m25fb7
        unique (email),
    constraint fklqj25c28swu46s4jbudd7hore
        foreign key (image_id) references image
);


create table refresh_token
(
    token_id   identity,
    expires_at timestamp(6) with time zone,
    is_revoked boolean,
    issued_at  timestamp(6) with time zone,
    token      varchar(1000),
    user_id    bigint,
    primary key (token_id),
    constraint uk_r4k4edos30bx9neoq81mdvwph
        unique (token),
    constraint fkjtx87i0jvq2svedphegvdwcuy
        foreign key (user_id) references users
);

create table user_role_junction
(
    user_id bigint  not null,
    role_id integer not null,
    primary key (user_id, role_id),
    constraint fkavwwt8r8t6u63w8wevavwnfwt
        foreign key (role_id) references role,
    constraint fk5aqfsa7i8mxrr51gtbpcvp0v1
        foreign key (user_id) references users
);

create table followers
(
    user_id     bigint not null,
    follower_id bigint not null,
    primary key (user_id, follower_id),
    constraint fk9w6mv39vle9f9yacvvkfieai7
        foreign key (follower_id) references users,
    constraint fkndvqwh40g1qt4xirl6vp2d6m6
        foreign key (user_id) references users
);

create table following
(
    user_id      bigint not null,
    following_id bigint not null,
    primary key (user_id, following_id),
    constraint fks2ok47rt2ebbsdp6ginkffrci
        foreign key (following_id) references users,
    constraint fks8v9phajoli0ka1vbrj7ypkpd
        foreign key (user_id) references users
);

create index image_hash_idx
    on image (hash);

create table ingredient
(
    ingredient_id identity,
    name          varchar(255),
    primary key (ingredient_id)
);

create table recipe
(
    recipe_id            identity,
    cooking_time_minutes integer,
    created_at           timestamp(6),
    updated_at           timestamp(6),
    description          varchar(1000),
    difficulty           varchar(255) not null,
    title                varchar(255) not null,
    view_count           bigint,
    category_id          bigint,
    image_id             bigint,
    user_id              bigint,
    primary key (recipe_id),
    constraint fkrufhnv33hpfxstx9x108553kj
        foreign key (category_id) references category,
    constraint fkky5g165dha9n8jdyx597kp9wa
        foreign key (image_id) references image,
    constraint fk5mx01yw4j003wisa2aqmwir6l
        foreign key (user_id) references users,
    constraint recipe_difficulty_check
        check (recipe.difficulty in ('EASY', 'MEDIUM', 'HARD'))
);
create table user_recipe_bookmarks
(
    recipe_id bigint not null,
    user_id   bigint not null,
    primary key (recipe_id, user_id),
    constraint fkou1j14ur9cx2sj6eeo9v04x96
        foreign key (user_id) references users,
    constraint fk61rnc57d5q3djcudstfrl8via
        foreign key (recipe_id) references recipe
);

create table recipe_ingredient
(
    recipe_ingredient_id identity,
    amount               double precision,
    measure_unit         varchar(255),
    ingredient_id        bigint,
    recipe_id            bigint,
    primary key (recipe_ingredient_id),
    constraint fk9b3oxoskt0chwqxge0cnlkc29
        foreign key (ingredient_id) references ingredient,
    constraint fkgu1oxq7mbcgkx5dah6o8geirh
        foreign key (recipe_id) references recipe
);

create table user_recipe_likes
(
    recipe_id bigint not null,
    user_id   bigint not null,
    primary key (recipe_id, user_id),
    constraint fkcd0pgt91shj2gglw710bl7bnw
        foreign key (user_id) references users,
    constraint fkae85gcb6owcle869isvgjnwth
        foreign key (recipe_id) references recipe
);

create table comment
(
    comment_id        identity
        primary key,
    created_at        timestamp(6),
    updated_at       timestamp(6),
    text              varchar(255) not null,
    user_id           bigint
        constraint fkqm52p1v3o13hy268he0wcngr5
            references users,
    parent_comment_id bigint
        constraint fkhvh0e2ybgg16bpu229a5teje7
            references comment,
    recipe_id         bigint
        constraint fke5i1rxybcm40jcn98fj1jmvit
            references recipe,
    is_deleted boolean
);

create table user_comment_likes
(
    user_id    bigint not null
        constraint fkbcucvykkopqscgu4liy54u5pd
            references comment,
    comment_id bigint not null
        constraint fkggdk3uogfkd2yartj4fyyol57
            references users,
    primary key (user_id, comment_id)
);





