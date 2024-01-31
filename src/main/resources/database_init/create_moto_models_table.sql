USE `motorcycles`;

CREATE TABLE `moto_models` (
  `id` int NOT NULL AUTO_INCREMENT,
  `make` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  `start_year` int NOT NULL,
  `end_year` int NOT NULL,
  `engine` varchar(500),
  `capacity` double NOT NULL,
  `power` double NOT NULL,
  `clutch` varchar(100),
  `torque` double,
  `abs` BOOLEAN,
  `transmission` int,
  `final_drive` varchar(100),
  `seat_height` double,
  `dry_weight` double,
  `wet_weight` double,
  `fuel_capacity` double,
  `reserve` double,
  `consumption` double,
  `cooling_system` varchar(100),
  `top_speed` double,
  `url` varchar(1000),
  `image` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;