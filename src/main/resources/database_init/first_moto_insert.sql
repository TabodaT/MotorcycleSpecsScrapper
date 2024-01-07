USE `motorcycles`;

select * from moto_models

insert into 
moto_models(id,make,model,capacity,power) 
values (1,'BMW','R 1200gs','1200','81kw');

update moto_models
set capacity = '1170'
where id = 1 

delete from moto_models where id = 1;


------------------
SELECT * from user
insert into user(id,fullname,email,password) values(4,'name4','email4','password4');
-- SELECT fullname from user;

select * from user ORDER BY id DESC LIMIT 1