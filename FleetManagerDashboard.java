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

    private void addNewVehicle() {
        try {
            String brand = getInput("Enter Brand:");
            if (brand == null || brand.trim().isEmpty()) return;
            String model = getInput("Enter Model:");
            if (model == null || model.trim().isEmpty()) return;
            String loc = getInput("Enter Location:");
            if (loc == null || loc.trim().isEmpty()) return;
            
            String[] fuelOpts = {"Gasoline", "Diesel", "Electric", "Hybrid"};
            String fuel = (String) JOptionPane.showInputDialog(this, "Select Fuel Type:", "Fuel", JOptionPane.QUESTION_MESSAGE, null, fuelOpts, fuelOpts[0]);
            if (fuel == null) return;
            
            String[] transOpts = {"Automatic", "Manual"};
            String trans = (String) JOptionPane.showInputDialog(this, "Select Transmission:", "Transmission", JOptionPane.QUESTION_MESSAGE, null, transOpts, transOpts[0]);
            if (trans == null) return;

            String[] catOpts = {"Sedan", "SUV", "Hatchback", "Luxury", "Economy"};
            String cat = (String) JOptionPane.showInputDialog(this, "Select Vehicle Type:", "Category", JOptionPane.QUESTION_MESSAGE, null, catOpts, catOpts[0]);
            if (cat == null) return;
            
            String priceStr = getInput("Enter Daily Price:");
            if (priceStr == null || priceStr.trim().isEmpty()) return;
            
            String seatStr = getInput("Enter Number of Seats (e.g. 5):");
            if (seatStr == null || seatStr.trim().isEmpty()) seatStr = "5";
            
            String features = getInput("Enter Features (e.g. GPS, Sunroof):");
            if (features == null) features = "Standard";
            
            double price = Double.parseDouble(priceStr);
            int seats = Integer.parseInt(seatStr);
            
            if (price <= 0) {
                showError("Daily price must be a positive number.");
                return;
            }
            
            Vehicle v = new Vehicle();
            v.managerId = getManagerId();
            v.brand = brand;
            v.model = model;
            v.location = loc;
            v.fuelType = fuel;
            v.transmission = trans;
            v.seats = seats;
            v.category = cat;
            v.dailyPrice = price;
            v.stockQuantity = 1;
            v.status = "Available";
            v.features = features;
            
            new FleetController().addVehicle(v);
            JOptionPane.showMessageDialog(this, "Vehicle added!");
            loadVehicles();
        } catch (NumberFormatException ex) {
            showError("Price must be a valid number!");
        } catch (Exception ex) { 
            showError("Failed to add vehicle: " + ex.getMessage()); 
        }
    }
    
    private int getManagerId() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT ManagerID FROM FleetManager WHERE UserID = ?");
            stmt.setInt(1, AuthController.currentUser.userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            throw new Exception("Manager not found.");
        }
    }
    private void editSelectedVehicle() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { showError("Select a vehicle to edit."); return; }
        int vId = (int) vehiclesTable.getValueAt(row, 0);
        
        try {
            String brand = getInput("New Brand (current: " + vehiclesTable.getValueAt(row, 1) + "):");
            if (brand == null || brand.trim().isEmpty()) return;
            String model = getInput("New Model (current: " + vehiclesTable.getValueAt(row, 2) + "):");
            if (model == null || model.trim().isEmpty()) return;
            
            String[] fuelOpts = {"Gasoline", "Diesel", "Electric", "Hybrid"};
            String fuel = (String) JOptionPane.showInputDialog(this, "Select Fuel Type:", "Edit Fuel", JOptionPane.QUESTION_MESSAGE, null, fuelOpts, vehiclesTable.getValueAt(row, 6));
            if (fuel == null) return;
            
            String[] transOpts = {"Automatic", "Manual"};
            String trans = (String) JOptionPane.showInputDialog(this, "Select Transmission:", "Edit Transmission", JOptionPane.QUESTION_MESSAGE, null, transOpts, vehiclesTable.getValueAt(row, 7));
            if (trans == null) return;
            
            String priceStr = getInput("New Daily Price (current: " + vehiclesTable.getValueAt(row, 4) + "):");
            if (priceStr == null || priceStr.trim().isEmpty()) return;
            double price = Double.parseDouble(priceStr);
            if (price <= 0) { showError("Price must be positive."); return; }
            
            try (Connection conn = DBConnection.getConnection()) {
                String q = "UPDATE Vehicle SET Brand=?, Model=?, FuelType=?, Transmission=?, DailyPrice=? WHERE VehicleID=?";
                PreparedStatement stmt = conn.prepareStatement(q);
                stmt.setString(1, brand);
                stmt.setString(2, model);
                stmt.setString(3, fuel);
                stmt.setString(4, trans);
                stmt.setDouble(5, price);
                stmt.setInt(6, vId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Vehicle updated!");
                loadVehicles();
            }
        } catch (Exception ex) { showError("Update failed: " + ex.getMessage()); }
    }
    
    private void deleteSelectedVehicle() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { showError("Select a vehicle to delete."); return; }
        int vId = (int) vehiclesTable.getValueAt(row, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to PERMANENTLY delete this vehicle?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Vehicle WHERE VehicleID = ?");
            stmt.setInt(1, vId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Vehicle deleted!");
            loadVehicles();
        } catch (Exception ex) {
            showError("Cannot delete vehicle. It might have active reservations. Try using 'Toggle Availability' to hide it instead.");
        }
    }
    
    private void toggleVehicleStatus() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { showError("Select a vehicle first."); return; }
        int vId = (int) vehiclesTable.getValueAt(row, 0);
        String currentStatus = (String) vehiclesTable.getValueAt(row, 5);
        String newStatus = currentStatus.equals("Available") ? "Unavailable" : "Available";
        
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Vehicle SET Status = ? WHERE VehicleID = ?");
            stmt.setString(1, newStatus);
            stmt.setInt(2, vId);
            stmt.executeUpdate();
            loadVehicles();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }
    
    private void loadVehicles() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT VehicleID, Brand, Model, Location, DailyPrice, Status, FuelType, Transmission FROM Vehicle WHERE ManagerID = ?");
            stmt.setInt(1, getManagerId());
            ResultSet rs = stmt.executeQuery();
            String[] cols = {"ID", "Brand", "Model", "Loc.", "Price", "Status", "Fuel", "Trans."};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getString(7), rs.getString(8)});
            }
            vehiclesTable.setModel(model);
        } catch (Exception ex) {}
    }
    
    private void loadRequests() {
        try (Connection conn = DBConnection.getConnection()) {
            String q = "SELECT r.ReservationID, v.Brand, v.Model, r.PickupDate, r.ReturnDate, r.TotalPrice, r.Status FROM Reservation r JOIN Vehicle v ON r.VehicleID = v.VehicleID WHERE v.ManagerID = ?";
            PreparedStatement stmt = conn.prepareStatement(q);
            stmt.setInt(1, getManagerId());
            ResultSet rs = stmt.executeQuery();
            String[] cols = {"Res ID", "Brand", "Model", "Pickup", "Return", "Total ($)", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDate(4), rs.getDate(5), rs.getDouble(6), rs.getString(7)});
            requestsTable.setModel(model);
        } catch (Exception ex) {}
    }
    
    private void updateStatus(String newStatus) {
        int row = requestsTable.getSelectedRow();
        if (row == -1) return;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Reservation SET Status = ? WHERE ReservationID = ?");
            stmt.setString(1, newStatus);
            stmt.setInt(2, (int) requestsTable.getValueAt(row, 0));
            stmt.executeUpdate();
            loadRequests();
        } catch (Exception ex) {}
    }
    
    private JPanel createEarningsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridLayout(2, 2, 20, 20));
        center.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        Runnable loadStats = () -> {
            center.removeAll();
            try (Connection conn = DBConnection.getConnection()) {
                String q1 = "SELECT SUM(r.TotalPrice), COUNT(r.ReservationID) FROM Reservation r JOIN Vehicle v ON r.VehicleID = v.VehicleID WHERE v.ManagerID = ? AND r.Status IN ('Completed', 'Approved')";
                PreparedStatement stmt1 = conn.prepareStatement(q1);
                stmt1.setInt(1, getManagerId());
                ResultSet rs1 = stmt1.executeQuery();
                String earn = "$0.00", comp = "0";
                if (rs1.next()) {
                    earn = "$" + String.format("%.2f", rs1.getDouble(1));
                    comp = String.valueOf(rs1.getInt(2));
                }
                
                String active = "0";
                String q2 = "SELECT COUNT(*) FROM Reservation r JOIN Vehicle v ON r.VehicleID = v.VehicleID WHERE v.ManagerID = ? AND r.Status IN ('Pending', 'Approved')";
                PreparedStatement stmt2 = conn.prepareStatement(q2);
                stmt2.setInt(1, getManagerId());
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) active = String.valueOf(rs2.getInt(1));
                
                String avg = "0.0";
                String q3 = "SELECT AVG(Rating) FROM Review r JOIN Vehicle v ON r.VehicleID = v.VehicleID WHERE v.ManagerID = ?";
                PreparedStatement stmt3 = conn.prepareStatement(q3);
                stmt3.setInt(1, getManagerId());
                ResultSet rs3 = stmt3.executeQuery();
                if (rs3.next()) avg = String.format("%.1f", rs3.getDouble(1));

                center.add(createStatCard("Total Earnings", earn, new Color(46, 204, 113)));
                center.add(createStatCard("Completed Rentals", comp, new Color(52, 152, 219)));
                center.add(createStatCard("Average Rating", avg, new Color(241, 196, 15)));
                center.add(createStatCard("Active Res", active, new Color(230, 126, 34)));

            } catch (Exception ex) {}
            center.revalidate();
            center.repaint();
        };
        
        panel.add(center, BorderLayout.CENTER);
        JButton refreshBtn = new JButton("Refresh Statistics");
        refreshBtn.addActionListener(e -> loadStats.run());
        JPanel bot = new JPanel(); bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);
        
        loadStats.run();
        return panel;
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
    @Override public String getInput(String prompt) { return JOptionPane.showInputDialog(this, prompt); }
}

