package com.planttracker.dao;

import com.planttracker.DbUtil;
import com.planttracker.model.Information;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InformationDao {
    private final String url, user, pass;

    public InformationDao(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public int insert(Information info) throws SQLException {
        String sql = "INSERT INTO Information (Plant_ID, FromAnotherPlant, SoilType, PotSize, WaterGlobeRequired) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DbUtil.getConnection(url, user, pass);
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, info.getPlantId());
            ps.setBoolean(2, info.isFromAnotherPlant());
            ps.setString(3, info.getSoilType());
            ps.setString(4, info.getPotSize());
            ps.setBoolean(5, info.isWaterGlobeRequired());
            return ps.executeUpdate();
        }
    }

    public Information findByPlantId(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, FromAnotherPlant, SoilType, PotSize, WaterGlobeRequired FROM Information WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
                return null;
            }
        }
    }

    public int update(Information info) throws SQLException {
        String sql = "UPDATE Information SET FromAnotherPlant=?, SoilType=?, PotSize=?, WaterGlobeRequired=? WHERE Plant_ID=?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, info.isFromAnotherPlant());
            ps.setString(2, info.getSoilType());
            ps.setString(3, info.getPotSize());
            ps.setBoolean(4, info.isWaterGlobeRequired());
            ps.setInt(5, info.getPlantId());
            return ps.executeUpdate();
        }
    }

    public int delete(int plantId) throws SQLException {
        String sql = "DELETE FROM Information WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            return ps.executeUpdate();
        }
    }

    private Information map(ResultSet rs) throws SQLException {
        Information info = new Information();
        info.setPlantId(rs.getInt("Plant_ID"));
        info.setFromAnotherPlant(rs.getBoolean("FromAnotherPlant"));
        info.setSoilType(rs.getString("SoilType"));
        info.setPotSize(rs.getString("PotSize"));
        info.setWaterGlobeRequired(rs.getBoolean("WaterGlobeRequired"));
        return info;
    }

    public List<Information> findAll() throws SQLException {
        List<Information> out = new ArrayList<>();
        String sql = "SELECT Plant_ID, FromAnotherPlant, SoilType, PotSize, WaterGlobeRequired FROM Information";
        try (Connection c = DbUtil.getConnection(url, user, pass);
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs)); // assumes private Information map(ResultSet) exists
            }
        }
        return out;
    }
}
