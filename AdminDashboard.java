import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class AdminDashboard extends JFrame implements IUserInterface {
    private JTable userTable;
    private AdminController adminController;
    
    public AdminDashboard() {
        adminController = new AdminController();
        setTitle("CRS - Admin Control Panel");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel userInfo = new JLabel("Role: Administrator  |  User: " + AuthController.currentUser.name);
        userInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.addActionListener(e -> {
            AuthController.currentUser = null;
            new LoginScreen();
            this.dispose();
        });
        
        topBar.add(userInfo, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.add("Dashboard Reports", createReportsPanel());
        tabs.add("Manage Users", createUserManagementPanel());
        tabs.add("Manage All Vehicles", createVehicleManagementPanel());
        
        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }
