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
        if (checkPassword(password, user.password)) {
            userDAO.resetFailedAttempts(user.userId);
            currentUser = user;
            return true;
        } else {
            userDAO.incrementFailedAttempts(user.userId);
            // Re-fetch user to check updated failed attempts count
            User updatedUser = userDAO.getUserByEmail(email);
            if (updatedUser.failedAttempts >= 3) {
                userDAO.lockAccount(user.userId);
                throw new Exception("Account locked after 3 failed attempts.");
            }
            throw new Exception("Invalid email or password. Attempt " + updatedUser.failedAttempts + " of 3.");
        }
    }
    //Dear friends add remaining codes here

}
