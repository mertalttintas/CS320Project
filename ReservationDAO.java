import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO implements IReservationDAO {

    @Override
    public boolean hasConflict(int vehicleId, Date startDate, Date endDate) throws SQLException {
        // SDD rule: (Requested_Start <= Existing_End) AND (Requested_End >= Existing_Start)
        String query = "SELECT COUNT(*) FROM Reservation WHERE VehicleID = ? AND Status IN ('Pending', 'Approved') AND (? <= ReturnDate AND ? >= PickupDate)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vehicleId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean saveReservation(Reservation res) throws SQLException {
        String query = "INSERT INTO Reservation (CustomerID, VehicleID, PickupDate, ReturnDate, TotalPrice, Status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            // ACID compliance (transaction) can be managed here or at the connection level
            conn.setAutoCommit(false);
            try {
                stmt.setInt(1, res.customerId);
                stmt.setInt(2, res.vehicleId);
                stmt.setDate(3, res.pickupDate);
                stmt.setDate(4, res.returnDate);
                stmt.setDouble(5, res.totalPrice);
                stmt.setString(6, res.status);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            res.reservationId = generatedKeys.getInt(1);
                        }
                    }
                }
                
                // Update vehicle status to rented or handle stock if needed
                conn.commit();
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<Reservation> getReservationsByCustomer(int customerId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String query = "SELECT * FROM Reservation WHERE CustomerID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation r = new Reservation();
                    r.reservationId = rs.getInt("ReservationID");
                    r.customerId = rs.getInt("CustomerID");
                    r.vehicleId = rs.getInt("VehicleID");
                    r.pickupDate = rs.getDate("PickupDate");
                    r.returnDate = rs.getDate("ReturnDate");
                    r.totalPrice = rs.getDouble("TotalPrice");
                    r.status = rs.getString("Status");
                    list.add(r);
                }
            }
        }
        return list;
    }
}
