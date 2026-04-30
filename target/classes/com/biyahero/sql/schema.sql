-- BiyaHero Tenant Schema (Generated from biyahero_backup.sql)

CREATE TABLE IF NOT EXISTS `van` (
                                     `van_id` int NOT NULL AUTO_INCREMENT,
                                     `plate_number` varchar(10) NOT NULL,
    `model` varchar(50) DEFAULT NULL,
    `capacity` int DEFAULT '15',
    `van_status` varchar(20) NOT NULL DEFAULT 'Available',
    PRIMARY KEY (`van_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `driver` (
                                        `driver_id` int NOT NULL AUTO_INCREMENT,
                                        `license_no` varchar(20) NOT NULL,
    `name` varchar(100) NOT NULL,
    `contact_number` varchar(15) DEFAULT NULL,
    `driver_status` varchar(20) NOT NULL DEFAULT 'Available',
    PRIMARY KEY (`driver_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `route` (
                                       `route_id` int NOT NULL AUTO_INCREMENT,
                                       `route_name` varchar(100) NOT NULL,
    `base_fare` decimal(10,2) DEFAULT NULL,
    PRIMARY KEY (`route_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `stop` (
                                      `stop_id` int NOT NULL AUTO_INCREMENT,
                                      `stop_name` varchar(100) NOT NULL,
    `city_province` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`stop_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `routestop` (
                                           `route_id` int NOT NULL,
                                           `stop_id` int NOT NULL,
                                           `stop_order` int DEFAULT NULL,
                                           `dist_from_prev` decimal(5,2) DEFAULT NULL,
    PRIMARY KEY (`route_id`,`stop_id`),
    CONSTRAINT `routestop_ibfk_1` FOREIGN KEY (`route_id`) REFERENCES `route` (`route_id`),
    CONSTRAINT `routestop_ibfk_2` FOREIGN KEY (`stop_id`) REFERENCES `stop` (`stop_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `trip` (
                                      `trip_id` int NOT NULL AUTO_INCREMENT,
                                      `van_id` int DEFAULT NULL,
                                      `driver_id` int DEFAULT NULL,
                                      `route_id` int DEFAULT NULL,
                                      `departure_dt` datetime DEFAULT NULL,
                                      `trip_status` varchar(20) DEFAULT 'Scheduled',
    `arrival_dt` datetime DEFAULT NULL,
    `current_stop_id` int DEFAULT NULL,
    PRIMARY KEY (`trip_id`),
    CONSTRAINT `fk_current_stop` FOREIGN KEY (`current_stop_id`) REFERENCES `stop` (`stop_id`),
    CONSTRAINT `trip_ibfk_1` FOREIGN KEY (`van_id`) REFERENCES `van` (`van_id`),
    CONSTRAINT `trip_ibfk_2` FOREIGN KEY (`driver_id`) REFERENCES `driver` (`driver_id`),
    CONSTRAINT `trip_ibfk_3` FOREIGN KEY (`route_id`) REFERENCES `route` (`route_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `passenger` (
                                           `passenger_id` int NOT NULL AUTO_INCREMENT,
                                           `name` varchar(100) NOT NULL,
    `contact_number` varchar(15) DEFAULT NULL,
    `address` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`passenger_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `booking` (
                                         `booking_id` int NOT NULL AUTO_INCREMENT,
                                         `trip_id` int DEFAULT NULL,
                                         `passenger_id` int DEFAULT NULL,
                                         `seat_number` int DEFAULT NULL,
                                         `pickup_stop` int DEFAULT NULL,
                                         `dropoff_stop` int DEFAULT NULL,
                                         `fare_paid` decimal(10,2) DEFAULT NULL,
    `booking_status` varchar(20) DEFAULT 'Reserved',
    PRIMARY KEY (`booking_id`),
    CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`),
    CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`passenger_id`) REFERENCES `passenger` (`passenger_id`),
    CONSTRAINT `booking_ibfk_3` FOREIGN KEY (`pickup_stop`) REFERENCES `stop` (`stop_id`),
    CONSTRAINT `booking_ibfk_4` FOREIGN KEY (`dropoff_stop`) REFERENCES `stop` (`stop_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;