import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class LoginScreen extends JFrame implements IUserInterface {
    private JTextField emailField;
    private JPasswordField passwordField;
    private AuthController authController;

    public LoginScreen() {
        authController = new AuthController();
        setTitle("Car Rental System");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(230, 126, 34)); // Orange
        headerPanel.setPreferredSize(new Dimension(450, 80));
        JLabel welcomeLabel = new JLabel("Welcome to CRS");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(welcomeLabel);

        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.gridx = 0;

        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton forgotBtn = new JButton("Forgot Password?");
        forgotBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setForeground(Color.GRAY);

        gbc.gridy = 0; formPanel.add(new JLabel("Email Address:"), gbc);
        gbc.gridy = 1; formPanel.add(emailField, gbc);
        gbc.gridy = 2; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridy = 3; formPanel.add(passwordField, gbc);
        gbc.gridy = 4; gbc.insets = new java.awt.Insets(20, 5, 5, 5); formPanel.add(loginButton, gbc);
        gbc.gridy = 5; gbc.insets = new java.awt.Insets(5, 5, 5, 5); formPanel.add(registerButton, gbc);
        gbc.gridy = 6; formPanel.add(forgotBtn, gbc);

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> {
            new RegisterScreen();
            this.dispose();
        });
        forgotBtn.addActionListener(e -> handleForgotPassword());

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void handleForgotPassword() {
        String email = getInput("Enter your registered email:");
        if (email == null || email.isEmpty()) return;
        String newPass = getInput("Enter NEW password:");
        if (newPass == null || newPass.isEmpty()) return;
        
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE User SET Password = ? WHERE Email = ?");
            stmt.setString(1, PasswordHasher.hashPassword(newPass));
            stmt.setString(2, email);
            int rows = stmt.executeUpdate();
            if (rows > 0) JOptionPane.showMessageDialog(this, "Password reset successfully!");
            else showError("Email not found.");
        } catch (Exception ex) { showError(ex.getMessage()); }
    }
    
    private void attemptLogin() {
        try {
            boolean success = authController.login(emailField.getText(), new String(passwordField.getPassword()));
            if (success) {
                this.dispose();
                // Route to appropriate dashboard based on role
                if (AuthController.currentUser.roleId == 1) {
                    new AdminDashboard();
                } else if (AuthController.currentUser.roleId == 2) {
                    new FleetManagerDashboard();
                } else if (AuthController.currentUser.roleId == 3) {
                    new CustomerDashboard();
                }
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @Override
    public void displayData(Object data) {
    }

    @Override
    public void showError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getInput(String prompt) {
        return JOptionPane.showInputDialog(this, prompt);
    }


    
}
