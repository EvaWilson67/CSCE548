package com.planttracker.app;

import com.planttracker.dao.CareDao;
import com.planttracker.dao.InformationDao;
import com.planttracker.dao.LocationDao;
import com.planttracker.dao.PlantDao;
import com.planttracker.model.Care;
import com.planttracker.model.Information;
import com.planttracker.model.Location;
import com.planttracker.model.Plant;

import java.sql.SQLException;       
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Console CRUD app for Plant + Care + Information + Location.
 * Uses the separate DAO classes. Reads DB credentials from environment.
 */
public class ConsoleApp {
    private final PlantDao plantDao;
    private final CareDao careDao;
    private final InformationDao informationDao;
    private final LocationDao locationDao;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleApp(String url, String user, String pass) {
        this.plantDao = new PlantDao(url, user, pass);
        this.careDao = new CareDao(url, user, pass);
        this.informationDao = new InformationDao(url, user, pass);
        this.locationDao = new LocationDao(url, user, pass);
    }

    public static void main(String[] args) {
        String url  = System.getenv().getOrDefault("PLANTDB_URL", "jdbc:mysql://localhost:3306/PlantDB?serverTimezone=UTC");
        String user = System.getenv().getOrDefault("PLANTDB_USER", "root");
        String pass = System.getenv().getOrDefault("PLANTDB_PASS", "Kq37wCXH");

        ConsoleApp app = new ConsoleApp(url, user, pass);
        app.run();
    }

    private void run() {
        printHeader();
        boolean quit = false;
        while (!quit) {
            printMenu();
            String choice = prompt("Choose an option");
            try {
                switch (choice) {
                    case "1": listPlants(); break;
                    case "2": viewPlantById(); break;
                    case "3": createPlantWithDetails(); break;
                    case "4": updatePlant(); break;
                    case "5": deletePlant(); break;

                    case "10": createCare(); break;
                    case "11": updateCare(); break;
                    case "12": deleteCare(); break;
                    case "13": viewCareByPlant(); break;

                    case "20": createInformation(); break;
                    case "21": updateInformation(); break;
                    case "22": deleteInformation(); break;
                    case "23": viewInformationByPlant(); break;

                    case "30": createLocation(); break;
                    case "31": updateLocation(); break;
                    case "32": deleteLocation(); break;
                    case "33": listLocationsForPlant(); break;

                    case "x":
                    case "X": quit = true; break;
                    default: System.out.println("Unknown choice. Try again."); break;
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye 👋");
    }

    private void printHeader() {
        System.out.println("======================================");
        System.out.println("       PlantTracker Console CRUD      ");
        System.out.println("======================================");
    }

    private void printMenu() {
        System.out.println("\nMenu (Plants)");
        System.out.println("  1) List all plants");
        System.out.println("  2) View plant by ID (with related records)");
        System.out.println("  3) Create plant (and optionally Care/Info/Location)");
        System.out.println("  4) Update plant");
        System.out.println("  5) Delete plant");

        System.out.println("\nMenu (Care)");
        System.out.println(" 10) Create Care for a Plant");
        System.out.println(" 11) Update Care for a Plant");
        System.out.println(" 12) Delete Care for a Plant");
        System.out.println(" 13) View Care by Plant ID");

        System.out.println("\nMenu (Information)");
        System.out.println(" 20) Create Information for a Plant");
        System.out.println(" 21) Update Information for a Plant");
        System.out.println(" 22) Delete Information for a Plant");
        System.out.println(" 23) View Information by Plant ID");

        System.out.println("\nMenu (Location)");
        System.out.println(" 30) Create Location for a Plant");
        System.out.println(" 31) Update Location (LightLevel)");
        System.out.println(" 32) Delete Location for a Plant");
        System.out.println(" 33) List Locations for a Plant");

        System.out.println("\n  X) Exit");
    }

    // ---------- Helpers ----------
    private String prompt(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private Integer promptInt(String label) {
        String s = prompt(label);
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { System.out.println("Invalid number."); return null; }
    }

    private LocalDate promptDate(String label) {
        String s = prompt(label + " (YYYY-MM-DD or blank)");
        if (s.isEmpty()) return null;
        try { return LocalDate.parse(s); }
        catch (DateTimeParseException e) { System.out.println("Invalid date format."); return null; }
    }

    private Boolean promptBool(String label) {
        String s = prompt(label + " (y/n)");
        if (s.isEmpty()) return null;
        return s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes") || s.equals("1");
    }

    // ---------- Plant CRUD ----------
    private void listPlants() throws SQLException {
        List<Plant> all = plantDao.findAll();
        if (all.isEmpty()) {
            System.out.println("No plants found.");
            return;
        }
        System.out.printf("%-6s %-20s %-20s %-8s %-12s %-15s\n", "ID", "Name", "Type", "Height", "Acquired", "Location");
        for (Plant p : all) {
            System.out.printf("%-6d %-20s %-20s %-8s %-12s %-15s\n",
                    p.getPlantId(),
                    safe(p.getName(), 20),
                    safe(p.getType(), 20),
                    p.getHeight() == null ? "N/A" : p.getHeight().toString(),
                    p.getDateAcquired() == null ? "N/A" : p.getDateAcquired().toString(),
                    safe(p.getLocationName(), 15));
        }
        System.out.println("Total: " + all.size());
    }

    private void viewPlantById() throws SQLException {
        Integer id = promptInt("Enter Plant ID");
        if (id == null) return;
        Plant p = plantDao.findById(id);
        if (p == null) { System.out.println("Plant not found."); return; }
        printPlantFull(p);
    }

    private void createPlantWithDetails() throws SQLException {
        System.out.println("Create new Plant (leave Plant_ID blank to set manually if desired)");
        Integer id = promptInt("Plant_ID (optional)");
        String name = prompt("Name");
        String type = prompt("Type");
        Double height = null;
        String h = prompt("Height (number, optional)");
        if (!h.isEmpty()) {
            try { height = Double.parseDouble(h); } catch (NumberFormatException e) { System.out.println("Invalid height ignored."); }
        }
        LocalDate acquired = promptDate("DateAcquired");
        String locName = prompt("location_name");

        Plant p = new Plant();
        if (id != null) p.setPlantId(id);
        p.setName(name);
        p.setType(type);
        p.setHeight(height);
        p.setDateAcquired(acquired);
        p.setLocationName(locName);

        int inserted = plantDao.insert(p);
        System.out.println("Inserted Plant rows: " + inserted);

        // optional related records
        if (promptBool("Add Care record now? (y/n)") == Boolean.TRUE) {
            createCareForPlant(p.getPlantId());
        }
        if (promptBool("Add Information now? (y/n)") == Boolean.TRUE) {
            createInformationForPlant(p.getPlantId());
        }
        if (promptBool("Add Location now? (y/n)") == Boolean.TRUE) {
            createLocationForPlant(p.getPlantId());
        }
    }

    private void updatePlant() throws SQLException {
        Integer id = promptInt("Enter Plant ID to update");
        if (id == null) return;
        Plant p = plantDao.findById(id);
        if (p == null) { System.out.println("Plant not found."); return; }

        String name = prompt("Name [" + safe(p.getName(),30) + "]");
        if (!name.isEmpty()) p.setName(name);

        String type = prompt("Type [" + safe(p.getType(),30) + "]");
        if (!type.isEmpty()) p.setType(type);

        String h = prompt("Height [" + (p.getHeight() == null ? "N/A" : p.getHeight().toString()) + "]");
        if (!h.isEmpty()) {
            try { p.setHeight(Double.parseDouble(h)); } catch (NumberFormatException e) { System.out.println("Invalid height ignored."); }
        }

        LocalDate d = promptDate("DateAcquired [" + (p.getDateAcquired() == null ? "N/A" : p.getDateAcquired().toString()) + "]");
        if (d != null) p.setDateAcquired(d);

        String loc = prompt("location_name [" + safe(p.getLocationName(),20) + "]");
        if (!loc.isEmpty()) p.setLocationName(loc);

        int updated = plantDao.update(p);
        System.out.println("Updated rows: " + updated);
    }

    private void deletePlant() throws SQLException {
        Integer id = promptInt("Enter Plant ID to delete");
        if (id == null) return;
        String confirm = prompt("Type DELETE to confirm deletion of plant " + id);
        if (!"DELETE".equals(confirm)) { System.out.println("Aborted."); return; }
        int del = plantDao.delete(id);
        System.out.println("Deleted rows: " + del + " (children cascaded if FK set)");
    }

    // ---------- Care CRUD ----------
    private void createCare() throws SQLException {
        Integer pid = promptInt("Plant ID for Care");
        if (pid == null) return;
        createCareForPlant(pid);
    }

    private void createCareForPlant(int plantId) throws SQLException {
        LocalDate lastSoil = promptDate("LastSoilChange");
        LocalDate lastWater = promptDate("LastWatering");
        Care c = new Care(plantId, lastSoil, lastWater);
        int r = careDao.insert(c);
        System.out.println("Inserted Care rows: " + r);
    }

    private void updateCare() throws SQLException {
        Integer pid = promptInt("Plant ID for Care update");
        if (pid == null) return;
        Care c = careDao.findByPlantId(pid);
        if (c == null) { System.out.println("No Care record found for that plant."); return; }

        LocalDate d1 = promptDate("LastSoilChange [" + (c.getLastSoilChange()==null?"N/A":c.getLastSoilChange()) + "]");
        if (d1 != null) c.setLastSoilChange(d1);
        LocalDate d2 = promptDate("LastWatering [" + (c.getLastWatering()==null?"N/A":c.getLastWatering()) + "]");
        if (d2 != null) c.setLastWatering(d2);

        int u = careDao.update(c);
        System.out.println("Updated Care rows: " + u);
    }

    private void deleteCare() throws SQLException {
        Integer pid = promptInt("Plant ID for Care delete");
        if (pid == null) return;
        int d = careDao.delete(pid);
        System.out.println("Deleted Care rows: " + d);
    }

    private void viewCareByPlant() throws SQLException {
        Integer pid = promptInt("Plant ID to view Care");
        if (pid == null) return;
        Care c = careDao.findByPlantId(pid);
        if (c == null) System.out.println("No Care record.");
        else {
            System.out.println("Last Soil Change: " + (c.getLastSoilChange()==null?"N/A":c.getLastSoilChange()));
            System.out.println("Last Watering   : " + (c.getLastWatering()==null?"N/A":c.getLastWatering()));
        }
    }

    // ---------- Information CRUD ----------
    private void createInformation() throws SQLException {
        Integer pid = promptInt("Plant ID for Information");
        if (pid == null) return;
        createInformationForPlant(pid);
    }

    private void createInformationForPlant(int plantId) throws SQLException {
        Boolean fromAnother = promptBool("From another plant?");
        String soil = prompt("SoilType");
        String pot = prompt("PotSize");
        Boolean globe = promptBool("Water globe required?");
        Information i = new Information(plantId,
                fromAnother == null ? false : fromAnother,
                soil,
                pot,
                globe == null ? false : globe);
        int r = informationDao.insert(i);
        System.out.println("Inserted Information rows: " + r);
    }

    private void updateInformation() throws SQLException {
        Integer pid = promptInt("Plant ID for Information update");
        if (pid == null) return;
        Information i = informationDao.findByPlantId(pid);
        if (i == null) { System.out.println("No Information record found for that plant."); return; }

        Boolean b1 = promptBool("From another plant? (current: " + i.isFromAnotherPlant() + ")");
        if (b1 != null) i.setFromAnotherPlant(b1);
        String s = prompt("SoilType [" + safe(i.getSoilType(), 30) + "]");
        if (!s.isEmpty()) i.setSoilType(s);
        String p = prompt("PotSize [" + safe(i.getPotSize(), 15) + "]");
        if (!p.isEmpty()) i.setPotSize(p);
        Boolean b2 = promptBool("Water globe required? (current: " + i.isWaterGlobeRequired() + ")");
        if (b2 != null) i.setWaterGlobeRequired(b2);

        int u = informationDao.update(i);
        System.out.println("Updated Information rows: " + u);
    }

    private void deleteInformation() throws SQLException {
        Integer pid = promptInt("Plant ID for Information delete");
        if (pid == null) return;
        int d = informationDao.delete(pid);
        System.out.println("Deleted Information rows: " + d);
    }

    private void viewInformationByPlant() throws SQLException {
        Integer pid = promptInt("Plant ID to view Information");
        if (pid == null) return;
        Information i = informationDao.findByPlantId(pid);
        if (i == null) System.out.println("No Information record.");
        else {
            System.out.println("From another plant : " + i.isFromAnotherPlant());
            System.out.println("Soil type          : " + safe(i.getSoilType(), 40));
            System.out.println("Pot size           : " + safe(i.getPotSize(), 20));
            System.out.println("Water globe req.   : " + i.isWaterGlobeRequired());
        }
    }

    // ---------- Location CRUD ----------
    private void createLocation() throws SQLException {
        Integer pid = promptInt("Plant ID for new Location");
        if (pid == null) return;
        createLocationForPlant(pid);
    }

    private void createLocationForPlant(int plantId) throws SQLException {
        String locName = prompt("location_name");
        String light = prompt("LightLevel");
        Location l = new Location(plantId, locName, light);
        int r = locationDao.insert(l);
        System.out.println("Inserted Location rows: " + r);
    }

    private void updateLocation() throws SQLException {
        Integer pid = promptInt("Plant ID for Location update");
        if (pid == null) return;
        String locName = prompt("location_name (the key)");
        if (locName.isEmpty()) { System.out.println("location_name required."); return; }
        Location l = locationDao.find(pid, locName);
        if (l == null) { System.out.println("Location record not found."); return; }
        String light = prompt("LightLevel [" + safe(l.getLightLevel(), 20) + "]");
        if (!light.isEmpty()) l.setLightLevel(light);
        int u = locationDao.update(l);
        System.out.println("Updated Location rows: " + u);
    }

    private void deleteLocation() throws SQLException {
        Integer pid = promptInt("Plant ID for Location delete");
        if (pid == null) return;
        String locName = prompt("location_name to delete");
        if (locName.isEmpty()) { System.out.println("location_name required."); return; }
        int d = locationDao.delete(pid, locName);
        System.out.println("Deleted Location rows: " + d);
    }

    private void listLocationsForPlant() throws SQLException {
        Integer pid = promptInt("Plant ID to list Locations");
        if (pid == null) return;
        List<Location> locs = locationDao.findForPlant(pid);
        if (locs == null || locs.isEmpty()) System.out.println("No locations.");
        else {
            System.out.printf("%-20s %-10s\n", "location_name", "LightLevel");
            for (Location L : locs) {
                System.out.printf("%-20s %-10s\n", L.getLocationName(), safe(L.getLightLevel(), 10));
            }
        }
    }

    // ---------- utility ----------
    private void printPlantFull(Plant p) {
        System.out.println("Plant ID: " + p.getPlantId());
        System.out.println("Name    : " + safe(p.getName(), 40));
        System.out.println("Type    : " + safe(p.getType(), 40));
        System.out.println("Height  : " + (p.getHeight() == null ? "N/A" : p.getHeight()));
        System.out.println("Acquired: " + (p.getDateAcquired() == null ? "N/A" : p.getDateAcquired()));
        System.out.println("LocName : " + safe(p.getLocationName(), 40));
        try {
            Care c = careDao.findByPlantId(p.getPlantId());
            Information i = informationDao.findByPlantId(p.getPlantId());
            List<Location> locs = locationDao.findForPlant(p.getPlantId());

            System.out.println("\n--- Care ---");
            if (c != null) {
                System.out.println("LastSoilChange: " + (c.getLastSoilChange()==null?"N/A":c.getLastSoilChange()));
                System.out.println("LastWatering  : " + (c.getLastWatering()==null?"N/A":c.getLastWatering()));
            } else System.out.println("No Care record.");

            System.out.println("\n--- Information ---");
            if (i != null) {
                System.out.println("FromAnotherPlant: " + i.isFromAnotherPlant());
                System.out.println("SoilType        : " + safe(i.getSoilType(),40));
                System.out.println("PotSize         : " + safe(i.getPotSize(),20));
                System.out.println("WaterGlobeReq   : " + i.isWaterGlobeRequired());
            } else System.out.println("No Information record.");

            System.out.println("\n--- Locations ---");
            if (locs != null && !locs.isEmpty()) {
                for (Location L : locs) {
                    System.out.println(" - " + L.getLocationName() + " (Light: " + safe(L.getLightLevel(),20) + ")");
                }
            } else System.out.println("No locations recorded.");
        } catch (SQLException e) {
            System.err.println("Error retrieving related records: " + e.getMessage());
        }
    }

    private static String safe(String s, int max) {
        if (s == null) return "N/A";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}
