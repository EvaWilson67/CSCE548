package com.planttracker.dao;

import com.planttracker.model.Care;
import com.planttracker.DbUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CareDao {
    private final String url, user, pass;

    public CareDao(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public int insert(Care cObj) throws SQLException {
        String sql = "INSERT INTO Care (Plant_ID, LastSoilChange, LastWatering) VALUES (?, ?, ?)";
        try (Connection c = DbUtil.getConnection(url, user, pass);
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

    public Care findByPlantId(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, LastSoilChange, LastWatering FROM Care WHERE Plant_ID = ?";
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

    public int update(Care cObj) throws SQLException {
        String sql = "UPDATE Care SET LastSoilChange=?, LastWatering=? WHERE Plant_ID=?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
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

    public int delete(int plantId) throws SQLException {
        String sql = "DELETE FROM Care WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection(url, user, pass);
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            return ps.executeUpdate();
        }
    }

    private Care map(ResultSet rs) throws SQLException {
        Care c = new Care();
        c.setPlantId(rs.getInt("Plant_ID"));
        Date s = rs.getDate("LastSoilChange");
        if (s != null)
            c.setLastSoilChange(s.toLocalDate());
        Date w = rs.getDate("LastWatering");
        if (w != null)
            c.setLastWatering(w.toLocalDate());
        return c;
    }

    public List<Care> findAll() throws SQLException {
        List<Care> out = new ArrayList<>();
        String sql = "SELECT Plant_ID, LastSoilChange, LastWatering FROM Care";
        try (Connection c = DbUtil.getConnection(url, user, pass);
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs)); // assumes private Care map(ResultSet) exists
            }
        }
        return out;
    }
}
