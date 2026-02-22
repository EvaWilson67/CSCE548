package com.planttracker.business;

import com.planttracker.dao.*;
import com.planttracker.model.*;

import java.sql.SQLException;
import java.util.List;

public class BusinessManager {

    private final PlantDao plantDao;
    private final CareDao careDao;
    private final InformationDao informationDao;
    private final LocationDao locationDao;

    public BusinessManager() {
        this.plantDao = new PlantDao();
        this.careDao = new CareDao();
        this.informationDao = new InformationDao();
        this.locationDao = new LocationDao();
    }

    // ======================================
    // ============ PLANT ===================
    // ======================================

    /**
     * Save a plant: insert (if no id) or update (if id present).
     * Returns the saved Plant (with id populated after insert).
     */
    public Plant savePlant(Plant plant) throws SQLException {
        Integer pid = plant.getPlantId();
        if (pid == null || pid == 0) {
            int newId = plantDao.insert(plant);
            plant.setPlantId(newId);
            return plant;
        } else {
            plantDao.update(plant);
            return plant;
        }
    }

    public Plant getPlant(int id) throws SQLException {
        return plantDao.findById(id);
    }

    public List<Plant> getAllPlants() throws SQLException {
        return plantDao.findAll();
    }

    public void deletePlant(int id) throws SQLException {
        plantDao.delete(id);
    }

    // ======================================
    // ============ CARE ====================
    // ======================================

    /**
     * Save or update care record. Returns the saved Care object.
     */
    public Care saveCare(Care care) throws SQLException {
        Care existing = careDao.findByPlantId(care.getPlantId());
        if (existing == null) {
            careDao.insert(care);
            // optionally re-query to return full persisted object
            return careDao.findByPlantId(care.getPlantId());
        } else {
            careDao.updateByPlantId(care);
            return careDao.findByPlantId(care.getPlantId());
        }
    }

    public Care getCare(int plantId) throws SQLException {
        return careDao.findByPlantId(plantId);
    }

    public void deleteCare(int plantId) throws SQLException {
        careDao.deleteByPlantId(plantId);
    }

    // ======================================
    // ============ INFORMATION =============
    // ======================================

    /**
     * Save or update information record. Returns the saved Information object.
     */
    public Information saveInformation(Information info) throws SQLException {
        Information existing = informationDao.findByPlantId(info.getPlantId());
        if (existing == null) {
            informationDao.insert(info);
            return informationDao.findByPlantId(info.getPlantId());
        } else {
            informationDao.updateByPlantId(info);
            return informationDao.findByPlantId(info.getPlantId());
        }
    }

    public Information getInformation(int plantId) throws SQLException {
        return informationDao.findByPlantId(plantId);
    }

    public void deleteInformation(int plantId) throws SQLException {
        informationDao.deleteByPlantId(plantId);
    }

    // ======================================
    // ============ LOCATION =================
    // ======================================

    /**
     * Save or update location record. Returns the saved Location object.
     */
    public Location saveLocation(Location location) throws SQLException {
        Location existing = locationDao.findByPlantId(location.getPlantId());
        if (existing == null) {
            locationDao.insert(location);
            return locationDao.findByPlantId(location.getPlantId());
        } else {
            locationDao.updateByPlantId(location);
            return locationDao.findByPlantId(location.getPlantId());
        }
    }

    public Location getLocation(int plantId) throws SQLException {
        return locationDao.findByPlantId(plantId);
    }

    public void deleteLocation(int plantId) throws SQLException {
        locationDao.deleteByPlantId(plantId);
    }
}