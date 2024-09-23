create table if not exists customer
(
    id   serial primary key not null ,
    name text not null
);

create table if not exists line_item
(
    customer bigint references customer (id),
    sku text not null  
);