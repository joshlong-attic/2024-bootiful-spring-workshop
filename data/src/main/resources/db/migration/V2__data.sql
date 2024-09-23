insert into customer(name) values ('Josh');
insert into customer(name) values ('Juergen');

insert into line_item( customer, sku)
    values ( ( select id from customer where name ='Josh') ,'123' );

insert into line_item( customer, sku)
    values ( ( select id from customer where name ='Josh') ,'456' );

insert into line_item( customer, sku)
values ( ( select id from customer where name ='Juergen') ,'123' );