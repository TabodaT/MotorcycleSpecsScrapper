USE `motorcycles`;

select * from moto_models where start_year >2024 order by model asc

select id, make, model, engine, capacity, power, url from moto_models where capacity = 0 and engine != 'NO_FIELDS' and engine != 'TABLE_24_MISSING'
select id, make, model, engine, capacity, power, url from moto_models where capacity = 0
select * from moto_models where make = 'BMW' and model = 'K 1600GT'
select * from moto_models order by id desc

-- insert duplicate
Insert into moto_models (id,make,model,start_year,end_year,engine,capacity,power,clutch,torque,abs,transmission,final_drive,seat_height,dry_weight,wet_weight,fuel_capacity,reserve,consumption,cooling_system,top_speed,url,image)
select 13737, make,model,start_year,end_year,engine,capacity,power,clutch,torque,abs,transmission,final_drive,seat_height,dry_weight,wet_weight,fuel_capacity,reserve,consumption,cooling_system,top_speed,url,image
from moto_models where id = 1279

select count(*) from moto_models where capacity = 0

delete from moto_models where capacity = 0

update moto_models set capacity = '600' where id = 12409

select count(*) from moto_models where make = 'Blast'
select * from moto_models where make = 'Zero'
select distinct make from moto_models

select * from moto_models where url = 'https://www.motorcyclespecs.co.za/model/beneli/benelli_v4_1000.htm'
select * from moto_models where id = 11215
delete from moto_models where url = 'https://www.motorcyclespecs.co.za/model/AJP/AJP_PR7.htm'

insert into 
moto_models(make,year,end_year,model,capacity,power) 
values ('BMW',2010,2012,'R 1200gs','1200',81);
insert into moto_models(make,model,start_year,end_year,engine,capacity,power,clutch,torque,abs,transmission,final_drive,seat_height,dry_weight,wet_weight,fuel_capacity,reserve,consumption,cooling_system,top_speed,url,image) values('test','test',1,1,'test',1,1,'test',1,true,1,'test',1,1,1,1.0,1,1.0,'test',1,'test','test')

delete from moto_models where id in (10510)
SET SQL_SAFE_UPDATES=0
delete from moto_models where capacity = 0 and engine != 'NO_FIELDS'

select count(*) from moto_models where url = 'test';
where year <= 2012 and end_year >= 2012



delete from moto_models where year >= 0; 
where id = 1;

alter table moto_models modify cooling_system varchar(500)

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