import java.sql.Date;
import java.sql.SQLException;

public class ReservationManager implements IBusinessLogic {
    private IReservationDAO reservationDAO;

    public ReservationManager() {
        this.reservationDAO = new ReservationDAO();
    }

    public boolean checkConflict(int vehicleId, Date start, Date end) throws SQLException {
        return reservationDAO.hasConflict(vehicleId, start, end);
    }

    public double calculateTotal(double basePrice, int days) {
        double insuranceFee = 50.0;
        double taxes = 0.18; 
        double subtotal = (basePrice * days) + insuranceFee;
        return subtotal + (subtotal * taxes);
    }

    public boolean confirmBooking(Reservation res) throws Exception {
        if (checkConflict(res.vehicleId, res.pickupDate, res.returnDate)) {
            throw new Exception("Vehicle Unavailable for the selected dates. Double-booking detected!");
        }
        return reservationDAO.saveReservation(res);
    }
}
