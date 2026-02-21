package com.planttracker.dao;

import com.planttracker.DbUtil;
import com.planttracker.model.Care;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CareDao {

    public CareDao() { }

    // Insert a care record for a plant. Uses Plant_ID as the key column.
    public int insert(Care cObj) throws SQLException {
        String sql = "INSERT INTO Care (Plant_ID, LastSoilChange, LastWatering) VALUES (?, ?, ?)";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, cObj.getPlantId());

            if (cObj.getLastSoilChange() != null)
                ps.setDate(2, Date.valueOf(cObj.getLastSoilChange()));
            else
                ps.setNull(2, Types.DATE);

            if (cObj.getLastWatering() != null)
                ps.setDate(3, Date.valueOf(cObj.getLastWatering()));
            else
                ps.setNull(3, Types.DATE);

            return ps.executeUpdate();
        }
    }

    // Update care by Plant_ID (assumes one care row per plant)
    public int updateByPlantId(Care cObj) throws SQLException {
        String sql = "UPDATE Care SET LastSoilChange = ?, LastWatering = ? WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (cObj.getLastSoilChange() != null)
                ps.setDate(1, Date.valueOf(cObj.getLastSoilChange()));
            else
                ps.setNull(1, Types.DATE);

            if (cObj.getLastWatering() != null)
                ps.setDate(2, Date.valueOf(cObj.getLastWatering()));
            else
                ps.setNull(2, Types.DATE);

            ps.setInt(3, cObj.getPlantId());

            return ps.executeUpdate();
        }
    }

    public int deleteByPlantId(int plantId) throws SQLException {
        String sql = "DELETE FROM Care WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            return ps.executeUpdate();
        }
    }

    public Care findByPlantId(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, LastSoilChange, LastWatering FROM Care WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Care cObj = new Care();
                    cObj.setPlantId(rs.getInt("Plant_ID"));
                    Date d1 = rs.getDate("LastSoilChange");
                    Date d2 = rs.getDate("LastWatering");
                    if (d1 != null) cObj.setLastSoilChange(d1.toLocalDate());
                    if (d2 != null) cObj.setLastWatering(d2.toLocalDate());
                    return cObj;
                }
            }
        }
        return null;
    }

    public List<Care> findAll() throws SQLException {
        String sql = "SELECT Plant_ID, LastSoilChange, LastWatering FROM Care";
        List<Care> out = new ArrayList<>();
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Care cObj = new Care();
                cObj.setPlantId(rs.getInt("Plant_ID"));
                Date d1 = rs.getDate("LastSoilChange");
                Date d2 = rs.getDate("LastWatering");
                if (d1 != null) cObj.setLastSoilChange(d1.toLocalDate());
                if (d2 != null) cObj.setLastWatering(d2.toLocalDate());
                out.add(cObj);
            }
        }
        return out;
    }
}