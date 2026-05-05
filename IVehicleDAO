import java.sql.SQLException;
import java.util.List;

public interface IVehicleDAO {
    void addVehicle(Vehicle v) throws SQLException;
    void updateVehicleStatus(int vehicleId, String status) throws SQLException;
    List<Vehicle> getAllVehicles() throws SQLException;
    List<Vehicle> searchVehicles(String location, String fuel, String trans, String category, double maxPrice) throws SQLException;
}
