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
insert into user(fullname,email,password) values('name1','email1','password1');
-- SELECT fullname from user;

select * from user where id in (1,2)

delete from user where id not in (1)
delete from user where id in (1)

select * from user ORDER BY id DESC LIMIT 1

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fullname` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;