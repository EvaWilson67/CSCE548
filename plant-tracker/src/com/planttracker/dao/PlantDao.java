package com.planttracker.dao;

import com.planttracker.model.Plant;
import com.planttracker.DbUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PlantDao {
    private final String url, user, pass;

    public PlantDao(String url, String user, String pass) {
        this.url = url; this.user = user; this.pass = pass;
    }

    public int insert(Plant p) throws SQLException {
        String sql = "INSERT INTO Plant (Plant_ID, Name, Type, Height, DateAcquired, location_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getPlantId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getType());
            if (p.getHeight() != null) ps.setDouble(4, p.getHeight()); else ps.setNull(4, Types.DECIMAL);
            if (p.getDateAcquired() != null) ps.setDate(5, Date.valueOf(p.getDateAcquired())); else ps.setNull(5, Types.DATE);
            ps.setString(6, p.getLocationName());
            return ps.executeUpdate();
        }
    }

    public Plant findById(int id) throws SQLException {
        String sql = "SELECT Plant_ID, Name, Type, Height, DateAcquired, location_name FROM Plant WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    public List<Plant> findAll() throws SQLException {
        String sql = "SELECT Plant_ID, Name, Type, Height, DateAcquired, location_name FROM Plant";
        List<Plant> out = new ArrayList<>();
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    public int update(Plant p) throws SQLException {
        String sql = "UPDATE Plant SET Name=?, Type=?, Height=?, DateAcquired=?, location_name=? WHERE Plant_ID=?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getType());
            if (p.getHeight() != null) ps.setDouble(3, p.getHeight()); else ps.setNull(3, Types.DECIMAL);
            if (p.getDateAcquired() != null) ps.setDate(4, Date.valueOf(p.getDateAcquired())); else ps.setNull(4, Types.DATE);
            ps.setString(5, p.getLocationName());
            ps.setInt(6, p.getPlantId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM Plant WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private Plant map(ResultSet rs) throws SQLException {
        Plant p = new Plant();
        p.setPlantId(rs.getInt("Plant_ID"));
        p.setName(rs.getString("Name"));
        p.setType(rs.getString("Type"));
        double h = rs.getDouble("Height");
        if (rs.wasNull()) p.setHeight(null); else p.setHeight(h);
        Date d = rs.getDate("DateAcquired");
        if (d != null) p.setDateAcquired(d.toLocalDate());
        p.setLocationName(rs.getString("location_name"));
        return p;
    }
}
