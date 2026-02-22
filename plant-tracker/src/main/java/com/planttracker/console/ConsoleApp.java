package com.planttracker.console;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;

/**
 * Console client to exercise the Plant service.
 * - Set PLANT_SERVICE_URL env var to point at your service (defaults to
 * https://csce548.onrender.com)
 * - Run with: mvn clean compile exec:java
 */

/**
 * ========================================
 * CONSOLE TEST CLIENT
 * ========================================
 *
 * This console-based application is used to test the deployed REST service.
 *
 * It performs the following sequence:
 *   1. CREATE a new Plant (POST)
 *   2. READ the created Plant (GET)
 *   3. UPDATE the Plant (PUT)
 *   4. DELETE the Plant (DELETE)
 *   5. VERIFY deletion (GET)
 *
 * The service is hosted on Render:
 *   https://csce548.onrender.com
 *
 * The console client communicates via HTTP using Java HttpClient.
 *
 * This verifies full end-to-end functionality of:
 *   Console → REST API → Business Layer → DAO Layer → PostgreSQL DB
 */

public class ConsoleApp {

    // change this to your Render URL when testing in the cloud (or set
    // PLANT_SERVICE_URL)
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
            System.out.println("Console client starting (base url: " + BASE_URL + ")");

            // 1) Create a new plant
            Plant newPlant = new Plant();
            newPlant.setName("Test Fern");
            newPlant.setType("Fern");
            newPlant.setHeight(12.5);
            newPlant.setDateAcquired(LocalDate.now());
            newPlant.setLocationName("Office Desk");

            System.out.println("\n--- Creating plant ---");
            Plant created = createPlant(newPlant);
            if (created == null) {
                System.err.println("Create failed - aborting test");
                return;
            }
            System.out.println("Created: " + created);

            Integer idObj = created.getId();
            if (idObj == null) {
                System.err.println("Server did not return an id (id == null). Aborting.");
                return;
            }
            int id = idObj.intValue();

            System.out.println("\n--- Read (GET) created plant ---");
            Plant fetched = getPlant(id);
            System.out.println("Fetched: " + fetched);

            // 2) Update the plant
            System.out.println("\n--- Updating plant (height -> 20.0) ---");
            if (fetched != null) {
                fetched.setHeight(20.0);
                Plant updated = updatePlant(id, fetched);
                System.out.println("Updated: " + updated);
            } else {
                System.out.println("Skipping update because fetched is null.");
            }

            // 3) Read again
            System.out.println("\n--- Read (GET) after update ---");
            Plant afterUpdate = getPlant(id);
            System.out.println("After update: " + afterUpdate);

            // 4) Delete the plant
            System.out.println("\n--- Deleting plant ---");
            boolean deleted = deletePlant(id);
            System.out.println("Deleted success: " + deleted);

            // 5) Attempt to read deleted plant
            System.out.println("\n--- Read (GET) after delete (should be 404 or null) ---");
            Plant afterDelete = getPlant(id);
            System.out.println("After delete fetch returned: " + afterDelete);

            System.out.println("\nConsole test finished.");
        } catch (Throwable t) {
            System.err.println("Console client failed with exception:");
            t.printStackTrace(System.err);
            System.exit(2);
        }
    }

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

    // Minimal DTO matching your Plant JSON shape — accept several id names via
    // aliases
    public static class Plant {
        @JsonProperty("id")
        @JsonAlias({ "plantId", "plant_id", "Plant_ID", "PlantId", "PlantID" })
        private Integer id; // server-assigned PK

        private String name;
        private String type;
        private Double height;
        private LocalDate dateAcquired;
        private String locationName;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getHeight() {
            return height;
        }

        public void setHeight(Double height) {
            this.height = height;
        }

        public LocalDate getDateAcquired() {
            return dateAcquired;
        }

        public void setDateAcquired(LocalDate dateAcquired) {
            this.dateAcquired = dateAcquired;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }

        @Override
        public String toString() {
            return "Plant{id=" + id + ", name='" + name + '\'' +
                    ", type='" + type + '\'' + ", height=" + height +
                    ", dateAcquired=" + dateAcquired + ", locationName='" + locationName + '\'' + '}';
        }
    }
}