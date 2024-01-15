USE `motorcycles`;

CREATE TABLE `moto_models` (
  `id` int NOT NULL AUTO_INCREMENT,
  `make` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  `year` int NOT NULL,
  `end_year` int NOT NULL,
  `engine` varchar(500),
  `capacity` int NOT NULL,
  `power` int NOT NULL,
  `clutch` varchar(100),
  `torque` int,
  `abs` BOOLEAN,
  `transmission` varchar(100),
  `final_drive` varchar(100),
  `seat_height` int,
  `dry_weight` int,
  `wet_weight` int,
  `fuel_capacity` double,
  `reserve` varchar(100),
  `consumption` double,
  `cooling_system` varchar(100),
  `top_speed` int,
  `url` varchar(1000),
  `image` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;