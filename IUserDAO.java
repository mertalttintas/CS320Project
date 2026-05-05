import java.sql.SQLException;

public interface IUserDAO {
    User getUserByEmail(String email) throws SQLException;
    void incrementFailedAttempts(int userId) throws SQLException;
    void lockAccount(int userId) throws SQLException;
    void resetFailedAttempts(int userId) throws SQLException;
}
