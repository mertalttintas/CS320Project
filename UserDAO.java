import java.sql.*;

public class UserDAO implements IUserDAO {
    @Override
    public User getUserByEmail(String email) throws SQLException {
        User user = null;
        String query = "SELECT * FROM User WHERE Email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.userId = rs.getInt("UserID");
                    user.roleId = rs.getInt("RoleID");
                    user.name = rs.getString("Name");
                    user.email = rs.getString("Email");
                    user.password = rs.getString("Password");
                    user.failedAttempts = rs.getInt("FailedAttempts");
                    user.isLocked = rs.getBoolean("IsLocked");
                }
            }
        }
        return user;
    }
    
    @Override
    public void incrementFailedAttempts(int userId) throws SQLException {
        String query = "UPDATE User SET FailedAttempts = FailedAttempts + 1 WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    @Override
    public void lockAccount(int userId) throws SQLException {
        String query = "UPDATE User SET IsLocked = TRUE WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    @Override
    public void resetFailedAttempts(int userId) throws SQLException {
        String query = "UPDATE User SET FailedAttempts = 0 WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}
