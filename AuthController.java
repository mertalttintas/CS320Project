import java.sql.SQLException;

public class AuthController implements IBusinessLogic {
    private IUserDAO userDAO;
    public static User currentUser;

    public AuthController() {
        this.userDAO = new UserDAO();
    }

    public boolean login(String email, String password) throws Exception {
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            throw new Exception("Invalid email or password.");
        }
        if (user.isLocked) {
            throw new Exception("Account is locked due to 3 consecutive failed attempts.");
        }
    }
}
