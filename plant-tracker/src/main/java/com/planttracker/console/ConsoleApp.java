package com.planttracker.console;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planttracker.dao.PlantDao;
import com.planttracker.business.BusinessManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;

/**
 * Console client to exercise the Plant service.
 * - Set PLANT_SERVICE_URL env var to point at your service (defaults to
 *   https://csce548.onrender.com)
 * - Choose layer to test:
 *     TEST_LAYER env var or first command-line arg: "data" | "bus" | "svc"
 *   Example:
 *     mvn compile exec:java -Dexec.mainClass="com.planttracker.console.ConsoleApp" -Dexec.args="data"
 */

/**
 * ========================================
 * CONSOLE TEST CLIENT
 * ========================================
 *
 * This console-based application is used to test the service and layers:
 *
 *   - Data layer (direct DAO): insert -> read -> update -> delete
 *   - Business layer (BusinessManager): same sequence using business API
 *   - Service layer (HTTP): same sequence via REST endpoints
 *
 * The service is assumed hosted at:
 *   https://csce548.onrender.com
 *
 * The console client communicates via Java HttpClient for service tests,
 * and directly calls DAO / BusinessManager for local-layer tests.
 */
public class ConsoleApp {

    // change this to your Render URL when testing in the cloud (or set PLANT_SERVICE_URL)
    private static final String BASE_URL = System.getenv().getOrDefault("PLANT_SERVICE_URL",
            "https://csce548.onrender.com");

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Force HTTP/1.1 to avoid some HTTP/2 server compatibility issues
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static void main(String[] args) {
        try {
            String defaultLayer = "";
            String layer = System.getenv().getOrDefault("TEST_LAYER",
                    (args != null && args.length > 0) ? args[0] : defaultLayer);

            System.out.println("Console client starting (mode: " + layer + ", base url: " + BASE_URL + ")");

            if ("data".equalsIgnoreCase(layer)) {
                TestDataLayer();
            } else if ("bus".equalsIgnoreCase(layer) || "business".equalsIgnoreCase(layer)) {
                TestBusinessLayer();
            } else {
                // default to service tests
                TestServiceLayer();
            }

            System.out.println("\nConsole client finished.");
        } catch (Throwable t) {
            System.err.println("Console client failed with exception:");
            t.printStackTrace(System.err);
            System.exit(2);
        }
    }

    // ---------------------------------------------------
    // SERVICE LAYER (HTTP) - uses existing helpers below
    // ---------------------------------------------------
    private static void TestServiceLayer() {
        System.out.println("=== Running Service Layer Tests (HTTP -> /api/plants) ===");

        System.out.println("This takes a longer time because it is connecting to render...");
        try {
            // create JSON using client DTO
            Plant newPlant = new Plant();
            newPlant.setName("SVC-Test-" + System.currentTimeMillis());
            newPlant.setType("ServicePlant");
            newPlant.setHeight(6.5);
            newPlant.setDateAcquired(LocalDate.now());
            newPlant.setLocationName("Cloud");

            Plant created = createPlant(newPlant);
            if (created == null) {
                System.err.println("Service create returned null / failed. Check server logs.");
                return;
            }
            System.out.println("Created via service: " + created);

            Integer idObj = created.getId();
            if (idObj == null) {
                System.err.println("Created plant did not include id. Aborting service test.");
                return;
            }
            int id = idObj;

            Plant fetched = getPlant(id);
            System.out.println("Fetched via service: " + fetched);

            // Update
            if (fetched != null) {
                fetched.setHeight(12.3);
                Plant updated = updatePlant(id, fetched);
                System.out.println("Updated via service: " + updated);
            } else {
                System.out.println("Skipping update because fetched is null.");
            }

            // Delete
            boolean deleted = deletePlant(id);
            System.out.println("Deleted via service (success = " + deleted + ")");

            Plant afterDelete = getPlant(id);
            System.out.println("After delete (should be null): " + afterDelete);

        } catch (Exception e) {
            System.err.println("Service layer test failed:");
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------
    // DATA LAYER (DAO) - direct DB access; runs locally
    // ---------------------------------------------------
    private static void TestDataLayer() {
        System.out.println("=== Running Data Layer Tests (direct DAO) ===");
        PlantDao dao = new PlantDao();
        try {
            // Build server-side model instance using fully-qualified class
            com.planttracker.model.Plant p = new com.planttracker.model.Plant();
            p.setName("DL-Test-" + System.currentTimeMillis());
            p.setType("TestPlant");
            p.setHeight(10.0);
            p.setDateAcquired(LocalDate.now());
            p.setLocationName("Desk");

            System.out.println("Inserting plant via DAO: " + p.getName());
            int inserted = dao.insert(p);   // PlantDao.insert is expected to set Plant_ID back on p
            System.out.println("Inserted rows: " + inserted + ", new id: " + p.getPlantId());

            int newId = p.getPlantId();
            com.planttracker.model.Plant read = dao.findById(newId);
            System.out.println("Read back: " + read);

            // Update
            read.setHeight(15.5);
            int updated = dao.update(read);
            System.out.println("Updated rows: " + updated);
            com.planttracker.model.Plant afterUpdate = dao.findById(newId);
            System.out.println("After update: " + afterUpdate);

            // Delete
            int deleted = dao.delete(newId);
            System.out.println("Deleted rows: " + deleted);
            com.planttracker.model.Plant afterDelete = dao.findById(newId);
            System.out.println("After delete (should be null): " + afterDelete);

        } catch (Exception e) {
            System.err.println("Data layer test failed:");
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------
    // BUSINESS LAYER (BusinessManager) - local invocation
    // ---------------------------------------------------
    private static void TestBusinessLayer() {
        System.out.println("=== Running Business Layer Tests (BusinessManager) ===");
        BusinessManager bm = new BusinessManager();
        try {
            // Create server model instance
            com.planttracker.model.Plant p = new com.planttracker.model.Plant();
            p.setName("BL-Test-" + System.currentTimeMillis());
            p.setType("BusinessPlant");
            p.setHeight(7.25);
            p.setDateAcquired(LocalDate.now());
            p.setLocationName("Office");

            System.out.println("Saving plant via BusinessManager.savePlant()");
            bm.savePlant(p); // should insert and set Plant_ID on the object
            System.out.println("Saved plant id: " + p.getPlantId());

            com.planttracker.model.Plant fetched = bm.getPlant(p.getPlantId());
            System.out.println("Fetched via BusinessManager: " + fetched);

            // Update
            fetched.setHeight(9.9);
            bm.savePlant(fetched); // update path
            com.planttracker.model.Plant afterUpdate = bm.getPlant(fetched.getPlantId());
            System.out.println("After update: " + afterUpdate);

            // Delete
            bm.deletePlant(afterUpdate.getPlantId());
            com.planttracker.model.Plant afterDelete = bm.getPlant(afterUpdate.getPlantId());
            System.out.println("After delete (should be null): " + afterDelete);

        } catch (Exception e) {
            System.err.println("Business layer test failed:");
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------
    // REST helpers (existing implementation kept)
    // ---------------------------------------------------
    private static Plant createPlant(Plant p) throws IOException, InterruptedException {
        String json = MAPPER.writeValueAsString(p);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/plants"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        int code = resp.statusCode();
        System.out.println("POST /api/plants -> " + code);
        if (code == 200 || code == 201) {
            try {
                return MAPPER.readValue(resp.body(), Plant.class);
            } catch (Exception ex) {
                System.err.println("Failed to parse create response body: " + resp.body());
                throw ex;
            }
        } else {
            System.err.println("Create failed: " + resp.body());
            return null;
        }
    }

    private static Plant getPlant(int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/plants/" + id))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        int code = resp.statusCode();
        System.out.println("GET /api/plants/" + id + " -> " + code);
        if (code == 200) {
            return MAPPER.readValue(resp.body(), Plant.class);
        } else {
            System.out.println("GET returned non-200: " + resp.body());
            return null;
        }
    }

    private static Plant updatePlant(int id, Plant p) throws IOException, InterruptedException {
        // ask server which methods are allowed
        HttpRequest optionsReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/plants/" + id))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<Void> optionsResp = CLIENT.send(optionsReq, HttpResponse.BodyHandlers.discarding());
        String allow = optionsResp.headers().firstValue("Allow").orElse("");
        System.out.println("Allowed methods: " + allow);

        // choose method
        String methodToUse = null;
        if (allow.contains("PUT"))
            methodToUse = "PUT";
        else if (allow.contains("PATCH"))
            methodToUse = "PATCH";
        else if (allow.contains("POST"))
            methodToUse = "POST";

        if (methodToUse == null) {
            System.err.println("No supported update method found for /api/plants/" + id + ". Server Allow: " + allow);
            return null;
        }

        String json = MAPPER.writeValueAsString(p);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/plants/" + id))
                .header("Content-Type", "application/json")
                .method(methodToUse, HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println(methodToUse + " /api/plants/" + id + " -> " + resp.statusCode());
        if (resp.statusCode() == 200) {
            return MAPPER.readValue(resp.body(), Plant.class);
        } else {
            System.err.println("Update failed: " + resp.body());
            return null;
        }
    }

    private static boolean deletePlant(int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/plants/" + id))
                .DELETE()
                .build();

        HttpResponse<String> resp = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("DELETE /api/plants/" + id + " -> " + resp.statusCode());
        return resp.statusCode() == 200 || resp.statusCode() == 204;
    }

    // ---------------------------------------------------
    // Client DTO (keeps your existing JSON mapping behavior)
    // ---------------------------------------------------
    public static class Plant {
        @JsonProperty("id")
        @JsonAlias({ "plantId", "plant_id", "Plant_ID", "PlantId", "PlantID" })
        private Integer id; // server-assigned PK

        private String name;
        private String type;
        private Double height;
        private LocalDate dateAcquired;
        private String locationName;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Double getHeight() { return height; }
        public void setHeight(Double height) { this.height = height; }

        public LocalDate getDateAcquired() { return dateAcquired; }
        public void setDateAcquired(LocalDate dateAcquired) { this.dateAcquired = dateAcquired; }

        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }

        @Override
        public String toString() {
            return "Plant{id=" + id + ", name='" + name + '\'' +
                    ", type='" + type + '\'' + ", height=" + height +
                    ", dateAcquired=" + dateAcquired + ", locationName='" + locationName + '\'' + '}';
        }
    }
}