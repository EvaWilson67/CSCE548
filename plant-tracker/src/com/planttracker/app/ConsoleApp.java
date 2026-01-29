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
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Simple console frontend for the PlantTracker DAOs.
 *
 * Expects environment variables:
 *   PLANTDB_URL (default: jdbc:mysql://localhost:3306/PlantDB?serverTimezone=UTC)
 *   PLANTDB_USER (default: root)
 *   PLANTDB_PASS (default: empty)
 *
 * Usage (from project root, no build tool):
 * 1) compile all classes into out/
 * 2) run:
 *    java -cp "out:lib/*" com.example.planttracker.app.ConsoleApp
 *
 * (Use ';' instead of ':' on Windows classpath.)
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
            switch (choice) {
                case "1": listAllPlants(); break;
                case "2": viewPlantById(); break;
                case "3": searchPlantsByName(); break;
                case "4": listPlantsSummary(); break;
                case "x":
                case "X": quit = true; break;
                default: System.out.println("Unknown choice. Try again."); break;
            }
        }
        System.out.println("Goodbye 👋");
    }

    private void printHeader() {
        System.out.println("======================================");
        System.out.println("       PlantTracker Console UI        ");
        System.out.println("======================================");
    }

    private void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("  1) List all plants (detailed)");
        System.out.println("  2) View plant by ID (Plant + Care + Info + Locations)");
        System.out.println("  3) Search plants by name");
        System.out.println("  4) List plants (id, name, type) summary");
        System.out.println("  X) Exit");
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private void listAllPlants() {
        try {
            List<Plant> all = plantDao.findAll();
            if (all.isEmpty()) {
                System.out.println("No plants found.");
                return;
            }
            for (Plant p : all) {
                printPlantFull(p);
                System.out.println("--------------------------------------");
            }
            System.out.printf("Total: %d plants\n", all.size());
        } catch (SQLException e) {
            System.err.println("Error listing plants: " + e.getMessage());
        }
    }

    private void listPlantsSummary() {
        try {
            List<Plant> all = plantDao.findAll();
            if (all.isEmpty()) {
                System.out.println("No plants found.");
                return;
            }
            System.out.printf("%-6s %-20s %-20s\n", "ID", "Name", "Type");
            System.out.println("----------------------------------------------");
            for (Plant p : all) {
                System.out.printf("%-6d %-20s %-20s\n", p.getPlantId(), safe(p.getName(), 20), safe(p.getType(), 20));
            }
            System.out.printf("Total: %d plants\n", all.size());
        } catch (SQLException e) {
            System.err.println("Error listing plants summary: " + e.getMessage());
        }
    }

    private void viewPlantById() {
        String s = prompt("Enter Plant ID");
        if (s.isEmpty()) { System.out.println("Cancelled."); return; }
        int id;
        try {
            id = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid ID.");
            return;
        }

        try {
            Plant p = plantDao.findById(id);
            if (p == null) {
                System.out.println("No plant found with ID " + id);
                return;
            }
            printPlantFull(p);
        } catch (SQLException e) {
            System.err.println("Error retrieving plant: " + e.getMessage());
        }
    }

    private void searchPlantsByName() {
        String q = prompt("Enter search term (partial name)");
        if (q.isEmpty()) { System.out.println("Cancelled."); return; }

        try {
            List<Plant> all = plantDao.findAll();
            List<Plant> matches = all.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q.toLowerCase()))
                    .collect(Collectors.toList());

            if (matches.isEmpty()) {
                System.out.println("No plants match \"" + q + "\"");
                return;
            }

            System.out.printf("Found %d matches:\n", matches.size());
            for (Plant p : matches) {
                System.out.printf("ID: %d  Name: %s  Type: %s\n", p.getPlantId(), p.getName(), p.getType());
            }
            String pick = prompt("Enter ID to view details or press Enter to return");
            if (!pick.isEmpty()) {
                try {
                    int id = Integer.parseInt(pick);
                    Plant p = plantDao.findById(id);
                    if (p != null) printPlantFull(p);
                    else System.out.println("No plant with that ID.");
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid ID.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching plants: " + e.getMessage());
        }
    }

    private void printPlantFull(Plant p) {
        System.out.printf("Plant ID: %d\n", p.getPlantId());
        System.out.printf("Name    : %s\n", safe(p.getName(), 40));
        System.out.printf("Type    : %s\n", safe(p.getType(), 40));
        System.out.printf("Height  : %s\n", p.getHeight() == null ? "N/A" : p.getHeight().toString());
        System.out.printf("Acquired: %s\n", p.getDateAcquired() == null ? "N/A" : p.getDateAcquired().toString());
        System.out.printf("LocName : %s\n", safe(p.getLocationName(), 40));

        try {
            Care c = careDao.findByPlantId(p.getPlantId());
            Information i = informationDao.findByPlantId(p.getPlantId());
            List<Location> locs = locationDao.findForPlant(p.getPlantId());

            System.out.println("\n--- Care ---");
            if (c != null) {
                System.out.printf("Last Soil Change: %s\n", c.getLastSoilChange() == null ? "N/A" : c.getLastSoilChange());
                System.out.printf("Last Watering   : %s\n", c.getLastWatering() == null ? "N/A" : c.getLastWatering());
            } else {
                System.out.println("No care record.");
            }

            System.out.println("\n--- Information ---");
            if (i != null) {
                System.out.printf("From Another Plant : %s\n", i.isFromAnotherPlant());
                System.out.printf("Soil Type          : %s\n", safe(i.getSoilType(), 40));
                System.out.printf("Pot Size           : %s\n", safe(i.getPotSize(), 20));
                System.out.printf("Water Globe Req.   : %s\n", i.isWaterGlobeRequired());
            } else {
                System.out.println("No information record.");
            }

            System.out.println("\n--- Locations ---");
            if (locs != null && !locs.isEmpty()) {
                for (Location L : locs) {
                    System.out.printf(" - %s (Light: %s)\n", L.getLocationName(), safe(L.getLightLevel(), 20));
                }
            } else {
                System.out.println("No locations recorded.");
            }

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
