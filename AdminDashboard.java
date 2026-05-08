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

     private JPanel createVehicleManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTable vTable = new JTable();
        Runnable loadAllVehicles = () -> {
            try (Connection conn = DBConnection.getConnection()) {
                String q = "SELECT VehicleID, Brand, Model, Category, DailyPrice, Status FROM Vehicle";
                PreparedStatement stmt = conn.prepareStatement(q);
                ResultSet rs = stmt.executeQuery();
                String[] cols = {"ID", "Brand", "Model", "Type", "Price", "Status"};
                DefaultTableModel model = new DefaultTableModel(cols, 0);
                while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getString(6)});
                vTable.setModel(model);
            } catch (Exception ex) {}
        };
        
        loadAllVehicles.run();
        panel.add(new JScrollPane(vTable), BorderLayout.CENTER);
        
        JPanel bot = new JPanel();
        JButton removeBtn = new JButton("Remove Listing");
        JButton refreshBtn = new JButton("Refresh");
        bot.add(removeBtn);
        bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);
        
        refreshBtn.addActionListener(e -> loadAllVehicles.run());
        removeBtn.addActionListener(e -> {
            int row = vTable.getSelectedRow();
            if (row == -1) return;
            try (Connection conn = DBConnection.getConnection()) {
                conn.prepareStatement("DELETE FROM Vehicle WHERE VehicleID = " + (int)vTable.getValueAt(row, 0)).executeUpdate();
                loadAllVehicles.run();
            } catch (Exception ex) { showError("Cannot remove vehicle (Active reservations)."); }
        });
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20)); 
        gridPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        Runnable loadStats = () -> {
            gridPanel.removeAll();
            try {
                Map<String, String> stats = adminController.getDashboardStats();
                gridPanel.add(createStatCard("Total Revenue", stats.get("TotalRevenue"), new Color(230, 126, 34))); // Orange
                gridPanel.add(createStatCard("Most Rented Brand", stats.get("TopBrand"), new Color(46, 204, 113))); // Green
                gridPanel.add(createStatCard("Total Users", stats.get("TotalUsers"), new Color(52, 152, 219))); // Blue
                gridPanel.add(createStatCard("Pending Reservations", stats.get("PendingRes"), new Color(155, 89, 182))); // Purple
            } catch (Exception ex) {
                gridPanel.add(new JLabel("Failed to load statistics."));
            }
            gridPanel.revalidate();
            gridPanel.repaint();
        };
        
        JButton refreshBtn = new JButton("Refresh Statistics");
        JButton exportBtn = new JButton("Export Report (TXT)");
        refreshBtn.addActionListener(e -> loadStats.run());
        exportBtn.addActionListener(e -> exportReport());
        
        JPanel bot = new JPanel(); 
        bot.add(refreshBtn);
        bot.add(exportBtn);
        
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(bot, BorderLayout.SOUTH);
        loadStats.run();
        return mainPanel;
    }
    
    private void exportReport() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter("System_Report.txt")) {
            Map<String, String> stats = adminController.getDashboardStats();
            writer.println("--- CRS SYSTEM REPORT ---");
            writer.println("Generated on: " + new java.util.Date());
            writer.println("Total Revenue: " + stats.get("TotalRevenue"));
            writer.println("Top Brand: " + stats.get("TopBrand"));
            writer.println("Total Users: " + stats.get("TotalUsers"));
            writer.println("Pending Reservations: " + stats.get("PendingRes"));
            writer.println("-------------------------");
            JOptionPane.showMessageDialog(this, "Report exported to System_Report.txt");
        } catch (Exception ex) { showError("Export failed: " + ex.getMessage()); }
    }
    
    private JPanel createStatCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);
        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(Color.WHITE);
        card.add(titleLbl);
        card.add(valLbl);
        return card;
    }
    
    @Override public void displayData(Object data) {}
    @Override public void showError(String errorMessage) { JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE); }
    @Override public String getInput(String prompt) { return ""; }
}
