CREATE DATABASE PlantDB;

USE PlantDB;

CREATE TABLE Plant (
  Plant_ID      INT NOT NULL AUTO_INCREMENT,
  Name          VARCHAR(100) NOT NULL,   
  Type          VARCHAR(100) NOT NULL,   
  Height        DECIMAL(6,2),
  DateAcquired  DATE,
  location_name VARCHAR(100),
  PRIMARY KEY (Plant_ID)
);

CREATE TABLE Care (
  Plant_ID       INT NOT NULL,
  LastSoilChange DATE,
  LastWatering   DATE,
  PRIMARY KEY (Plant_ID),
  CONSTRAINT fk_care_plant
    FOREIGN KEY (Plant_ID)
    REFERENCES Plant(Plant_ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

CREATE TABLE Information (
  Plant_ID            INT NOT NULL,
  FromAnotherPlant    BOOLEAN DEFAULT FALSE,
  SoilType            VARCHAR(100),
  PotSize             VARCHAR(50),
  WaterGlobeRequired  BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (Plant_ID),
  CONSTRAINT fk_information_plant
    FOREIGN KEY (Plant_ID)
    REFERENCES Plant(Plant_ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

CREATE TABLE Location (
  Plant_ID      INT NOT NULL,
  location_name VARCHAR(100) NOT NULL,
  LightLevel    VARCHAR(50),
  PRIMARY KEY (Plant_ID, location_name),
  CONSTRAINT fk_location_plant
    FOREIGN KEY (Plant_ID)
    REFERENCES Plant(Plant_ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);
