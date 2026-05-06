import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO implements IVehicleDAO {
    @Override
    public void addVehicle(Vehicle v) throws SQLException {
        String query = "INSERT INTO Vehicle (ManagerID, Brand, Model, Location, FuelType, Transmission, Seats, DailyPrice, StockQuantity, Category, Status, Features) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, v.managerId);
            stmt.setString(2, v.brand);
            stmt.setString(3, v.model);
            stmt.setString(4, v.location);
            stmt.setString(5, v.fuelType);
            stmt.setString(6, v.transmission);
            stmt.setInt(7, v.seats);
            stmt.setDouble(8, v.dailyPrice);
            stmt.setInt(9, v.stockQuantity);
            stmt.setString(10, v.category);
            stmt.setString(11, v.status);
            stmt.setString(12, v.features);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateVehicleStatus(int vehicleId, String status) throws SQLException {
        String query = "UPDATE Vehicle SET Status = ? WHERE VehicleID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, vehicleId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String query = "SELECT * FROM Vehicle";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToVehicle(rs));
            }
        }
        return list;
    }

    @Override
    public List<Vehicle> searchVehicles(String location, String fuel, String trans, String category, double maxPrice) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM Vehicle WHERE Status = 'Available'");
        
        if (location != null && !location.trim().isEmpty()) query.append(" AND Location LIKE ?");
        if (fuel != null && !fuel.equals("Any")) query.append(" AND FuelType = ?");
        if (trans != null && !trans.equals("Any")) query.append(" AND Transmission = ?");
        if (category != null && !category.equals("Any")) query.append(" AND Category = ?");
        if (maxPrice > 0) query.append(" AND DailyPrice <= ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            int paramIdx = 1;
            if (location != null && !location.trim().isEmpty()) stmt.setString(paramIdx++, "%" + location + "%");
            if (fuel != null && !fuel.equals("Any")) stmt.setString(paramIdx++, fuel);
            if (trans != null && !trans.equals("Any")) stmt.setString(paramIdx++, trans);
            if (category != null && !category.equals("Any")) stmt.setString(paramIdx++, category);
            if (maxPrice > 0) stmt.setDouble(paramIdx++, maxPrice);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVehicle(rs));
                }
            }
        }
        return list;
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.vehicleId = rs.getInt("VehicleID");
        v.managerId = rs.getInt("ManagerID");
        v.brand = rs.getString("Brand");
        v.model = rs.getString("Model");
        v.location = rs.getString("Location");
        v.fuelType = rs.getString("FuelType");
        v.transmission = rs.getString("Transmission");
        v.seats = rs.getInt("Seats");
        v.dailyPrice = rs.getDouble("DailyPrice");
        v.stockQuantity = rs.getInt("StockQuantity");
        v.category = rs.getString("Category");
        v.status = rs.getString("Status");
        v.features = rs.getString("Features");
        return v;
    }
}
