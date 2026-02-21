package com.planttracker.dao;

import com.planttracker.DbUtil;
import com.planttracker.model.Information;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InformationDao {
    public InformationDao() {
        // no url/user/pass needed if DbUtil.getConnection() is the no-arg version
    }

    // Insert using fields that actually exist on Information.java
    public int insert(Information info) throws SQLException {
        String sql = "INSERT INTO Information (Plant_ID, FromAnotherPlant, SoilType, PotSize, WaterGlobeRequired) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Use the model's getters (not getNotes())
            ps.setInt(1, info.getPlantId());
            ps.setBoolean(2, info.isFromAnotherPlant());
            ps.setString(3, info.getSoilType());
            ps.setString(4, info.getPotSize());
            ps.setBoolean(5, info.isWaterGlobeRequired());

            int affected = ps.executeUpdate();
            // if you have an auto-generated key and an id field in your model, set it here:
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    // if your model had an id field, you'd call something like
                    // info.setId(rs.getInt(1));
                }
            }
            return affected;
        }
    }

    public int update(Information info) throws SQLException {
        // Update expects you have a primary key column to address row; adjust column
        // names as needed.
        // If you don't have an ID column in Information, you'll need to choose an
        // appropriate WHERE clause.
        String sql = "UPDATE Information SET FromAnotherPlant = ?, SoilType = ?, PotSize = ?, WaterGlobeRequired = ? WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBoolean(1, info.isFromAnotherPlant());
            ps.setString(2, info.getSoilType());
            ps.setString(3, info.getPotSize());
            ps.setBoolean(4, info.isWaterGlobeRequired());
            ps.setInt(5, info.getPlantId());

            return ps.executeUpdate();
        }
    }

    public int deleteByPlantId(int plantId) throws SQLException {
        String sql = "DELETE FROM Information WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, plantId);
            return ps.executeUpdate();
        }
    }

    public Information findByPlantId(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, FromAnotherPlant, SoilType, PotSize, WaterGlobeRequired FROM Information WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, plantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public List<Information> findAll() throws SQLException {
        List<Information> out = new ArrayList<>();
        String sql = "SELECT Plant_ID, FromAnotherPlant, SoilType, PotSize, WaterGlobeRequired FROM Information";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }
        }
        return out;
    }

    // Map resultset columns to your Information model fields
    private Information map(ResultSet rs) throws SQLException {
        Information info = new Information();
        info.setPlantId(rs.getInt("Plant_ID"));
        info.setFromAnotherPlant(rs.getBoolean("FromAnotherPlant"));
        info.setSoilType(rs.getString("SoilType"));
        info.setPotSize(rs.getString("PotSize"));
        info.setWaterGlobeRequired(rs.getBoolean("WaterGlobeRequired"));
        return info;
    }

    public int updateByPlantId(Information info) throws SQLException {
        String sql = "UPDATE Information SET FromAnotherPlant = ?, SoilType = ?, PotSize = ?, WaterGlobeRequired = ? WHERE Plant_ID = ?";

        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBoolean(1, info.isFromAnotherPlant());
            ps.setString(2, info.getSoilType());
            ps.setString(3, info.getPotSize());
            ps.setBoolean(4, info.isWaterGlobeRequired());
            ps.setInt(5, info.getPlantId());

            return ps.executeUpdate();
        }
    }
}