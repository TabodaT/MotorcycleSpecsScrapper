USE `motorcycles`;

select * from moto_models
-- where model = 'F 750GS'
where make = 'BMW' and start_year = 2020
order by model asc
select count(*) from moto_models 
where make = 'Bajaj'

insert into 
moto_models(make,year,end_year,model,capacity,power) 
values ('BMW',2010,2012,'R 1200gs','1200',81);
insert into moto_models(make,model,start_year,end_year,engine,capacity,power,clutch,torque,abs,transmission,final_drive,seat_height,dry_weight,wet_weight,fuel_capacity,reserve,consumption,cooling_system,top_speed,url,image) values('test','test',1,1,'test',1,1,'test',1,true,1,'test',1,1,1,1.0,1,1.0,'test',1,'test','test')

delete from moto_models where id in (212)
SET SQL_SAFE_UPDATES=0
delete from moto_models where cooling_system = '0'

select id from moto_models where cooling = '0'


select count(*) from moto_models where url = 'test';
where year <= 2012 and end_year >= 2012

update moto_models
set capacity = '1170'
where id = 1 

delete from moto_models where year >= 0; 
where id = 1;


------------------
SELECT * from user
insert into user(fullname,email,password) values('name1','email1','password1');
-- SELECT fullname from user;

select * from user where id in (1,2)

delete from user where id not in (1)
delete from user where id in (2)

select * from user ORDER BY id DESC LIMIT 1

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fullname` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;