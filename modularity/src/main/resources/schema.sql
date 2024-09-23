create table if not exists dog
(
    id    serial primary key,
    name  text not null,
    owner text null
);