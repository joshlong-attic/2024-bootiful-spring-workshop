CREATE TABLE if not exists dog
(
    id          serial primary key  ,
    name        text                             NOT NULL,
    description text                             NOT NULL,
    dob         date                             NOT NULL,
    owner       text,
    gender      character(1) DEFAULT 'f'::bpchar NOT NULL,
    image       text                             NOT NULL
);


