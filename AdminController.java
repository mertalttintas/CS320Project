import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AdminController implements IBusinessLogic {
    
    public Map<String, String> getDashboardStats() throws SQLException {
        Map<String, String> stats = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            
            // 1. Total Revenue
            ResultSet rs = conn.prepareStatement("SELECT SUM(TotalPrice) FROM Reservation WHERE Status IN ('Completed', 'Approved')").executeQuery();
            if (rs.next() && rs.getString(1) != null) stats.put("TotalRevenue", "$" + String.format("%.2f", rs.getDouble(1)));
            else stats.put("TotalRevenue", "$0.00");
            
            // 2. Most Rented Brand
            ResultSet rs2 = conn.prepareStatement("SELECT v.Brand, COUNT(r.ReservationID) as c FROM Reservation r JOIN Vehicle v ON r.VehicleID = v.VehicleID GROUP BY v.Brand ORDER BY c DESC LIMIT 1").executeQuery();
            if (rs2.next()) stats.put("TopBrand", rs2.getString(1) + " (" + rs2.getInt(2) + " times)");
            else stats.put("TopBrand", "N/A");
