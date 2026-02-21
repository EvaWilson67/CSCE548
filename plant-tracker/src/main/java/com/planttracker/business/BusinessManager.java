package com.planttracker.business;

import com.planttracker.dao.*;
import com.planttracker.model.*;

import java.sql.SQLException;
import java.util.List;

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

    public void savePlant(Plant plant) throws SQLException {
        if (plant.getPlantId() == 0) {
            plantDao.insert(plant);
        } else {
            plantDao.update(plant);
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

    public void saveCare(Care care) throws SQLException {
        Care existing = careDao.findByPlantId(care.getPlantId());
        if (existing == null) {
            careDao.insert(care);
        } else {
            careDao.updateByPlantId(care);  
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

    public void saveInformation(Information info) throws SQLException {
        Information existing = informationDao.findByPlantId(info.getPlantId());
        if (existing == null) {
            informationDao.insert(info);
        } else {
            informationDao.updateByPlantId(info);  
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

    public void saveLocation(Location location) throws SQLException {
        Location existing = locationDao.findByPlantId(location.getPlantId());
        if (existing == null) {
            locationDao.insert(location);
        } else {
            locationDao.updateByPlantId(location);
        }
    }

    public Location getLocation(int plantId) throws SQLException {
        return locationDao.findByPlantId(plantId);
    }

    public void deleteLocation(int plantId) throws SQLException {
        locationDao.deleteByPlantId(plantId);
    }
}