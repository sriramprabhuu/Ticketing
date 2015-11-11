CREATE TABLE IF NOT EXISTS `ticketing`.`level_master` (
  `levelId` INT(11) NOT NULL AUTO_INCREMENT,
  `levelName` VARCHAR(20) NOT NULL,
  `price` INT(11) NOT NULL,
  `noOfRows` INT(11) NOT NULL,
  `noOfseats` INT(11) NOT NULL,
  PRIMARY KEY (`levelId`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


CREATE TABLE IF NOT EXISTS `ticketing`.`status` (
  `statusId` INT(11) NOT NULL,
  `statusName` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`statusId`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `ticketing`.`user` (
  `user_Id` INT(11) NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(50) NOT NULL,
  `mobile` VARCHAR(12) NULL DEFAULT NULL,
  PRIMARY KEY (`user_Id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `ticketing`.`hold` (
  `holdId` INT(11) NOT NULL AUTO_INCREMENT,
  `userId` INT(11) NOT NULL,
  `noOfseats` INT(11) NOT NULL,
  `createdDate` DATE NOT NULL,
  PRIMARY KEY (`holdId`),
  INDEX `userId` (`userId` ASC),
  CONSTRAINT `hold_ibfk_1`
    FOREIGN KEY (`userId`)
    REFERENCES `ticketing`.`user` (`user_Id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `ticketing`.`seatmap` (
  `holdId` INT(11) NOT NULL DEFAULT '1',
  `level` INT(11) NOT NULL,
  `showid` INT(11) NOT NULL,
  `rowid` INT(11) NOT NULL,
  `seatid` INT(11) NOT NULL,
  `createdDate` DATETIME NOT NULL,
  `status` INT(11) NOT NULL,
  PRIMARY KEY (`level`, `showid`, `rowid`, `seatid`),
  INDEX `status` (`status` ASC),
  INDEX `seatmap_ibfk_3_idx` (`level` ASC),
  INDEX `seatmap_ibfk_2_idx` (`holdId` ASC),
  CONSTRAINT `seatmap_ibfk_2`
    FOREIGN KEY (`holdId`)
    REFERENCES `ticketing`.`hold` (`holdId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `seatmap_ibfk_1`
    FOREIGN KEY (`status`)
    REFERENCES `ticketing`.`status` (`statusId`),
  CONSTRAINT `seatmap_ibfk_3`
    FOREIGN KEY (`level`)
    REFERENCES `ticketing`.`level_master` (`levelId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `ticketing`.`reservation` (
  `reservationId` INT(11) NOT NULL AUTO_INCREMENT,
  `holdId` INT(11) NOT NULL,
  `userId` INT(11) NOT NULL,
  `createdDate` DATE NOT NULL,
  PRIMARY KEY (`reservationId`),
  INDEX `holdId` (`holdId` ASC),
  INDEX `userId` (`userId` ASC),
  CONSTRAINT `reservation_ibfk_1`
    FOREIGN KEY (`holdId`)
    REFERENCES `ticketing`.`hold` (`holdId`),
  CONSTRAINT `reservation_ibfk_2`
    FOREIGN KEY (`userId`)
    REFERENCES `ticketing`.`user` (`user_Id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

INSERT INTO `ticketing`.`user` (`user_Id`, `email`, `mobile`) VALUES (1, 'rsri@outlook.com', '2018908888');
commit;

INSERT INTO `ticketing`.`level_master` (`levelId`, `levelName`, `price`, `noOfRows`, `noOfseats`) VALUES (1, 'Orchestra', 100, 25, 50);
INSERT INTO `ticketing`.`level_master` (`levelId`, `levelName`, `price`, `noOfRows`, `noOfseats`) VALUES (2, 'Main', 75, 20, 100);
INSERT INTO `ticketing`.`level_master` (`levelId`, `levelName`, `price`, `noOfRows`, `noOfseats`) VALUES (3, 'Balcony1', 50, 15, 100);
INSERT INTO `ticketing`.`level_master` (`levelId`, `levelName`, `price`, `noOfRows`, `noOfseats`) VALUES (4, 'Balcony2', 40, 15, 100);
commit;

INSERT INTO `ticketing`.`status` (`statusId`, `statusName`) VALUES (0, 'Held');
INSERT INTO `ticketing`.`status` (`statusId`, `statusName`) VALUES (1, 'Confirmed');
commit;
