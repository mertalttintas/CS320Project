import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FleetManagerDashboard extends JFrame implements IUserInterface {
    private JTable requestsTable, vehiclesTable;
    
    public FleetManagerDashboard() {
        setTitle("CRS - Fleet Manager Dashboard");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Üst Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel userInfo = new JLabel("Role: Fleet Manager  |  User: " + AuthController.currentUser.name);
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
        tabs.add("Incoming Requests", createRequestsPanel());
        tabs.add("Manage Fleet", createFleetPanel());
        tabs.add("My Earnings", createEarningsPanel());
        
        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }
    
    private JPanel createRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        requestsTable = new JTable();
        requestsTable.setRowHeight(25);
        requestsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(new JScrollPane(requestsTable), BorderLayout.CENTER);
        
        JPanel bot = new JPanel();
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        JButton completeBtn = new JButton("Mark Completed");
        JButton refreshBtn = new JButton("Refresh");
        
        bot.add(approveBtn);
        bot.add(rejectBtn);
        bot.add(completeBtn);
        bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);
        
        refreshBtn.addActionListener(e -> loadRequests());
        approveBtn.addActionListener(e -> updateStatus("Approved"));
        rejectBtn.addActionListener(e -> updateStatus("Rejected"));
        completeBtn.addActionListener(e -> updateStatus("Completed"));
        
        loadRequests();
        return panel;
    }
    
    private JPanel createFleetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        vehiclesTable = new JTable();
        vehiclesTable.setRowHeight(25);
        vehiclesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(new JScrollPane(vehiclesTable), BorderLayout.CENTER);
        
        JPanel bot = new JPanel();
        JButton addVehicleBtn = new JButton("+ Add Vehicle");
        JButton editBtn = new JButton("Edit Vehicle");
        JButton deleteBtn = new JButton("Delete Vehicle");
        JButton toggleStatusBtn = new JButton("Toggle Availability");
        JButton refreshBtn = new JButton("Refresh Fleet");
        bot.add(addVehicleBtn);
        bot.add(editBtn);
        bot.add(deleteBtn);
        bot.add(toggleStatusBtn);
        bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);
        
        refreshBtn.addActionListener(e -> loadVehicles());
        addVehicleBtn.addActionListener(e -> addNewVehicle());
        editBtn.addActionListener(e -> editSelectedVehicle());
        deleteBtn.addActionListener(e -> deleteSelectedVehicle());
        toggleStatusBtn.addActionListener(e -> toggleVehicleStatus());
        
        loadVehicles();
        return panel;
    }
    
