-- Create tables for Plant Tracker (PostgreSQL)

-- NOTE: Do NOT run CREATE DATABASE here on Render-managed DB.
-- Use the database name Render supplies, or create the DB separately if you manage your own PG server.

CREATE TABLE IF NOT EXISTS Plant (
  Plant_ID      SERIAL PRIMARY KEY,
  Name          VARCHAR(100) NOT NULL,
  Type          VARCHAR(100) NOT NULL,
  Height        DECIMAL(6,2),
  DateAcquired  DATE,
  location_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS Care (
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

CREATE TABLE IF NOT EXISTS Information (
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

CREATE TABLE IF NOT EXISTS Location (
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