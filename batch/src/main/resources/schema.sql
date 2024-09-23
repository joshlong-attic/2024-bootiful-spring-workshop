create table if not exists customer
(
    id   serial primary key,
    name text not null
) ;
delete from customer;