import java.sql.SQLException;

public class FleetController implements IBusinessLogic {
    private IVehicleDAO vehicleDAO;

    public FleetController() {
        this.vehicleDAO = new VehicleDAO();
    }

    public void addVehicle(Vehicle v) throws SQLException {
        // Business rules could be checked here (e.g., negative price check)
        if (v.dailyPrice < 0 || v.seats <= 0) {
            throw new IllegalArgumentException("Invalid vehicle parameters");
        }
        vehicleDAO.addVehicle(v);
    }

    public void changeVehicleStatus(int vId, String status) throws SQLException {
        vehicleDAO.updateVehicleStatus(vId, status);
    }
}
