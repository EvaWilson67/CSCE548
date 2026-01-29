package com.planttracker.dao;

import com.planttracker.DbUtil;
import com.planttracker.model.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDao {
    private final String url, user, pass;

    public LocationDao(String url, String user, String pass) {
        this.url = url; this.user = user; this.pass = pass;
    }

    public int insert(Location loc) throws SQLException {
        String sql = "INSERT INTO Location (Plant_ID, location_name, LightLevel) VALUES (?, ?, ?)";
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, loc.getPlantId());
            ps.setString(2, loc.getLocationName());
            ps.setString(3, loc.getLightLevel());
            return ps.executeUpdate();
        }
    }

    public Location find(int plantId, String locationName) throws SQLException {
        String sql = "SELECT Plant_ID, location_name, LightLevel FROM Location WHERE Plant_ID = ? AND location_name = ?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            ps.setString(2, locationName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    public List<Location> findForPlant(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, location_name, LightLevel FROM Location WHERE Plant_ID = ?";
        List<Location> out = new ArrayList<>();
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public int update(Location loc) throws SQLException {
        String sql = "UPDATE Location SET LightLevel = ? WHERE Plant_ID = ? AND location_name = ?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, loc.getLightLevel());
            ps.setInt(2, loc.getPlantId());
            ps.setString(3, loc.getLocationName());
            return ps.executeUpdate();
        }
    }

    public int delete(int plantId, String locationName) throws SQLException {
        String sql = "DELETE FROM Location WHERE Plant_ID = ? AND location_name = ?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            ps.setString(2, locationName);
            return ps.executeUpdate();
        }
    }

    private Location map(ResultSet rs) throws SQLException {
        Location l = new Location();
        l.setPlantId(rs.getInt("Plant_ID"));
        l.setLocationName(rs.getString("location_name"));
        l.setLightLevel(rs.getString("LightLevel"));
        return l;
    }
}
