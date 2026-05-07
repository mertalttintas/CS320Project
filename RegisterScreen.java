import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterScreen extends JFrame implements IUserInterface {
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public RegisterScreen() {
        setTitle("CRS - Register");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        nameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        String[] roles = {"Customer", "Fleet Manager"};
        roleBox = new JComboBox<>(roles);

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Account Type:"));
        panel.add(roleBox);
        
        JPanel btnPanel = new JPanel();
        JButton regBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");
        
        regBtn.addActionListener(e -> attemptRegister());
        backBtn.addActionListener(e -> {
            new LoginScreen();
            this.dispose();
        });
        
        btnPanel.add(regBtn);
        btnPanel.add(backBtn);
        
        add(panel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void attemptRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String pass = new String(passwordField.getPassword());
        int roleId = roleBox.getSelectedIndex() == 0 ? 3 : 2; // 3=Customer, 2=FleetManager
        
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showError("Please fill all fields.");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO User (RoleID, Name, Email, Password) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, roleId);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, PasswordHasher.hashPassword(pass)); 
            stmt.executeUpdate();
            
            // Generate Role specific profile
            java.sql.ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int newUserId = rs.getInt(1);
                if (roleId == 3) {
                    conn.prepareStatement("INSERT INTO Customer (UserID) VALUES (" + newUserId + ")").executeUpdate();
                } else {
                    conn.prepareStatement("INSERT INTO FleetManager (UserID, CompanyName) VALUES (" + newUserId + ", '" + name + " Company')").executeUpdate();
                }
            }
            
            JOptionPane.showMessageDialog(this, "Registration successful! You can now login.");
            new LoginScreen();
            this.dispose();
        } catch (Exception ex) {
            showError("Registration failed. Email might already be in use.");
        }
    }

    @Override public void displayData(Object data) {}
    @Override public void showError(String errorMessage) { JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE); }
    @Override public String getInput(String prompt) { return ""; }
}
