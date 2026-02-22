package com.planttracker.dao;

import com.planttracker.model.Plant;
import com.planttracker.DbUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PlantDao {

    public PlantDao() {
    }

    // inside src/main/java/com/planttracker/dao/PlantDao.java
    public int insert(Plant p) throws SQLException {
        // use lowercase unquoted identifiers to match Postgres table/column names
        String sql = "INSERT INTO plant (name, type, height, dateacquired, location_name) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING plant_id";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getType());

            // Height column DECIMAL -> use NUMERIC type when setting nulls
            if (p.getHeight() != null) {
                ps.setDouble(3, p.getHeight());
            } else {
                ps.setNull(3, Types.NUMERIC);
            }

            if (p.getDateAcquired() != null) {
                ps.setDate(4, Date.valueOf(p.getDateAcquired()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setString(5, p.getLocationName());

            // RETURNING produces a ResultSet; executeQuery() must be used
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // use column name in lowercase as returned by query
                    int newId = rs.getInt(1); // or rs.getInt("plant_id");
                    // set back on POJO - adapt setter name to your Plant model
                    // e.g. p.setId(newId) or p.setPlantId(newId)
                    p.setPlantId(newId); // replace with the actual setter name in your Plant class
                    return newId;
                } else {
                    return 0; // no id returned -> insert didn't happen
                }
            }
        }
    }

    public int update(Plant p) throws SQLException {
        String sql = "UPDATE Plant SET Name = ?, Type = ?, Height = ?, DateAcquired = ?, location_name = ? WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getType());

            if (p.getHeight() != null)
                ps.setDouble(3, p.getHeight());
            else
                ps.setNull(3, Types.DOUBLE);

            if (p.getDateAcquired() != null)
                ps.setDate(4, Date.valueOf(p.getDateAcquired()));
            else
                ps.setNull(4, Types.DATE);

            ps.setString(5, p.getLocationName());
            ps.setInt(6, p.getPlantId());

            return ps.executeUpdate();
        }
    }

    public int delete(int plantId) throws SQLException {
        String sql = "DELETE FROM Plant WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            return ps.executeUpdate();
        }
    }

    public Plant findById(int plantId) throws SQLException {
        String sql = "SELECT Plant_ID, Name, Type, Height, DateAcquired, location_name FROM Plant WHERE Plant_ID = ?";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Plant p = new Plant();
                    p.setPlantId(rs.getInt("Plant_ID"));
                    p.setName(rs.getString("Name"));
                    p.setType(rs.getString("Type"));

                    double h = rs.getDouble("Height");
                    if (!rs.wasNull())
                        p.setHeight(h);

                    Date da = rs.getDate("DateAcquired");
                    if (da != null)
                        p.setDateAcquired(da.toLocalDate());

                    p.setLocationName(rs.getString("location_name"));
                    return p;
                }
            }
        }
        return null;
    }

    public List<Plant> findAll() throws SQLException {
        String sql = "SELECT Plant_ID, Name, Type, Height, DateAcquired, location_name FROM Plant";
        List<Plant> out = new ArrayList<>();
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Plant p = new Plant();
                p.setPlantId(rs.getInt("Plant_ID"));
                p.setName(rs.getString("Name"));
                p.setType(rs.getString("Type"));

                double h = rs.getDouble("Height");
                if (!rs.wasNull())
                    p.setHeight(h);

                Date da = rs.getDate("DateAcquired");
                if (da != null)
                    p.setDateAcquired(da.toLocalDate());

                p.setLocationName(rs.getString("location_name"));
                out.add(p);
            }
        }
        return out;
    }

    /**
     * Optional helper if you still need max ID logic (not required if DB uses
     * auto-increment).
     */
    public int getMaxPlantId() throws SQLException {
        String sql = "SELECT MAX(Plant_ID) AS mx FROM Plant";
        try (Connection c = DbUtil.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int mx = rs.getInt("mx");
                if (rs.wasNull())
                    return 0;
                return mx;
            } else {
                return 0;
            }
        }
    }
}