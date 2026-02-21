package com.planttracker.app;

import com.planttracker.business.BusinessManager;
import com.planttracker.model.Care;
import com.planttracker.model.Information;
import com.planttracker.model.Location;
import com.planttracker.model.Plant;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

public class BusinessDemo {
    public static void main(String[] args) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Could not load db.properties");
            e.printStackTrace();
            return;
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.pass");

        BusinessManager bm = new BusinessManager(url, user, pass);

        try {
            System.out.println("=== CREATE PLANT + related ===");
            Plant p = new Plant();
            p.setPlantId(0); // 0 => insert
            p.setName("DemoPlant1");
            p.setType("Snake Plant");
            p.setHeight(10.0);
            p.setDateAcquired(LocalDate.now());
            p.setLocationName("Bedroom");

            // save plant (insert)
            bm.savePlant(p);
            int id = p.getPlantId();
            System.out.println("Inserted Plant with ID: " + id);

            // create related care
            Care c = new Care();
            c.setPlantId(id);
            c.setLastSoilChange(LocalDate.now().minusDays(30));
            c.setLastWatering(LocalDate.now().minusDays(3));
            bm.saveCare(c);

            // create information
            Information info = new Information();
            info.setPlantId(id);
            info.setFromAnotherPlant(true);
            info.setSoilType("succulent mix");
            info.setPotSize("medium");
            info.setWaterGlobeRequired(false);
            bm.saveInformation(info);

            // create location
            Location loc = new Location();
            loc.setPlantId(id);
            loc.setLocationName("Bedroom");
            loc.setLightLevel("Medium");
            bm.saveLocation(loc);

            // read and print
            printFull(bm, id);

            // ---------- Update ----------
            System.out.println("\n=== UPDATE Plant name and Location light ===");
            p.setName("DemoPlant1-updated");
            bm.savePlant(p); // update

            loc.setLightLevel("Bright");
            bm.saveLocation(loc); // update location

            printFull(bm, id);

            // ---------- Delete ----------
            System.out.println("\n=== DELETE Plant ===");
            int deleted = bm.deletePlant(id);
            System.out.println("Deleted plant rows: " + deleted);

            // verify deletion
            Plant after = bm.getPlantById(id);
            if (after == null)
                System.out.println("Plant " + id + " no longer exists (deleted).");
            else
                System.out.println("Plant still exists: " + after.getName());

        } catch (SQLException ex) {
            System.err.println("DB error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void printFull(BusinessManager bm, int plantId) throws SQLException {
        Plant p = bm.getPlantById(plantId);
        if (p == null) {
            System.out.println("Plant not found: " + plantId);
            return;
        }
        System.out.println("\n--- Plant ---");
        System.out.println("Plant ID : " + p.getPlantId());
        System.out.println("Name     : " + p.getName());
        System.out.println("Type     : " + p.getType());
        System.out.println("Height   : " + p.getHeight());
        System.out.println("Acquired : " + p.getDateAcquired());
        System.out.println("Plant.location_name : " + p.getLocationName());

        System.out.println("\n--- Care ---");
        Care c = bm.getCareByPlantId(plantId);
        if (c == null)
            System.out.println("No Care record.");
        else {
            System.out.println("LastSoilChange: " + c.getLastSoilChange());
            System.out.println("LastWatering  : " + c.getLastWatering());
        }

        System.out.println("\n--- Information ---");
        Information info = bm.getInformationByPlantId(plantId);
        if (info == null)
            System.out.println("No Information record.");
        else {
            System.out.println("FromAnotherPlant: " + info.isFromAnotherPlant());
            System.out.println("SoilType        : " + info.getSoilType());
            System.out.println("PotSize         : " + info.getPotSize());
            System.out.println("WaterGlobeReq   : " + info.isWaterGlobeRequired());
        }

        System.out.println("\n--- Locations ---");
        List<Location> locs = bm.getAllLocationsForPlant(plantId);
        if (locs == null || locs.isEmpty()) {
            System.out.println("No locations recorded.");
        } else {
            for (Location L : locs) {
                System.out
                        .println("LocationName    : " + (L.getLocationName() == null ? "(none)" : L.getLocationName()));
                System.out.println("LightLevel      : " + (L.getLightLevel() == null ? "N/A" : L.getLightLevel()));
            }
        }
    }
}