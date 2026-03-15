package com.planttracker.dao;

import com.planttracker.model.Location;
import com.planttracker.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDao {

    public LocationDao() {
    }

    /**
     * Insert a Location row for a plant.
     * Columns: Plant_ID (int), location_name (varchar), LightLevel (varchar)
     */
    public int insert(Location loc) throws SQLException {
        String sql = "INSERT INTO Location (Plant_ID, location_name, LightLevel) VALUES (?, ?, ?)";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, loc.getPlantId());
            ps.setString(2, loc.getLocationName());
            ps.setString(3, loc.getLightLevel());
            int affected = ps.executeUpdate();
            System.out.println("LocationDao.insert -> affected: " + affected + ", plantId=" + loc.getPlantId() + ", locationName=" + loc.getLocationName());
            return affected;
        }
    }

    /**
     * Update the Location row identified by Plant_ID.
     * Now updates the location_name and LightLevel for the plant_id.
     */
    public int updateByPlantId(Location loc) throws SQLException {
        String sql = "UPDATE Location SET location_name = ?, LightLevel = ? WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // set new values (name first, then light level)
            ps.setString(1, loc.getLocationName());
            ps.setString(2, loc.getLightLevel());
            ps.setInt(3, loc.getPlantId());

            int affected = ps.executeUpdate();
            System.out.println("LocationDao.updateByPlantId SQL: " + sql + " params: [locationName=" + loc.getLocationName() + ", lightLevel=" + loc.getLightLevel() + ", plantId=" + loc.getPlantId() + "] -> affected rows: " + affected);
            return affected;
        }
    }

    public int deleteByPlantIdAndName(int plantId, String locationName) throws SQLException {
        String sql = "DELETE FROM Location WHERE Plant_ID = ? AND location_name = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            ps.setString(2, locationName);
            int affected = ps.executeUpdate();
            System.out.println("LocationDao.deleteByPlantIdAndName -> affected: " + affected + ", plantId=" + plantId + ", locationName=" + locationName);
            return affected;
        }
    }

    public int deleteByPlantId(int plantId) throws SQLException {
        String sql = "DELETE FROM Location WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            int affected = ps.executeUpdate();
            System.out.println("LocationDao.deleteByPlantId -> affected: " + affected + ", plantId=" + plantId);
            return affected;
        }
    }

    /**
     * Find a single Location for a plant. If you expect multiple locations for a
     * plant, use findAllForPlant instead.
     */
    public Location findByPlantId(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, location_name, LightLevel FROM Location WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Location l = new Location();
                    l.setPlantId(rs.getInt("Plant_ID"));
                    l.setLocationName(rs.getString("location_name"));
                    l.setLightLevel(rs.getString("LightLevel"));
                    return l;
                }
            }
        }
        return null;
    }

    // in LocationDao.java
    public int renameLocationForPlant(int plantId, String newLocationName) throws SQLException {
        String sql = "UPDATE Location SET location_name = ? WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newLocationName);
            ps.setInt(2, plantId);
            int affected = ps.executeUpdate();
            System.out.println("LocationDao.renameLocationForPlant -> affected: " + affected + ", plantId=" + plantId + ", newLocationName=" + newLocationName);
            return affected; // returns number of rows updated
        }
    }

    /**
     * Return all locations for a plant (useful if a plant may have multiple
     * locations recorded).
     */
    public List<Location> findAllForPlant(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, location_name, LightLevel FROM Location WHERE Plant_ID = ?";
        List<Location> out = new ArrayList<>();
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Location l = new Location();
                    l.setPlantId(rs.getInt("Plant_ID"));
                    l.setLocationName(rs.getString("location_name"));
                    l.setLightLevel(rs.getString("LightLevel"));
                    out.add(l);
                }
            }
        }
        return out;
    }
}