package com.planttracker.business;

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

/**
 * BusinessManager: simple business layer over DAOs.
 *
 * Methods:
 *  - saveX(...) : if id == 0 then insert, else update
 *  - deleteX(...)
 *  - getXById(...) (calls DAO read)
 *  - getAllX(...) (calls DAO readAll)
 *
 * Notes:
 *  - savePlant when id == 0 calls plantDao.insert(p) and then attempts to set p.plantId
 *    by calling plantDao.getMaxPlantId() if available.
 *  - For Care/Information/Location save methods we try to detect existing row via DAO read methods.
 */
public class BusinessManager {

    private PlantDao plantDao;
    private CareDao careDao;
    private InformationDao informationDao;
    private LocationDao locationDao;

    public BusinessManager(String url, String user, String pass) {
        // initialize DAOs (constructor signatures assumed: (url,user,pass))
        this.plantDao = new PlantDao(url, user, pass);
        this.careDao = new CareDao(url, user, pass);
        this.informationDao = new InformationDao(url, user, pass);
        this.locationDao = new LocationDao(url, user, pass);
    }

    // ---------------- Plant ----------------
    public Plant savePlant(Plant p) throws SQLException {
        if (p == null) throw new IllegalArgumentException("Plant is null");
        if (p.getPlantId() == 0) {
            // insert
            plantDao.insert(p);
            // best-effort: set plant id by asking DAO for max id (requires PlantDao.getMaxPlantId())
            try {
                int max = plantDao.getMaxPlantId(); // implement this in PlantDao if not present
                if (max > 0) p.setPlantId(max);
            } catch (Exception ignore) {
                // If not present, ignore — caller can later refresh
            }
        } else {
            plantDao.update(p);
        }
        return p;
    }

    public int deletePlant(int plantId) throws SQLException {
        return plantDao.delete(plantId);
    }

    public Plant getPlantById(int plantId) throws SQLException {
        return plantDao.findById(plantId);
    }

    public List<Plant> getAllPlants() throws SQLException {
        return plantDao.findAll();
    }

    // ---------------- Care ----------------
    public Care saveCare(Care c) throws SQLException {
        if (c == null) throw new IllegalArgumentException("Care is null");
        if (c.getPlantId() == 0) throw new IllegalArgumentException("Care must have plantId set");

        Care existing = careDao.findByPlantId(c.getPlantId());
        if (existing == null) {
            careDao.insert(c);
        } else {
            careDao.update(c);
        }
        return c;
    }

    public int deleteCare(int plantId) throws SQLException {
        return careDao.delete(plantId);
    }

    public Care getCareByPlantId(int plantId) throws SQLException {
        return careDao.findByPlantId(plantId);
    }

    public List<Care> getAllCare() throws SQLException {
        return careDao.findAll();
    }

    // ---------------- Information ----------------
    public Information saveInformation(Information i) throws SQLException {
        if (i == null) throw new IllegalArgumentException("Information is null");
        if (i.getPlantId() == 0) throw new IllegalArgumentException("Information must have plantId set");

        Information existing = informationDao.findByPlantId(i.getPlantId());
        if (existing == null) {
            informationDao.insert(i);
        } else {
            informationDao.update(i);
        }
        return i;
    }

    public int deleteInformation(int plantId) throws SQLException {
        return informationDao.delete(plantId);
    }

    public Information getInformationByPlantId(int plantId) throws SQLException {
        return informationDao.findByPlantId(plantId);
    }

    public List<Information> getAllInformation() throws SQLException {
        return informationDao.findAll();
    }

    // ---------------- Location ----------------
    public Location saveLocation(Location l) throws SQLException {
        if (l == null) throw new IllegalArgumentException("Location is null");
        if (l.getPlantId() == 0) throw new IllegalArgumentException("Location must have plantId set");

        // Try to find existing location by composite key (plantId + locationName)
        Location existing = null;
        try {
            existing = locationDao.find(l.getPlantId(), l.getLocationName());
        } catch (Exception ex) {
            // If DAO doesn't provide find(plantId,locationName), fallback to scanning
            List<Location> list = locationDao.findForPlant(l.getPlantId());
            for (Location x : list) {
                String a = x.getLocationName() == null ? "" : x.getLocationName();
                String b = l.getLocationName() == null ? "" : l.getLocationName();
                if (a.equals(b)) { existing = x; break; }
            }
        }

        if (existing == null) {
            locationDao.insert(l);
        } else {
            locationDao.update(l);
        }
        return l;
    }

    public int deleteLocation(int plantId, String locationName) throws SQLException {
        return locationDao.delete(plantId, locationName);
    }

    public Location getLocation(int plantId, String locationName) throws SQLException {
        return locationDao.find(plantId, locationName);
    }

    public List<Location> getAllLocationsForPlant(int plantId) throws SQLException {
        return locationDao.findForPlant(plantId);
    }

    public List<Location> getAllLocations() throws SQLException {
        return locationDao.findAll();
    }
}