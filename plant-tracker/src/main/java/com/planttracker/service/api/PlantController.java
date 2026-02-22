package com.planttracker.service.api;

import com.planttracker.business.BusinessManager;
import com.planttracker.model.Care;
import com.planttracker.model.Information;
import com.planttracker.model.Location;
import com.planttracker.model.Plant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PlantController {

    private final BusinessManager mgr;

    public PlantController(BusinessManager mgr) {
        this.mgr = mgr;
    }

    // ---------- Plant ----------
    @GetMapping("/plants")
    public ResponseEntity<List<Plant>> listPlants() throws SQLException {
        return ResponseEntity.ok(mgr.getAllPlants());
    }

    @GetMapping("/plants/{id}")
    public ResponseEntity<Plant> getPlant(@PathVariable int id) throws SQLException {
        Plant p = mgr.getPlant(id);
        return p == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
    }

    @PostMapping("/plants")
    public ResponseEntity<Plant> savePlant(@RequestBody Plant plant) throws SQLException {
        Plant saved = mgr.savePlant(plant);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/plants/{id}")
    public ResponseEntity<Plant> updatePlant(@PathVariable int id, @RequestBody Plant plant) throws SQLException {
        plant.setPlantId(id);
        Plant saved = mgr.savePlant(plant);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/plants/{id}")
    public ResponseEntity<?> deletePlant(@PathVariable int id) throws SQLException {
        mgr.deletePlant(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Care ----------
    @GetMapping("/plants/{id}/care")
    public ResponseEntity<Care> getCare(@PathVariable int id) throws SQLException {
        Care c = mgr.getCare(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    @PostMapping("/plants/{id}/care")
    public ResponseEntity<Care> saveCare(@PathVariable int id, @RequestBody Care care) throws SQLException {
        care.setPlantId(id);
        Care saved = mgr.saveCare(care);
        return ResponseEntity.ok(saved);
    }

    // NEW: PUT for care (update existing care or create if missing)
    @PutMapping("/plants/{id}/care")
    public ResponseEntity<Care> updateCare(@PathVariable int id, @RequestBody Care care) throws SQLException {
        care.setPlantId(id);
        Care saved = mgr.saveCare(care);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/plants/{id}/care")
    public ResponseEntity<?> deleteCare(@PathVariable int id) throws SQLException {
        mgr.deleteCare(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Information ----------
    @GetMapping("/plants/{id}/information")
    public ResponseEntity<Information> getInformation(@PathVariable int id) throws SQLException {
        Information info = mgr.getInformation(id);
        return info == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(info);
    }

    @PostMapping("/plants/{id}/information")
    public ResponseEntity<Information> saveInformation(@PathVariable int id, @RequestBody Information info) throws SQLException {
        info.setPlantId(id);
        Information saved = mgr.saveInformation(info);
        return ResponseEntity.ok(saved);
    }

    // NEW: PUT for information
    @PutMapping("/plants/{id}/information")
    public ResponseEntity<Information> updateInformation(@PathVariable int id, @RequestBody Information info) throws SQLException {
        info.setPlantId(id);
        Information saved = mgr.saveInformation(info);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/plants/{id}/information")
    public ResponseEntity<?> deleteInformation(@PathVariable int id) throws SQLException {
        mgr.deleteInformation(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Location ----------
    @GetMapping("/plants/{id}/location")
    public ResponseEntity<Location> getLocation(@PathVariable int id) throws SQLException {
        Location l = mgr.getLocation(id);
        return l == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(l);
    }

    @PostMapping("/plants/{id}/location")
    public ResponseEntity<Location> saveLocation(@PathVariable int id, @RequestBody Location l) throws SQLException {
        l.setPlantId(id);
        Location saved = mgr.saveLocation(l);
        return ResponseEntity.ok(saved);
    }

    // NEW: PUT for location
    @PutMapping("/plants/{id}/location")
    public ResponseEntity<Location> updateLocation(@PathVariable int id, @RequestBody Location l) throws SQLException {
        l.setPlantId(id);
        Location saved = mgr.saveLocation(l);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/plants/{id}/location")
    public ResponseEntity<?> deleteLocation(@PathVariable int id) throws SQLException {
        mgr.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}