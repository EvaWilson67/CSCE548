package com.planttracker.dao;

// PlantTracker.java
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.planttracker.model.Care;
import com.planttracker.model.Information;
import com.planttracker.model.Plant;
import com.planttracker.model.Location;

public class PlantTracker {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;

    public PlantTracker(String jdbcUrl, String dbUser, String dbPassword) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    // ------------------------
    // Plant CRUD
    // ------------------------
    public int createPlant(Plant plant) throws SQLException {
        String sql = "INSERT INTO Plant (Plant_ID, Name, Type, Height, DateAcquired, location_name) VALUES (?, ?, ?, ?, ?, ?)";
        // if you prefer AUTO_INCREMENT usage, remove Plant_ID from the INSERT and use getGeneratedKeys()
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plant.getPlantId());
            ps.setString(2, plant.getName());
            ps.setString(3, plant.getType());
            if (plant.getHeight() != null) ps.setDouble(4, plant.getHeight());
            else ps.setNull(4, Types.DECIMAL);

            if (plant.getDateAcquired() != null) ps.setDate(5, Date.valueOf(plant.getDateAcquired()));
            else ps.setNull(5, Types.DATE);

            ps.setString(6, plant.getLocationName());
            return ps.executeUpdate();
        }
    }

    public Plant getPlantById(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, Name, Type, Height, DateAcquired, location_name FROM Plant WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPlant(rs);
                return null;
            }
        }
    }

    public List<Plant> getAllPlants() throws SQLException {
        String sql = "SELECT Plant_ID, Name, Type, Height, DateAcquired, location_name FROM Plant";
        List<Plant> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapPlant(rs));
        }
        return list;
    }

    public int updatePlant(Plant plant) throws SQLException {
        String sql = "UPDATE Plant SET Name = ?, Type = ?, Height = ?, DateAcquired = ?, location_name = ? WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plant.getName());
            ps.setString(2, plant.getType());
            if (plant.getHeight() != null) ps.setDouble(3, plant.getHeight());
            else ps.setNull(3, Types.DECIMAL);

            if (plant.getDateAcquired() != null) ps.setDate(4, Date.valueOf(plant.getDateAcquired()));
            else ps.setNull(4, Types.DATE);

            ps.setString(5, plant.getLocationName());
            ps.setInt(6, plant.getPlantId());
            return ps.executeUpdate();
        }
    }

    public int deletePlant(int plantId) throws SQLException {
        String sql = "DELETE FROM Plant WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            return ps.executeUpdate();
        }
    }

    // ------------------------
    // Care CRUD
    // ------------------------
    public int createCare(Care care) throws SQLException {
        String sql = "INSERT INTO Care (Plant_ID, LastSoilChange, LastWatering) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, care.getPlantId());
            if (care.getLastSoilChange() != null) ps.setDate(2, Date.valueOf(care.getLastSoilChange()));
            else ps.setNull(2, Types.DATE);
            if (care.getLastWatering() != null) ps.setDate(3, Date.valueOf(care.getLastWatering()));
            else ps.setNull(3, Types.DATE);
            return ps.executeUpdate();
        }
    }

    public Care getCare(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, LastSoilChange, LastWatering FROM Care WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCare(rs);
                return null;
            }
        }
    }

    public int updateCare(Care care) throws SQLException {
        String sql = "UPDATE Care SET LastSoilChange = ?, LastWatering = ? WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (care.getLastSoilChange() != null) ps.setDate(1, Date.valueOf(care.getLastSoilChange()));
            else ps.setNull(1, Types.DATE);

            if (care.getLastWatering() != null) ps.setDate(2, Date.valueOf(care.getLastWatering()));
            else ps.setNull(2, Types.DATE);

            ps.setInt(3, care.getPlantId());
            return ps.executeUpdate();
        }
    }

    public int deleteCare(int plantId) throws SQLException {
        String sql = "DELETE FROM Care WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            return ps.executeUpdate();
        }
    }

    // ------------------------
    // Information CRUD
    // ------------------------
    public int createInformation(Information info) throws SQLException {
        String sql = "INSERT INTO Information (Plant_ID, FromAnotherPlant, SoilType, PotSize, WaterGlobeRequired) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, info.getPlantId());
            ps.setBoolean(2, info.isFromAnotherPlant());
            ps.setString(3, info.getSoilType());
            ps.setString(4, info.getPotSize());
            ps.setBoolean(5, info.isWaterGlobeRequired());
            return ps.executeUpdate();
        }
    }

    public Information getInformation(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, FromAnotherPlant, SoilType, PotSize, WaterGlobeRequired FROM Information WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapInformation(rs);
                return null;
            }
        }
    }

    public int updateInformation(Information info) throws SQLException {
        String sql = "UPDATE Information SET FromAnotherPlant = ?, SoilType = ?, PotSize = ?, WaterGlobeRequired = ? WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, info.isFromAnotherPlant());
            ps.setString(2, info.getSoilType());
            ps.setString(3, info.getPotSize());
            ps.setBoolean(4, info.isWaterGlobeRequired());
            ps.setInt(5, info.getPlantId());
            return ps.executeUpdate();
        }
    }

    public int deleteInformation(int plantId) throws SQLException {
        String sql = "DELETE FROM Information WHERE Plant_ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            return ps.executeUpdate();
        }
    }

    // ------------------------
    // Location CRUD
    // ------------------------
    public int createLocation(Location location) throws SQLException {
        String sql = "INSERT INTO Location (Plant_ID, location_name, LightLevel) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, location.getPlantId());
            ps.setString(2, location.getLocationName());
            ps.setString(3, location.getLightLevel());
            return ps.executeUpdate();
        }
    }

    public Location getLocation(int plantId, String locationName) throws SQLException {
        String sql = "SELECT Plant_ID, location_name, LightLevel FROM Location WHERE Plant_ID = ? AND location_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            ps.setString(2, locationName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapLocation(rs);
                return null;
            }
        }
    }

    public List<Location> getLocationsForPlant(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, location_name, LightLevel FROM Location WHERE Plant_ID = ?";
        List<Location> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLocation(rs));
            }
        }
        return list;
    }

    public int updateLocation(Location location) throws SQLException {
        String sql = "UPDATE Location SET LightLevel = ? WHERE Plant_ID = ? AND location_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location.getLightLevel());
            ps.setInt(2, location.getPlantId());
            ps.setString(3, location.getLocationName());
            return ps.executeUpdate();
        }
    }

    public int deleteLocation(int plantId, String locationName) throws SQLException {
        String sql = "DELETE FROM Location WHERE Plant_ID = ? AND location_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            ps.setString(2, locationName);
            return ps.executeUpdate();
        }
    }

    // ------------------------
    // Mapping helpers
    // ------------------------
    private Plant mapPlant(ResultSet rs) throws SQLException {
        Plant p = new Plant();
        p.setPlantId(rs.getInt("Plant_ID"));
        p.setName(rs.getString("Name"));
        p.setType(rs.getString("Type"));
        double h = rs.getDouble("Height");
        if (rs.wasNull()) p.setHeight(null);
        else p.setHeight(h);
        Date d = rs.getDate("DateAcquired");
        if (d != null) p.setDateAcquired(d.toLocalDate());
        p.setLocationName(rs.getString("location_name"));
        return p;
    }

    private Care mapCare(ResultSet rs) throws SQLException {
        Care c = new Care();
        c.setPlantId(rs.getInt("Plant_ID"));
        Date s = rs.getDate("LastSoilChange");
        if (s != null) c.setLastSoilChange(s.toLocalDate());
        Date w = rs.getDate("LastWatering");
        if (w != null) c.setLastWatering(w.toLocalDate());
        return c;
    }

    private Information mapInformation(ResultSet rs) throws SQLException {
        Information info = new Information();
        info.setPlantId(rs.getInt("Plant_ID"));
        info.setFromAnotherPlant(rs.getBoolean("FromAnotherPlant"));
        info.setSoilType(rs.getString("SoilType"));
        info.setPotSize(rs.getString("PotSize"));
        info.setWaterGlobeRequired(rs.getBoolean("WaterGlobeRequired"));
        return info;
    }

    private Location mapLocation(ResultSet rs) throws SQLException {
        Location loc = new Location();
        loc.setPlantId(rs.getInt("Plant_ID"));
        loc.setLocationName(rs.getString("location_name"));
        loc.setLightLevel(rs.getString("LightLevel"));
        return loc;
    }
}
