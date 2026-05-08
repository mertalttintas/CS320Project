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

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTable uTable = new JTable();
        Runnable loadUsers = () -> {
            try (Connection conn = DBConnection.getConnection()) {
                String q = "SELECT UserID, Name, Email, RoleID, IsLocked FROM User";
                PreparedStatement stmt = conn.prepareStatement(q);
                ResultSet rs = stmt.executeQuery();
                String[] cols = {"ID", "Name", "Email", "Role ID", "Is Locked"};
                DefaultTableModel model = new DefaultTableModel(cols, 0);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getBoolean(5)});
                }
                uTable.setModel(model);
            } catch (Exception ex) { showError("Error loading users: " + ex.getMessage()); }
        };
        
        loadUsers.run();
        panel.add(new JScrollPane(uTable), BorderLayout.CENTER);
        
        JPanel bot = new JPanel();
        JButton deleteBtn = new JButton("Delete User");
        JButton toggleLockBtn = new JButton("Lock/Unlock Account");
        JButton refreshBtn = new JButton("Refresh");
        
        bot.add(deleteBtn);
        bot.add(toggleLockBtn);
        bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);
        
        refreshBtn.addActionListener(e -> loadUsers.run());
        deleteBtn.addActionListener(e -> {
            int row = uTable.getSelectedRow();
            if (row == -1) return;
            int uId = (int) uTable.getValueAt(row, 0);
            if (uId == AuthController.currentUser.userId) { showError("Cannot delete yourself."); return; }
            try (Connection conn = DBConnection.getConnection()) {
                conn.prepareStatement("DELETE FROM User WHERE UserID = " + uId).executeUpdate();
                loadUsers.run();
            } catch (Exception ex) { showError("Cannot delete user (Existing data)."); }
        });
        toggleLockBtn.addActionListener(e -> {
            int row = uTable.getSelectedRow();
            if (row == -1) return;
            int uId = (int) uTable.getValueAt(row, 0);
            boolean isLocked = (boolean) uTable.getValueAt(row, 4);
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE User SET IsLocked = ?, FailedAttempts = 0 WHERE UserID = ?");
                stmt.setBoolean(1, !isLocked);
                stmt.setInt(2, uId);
                stmt.executeUpdate();
                loadUsers.run();
            } catch (Exception ex) {}
        });
        
        return panel;
    }
