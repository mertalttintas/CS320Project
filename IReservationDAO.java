import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface IReservationDAO {
    boolean hasConflict(int vehicleId, Date startDate, Date endDate) throws SQLException;
    boolean saveReservation(Reservation res) throws SQLException;
    List<Reservation> getReservationsByCustomer(int customerId) throws SQLException;
}
