import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class CustomerDashboard extends JFrame implements IUserInterface {
    private IVehicleDAO vehicleDAO;
    private IReservationDAO reservationDAO;
    private ReservationManager reservationManager;
    private JTable searchTable, myResTable;
    private int customerId = -1;

    public CustomerDashboard() {
        vehicleDAO = new VehicleDAO();
        reservationDAO = new ReservationDAO();
        reservationManager = new ReservationManager();

        setTitle("CRS - Customer Dashboard");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel userInfo = new JLabel("Role: Customer  |  User: " + AuthController.currentUser.name);
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
        tabs.add("Search Vehicles", createSearchPanel());
        tabs.add("My Reservations", createMyReservationsPanel());
        tabs.add("My Statistics", createStatsPanel());

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new GridLayout(3, 4, 10, 10));
        top.setBorder(BorderFactory.createTitledBorder("Search Filters"));
        JTextField locField = new JTextField();
        String[] fuelOpts = { "Any", "Gasoline", "Diesel", "Electric", "Hybrid" };
        JComboBox<String> fuelCombo = new JComboBox<>(fuelOpts);
        String[] transOpts = { "Any", "Automatic", "Manual" };
        JComboBox<String> transCombo = new JComboBox<>(transOpts);
        String[] catOpts = { "Any", "Sedan", "SUV", "Hatchback", "Luxury", "Economy" };
        JComboBox<String> catCombo = new JComboBox<>(catOpts);
        JTextField priceField = new JTextField("0");

        top.add(new JLabel("Location:"));
        top.add(locField);
        top.add(new JLabel("Fuel Type:"));
        top.add(fuelCombo);
        top.add(new JLabel("Transmission:"));
        top.add(transCombo);
        top.add(new JLabel("Vehicle Type:"));
        top.add(catCombo);
        top.add(new JLabel("Max Price ($):"));
        top.add(priceField);

        panel.add(top, BorderLayout.NORTH);

        searchTable = new JTable();
        searchTable.setRowHeight(25);
        searchTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton detailsBtn = new JButton("View Details & Reviews");
        JButton bookBtn = new JButton("Reserve Selected Vehicle");
        bot.add(detailsBtn);
        bot.add(bookBtn);
        panel.add(bot, BorderLayout.SOUTH);

        Runnable triggerSearch = () -> {
            try {
                double maxP = 0;
                try {
                    maxP = Double.parseDouble(priceField.getText());
                } catch (Exception ex) {
                }
                List<Vehicle> list = vehicleDAO.searchVehicles(locField.getText(),
                        fuelCombo.getSelectedItem().toString(),
                        transCombo.getSelectedItem().toString(),
                        catCombo.getSelectedItem().toString(),
                        maxP);
                displayVehicles(list);
            } catch (Exception ex) {
            }
        };

        locField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                triggerSearch.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                triggerSearch.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                triggerSearch.run();
            }
        });

        priceField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                triggerSearch.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                triggerSearch.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                triggerSearch.run();
            }
        });

        fuelCombo.addActionListener(e -> triggerSearch.run());
        transCombo.addActionListener(e -> triggerSearch.run());
        catCombo.addActionListener(e -> triggerSearch.run());

        detailsBtn.addActionListener(e -> {
            int row = searchTable.getSelectedRow();
            if (row == -1) {
                showError("Select a vehicle.");
                return;
            }
            showVehicleDetails((int) searchTable.getValueAt(row, 0));
        });

        bookBtn.addActionListener(e -> {
            int row = searchTable.getSelectedRow();
            if (row == -1) {
                showError("Select a vehicle first.");
                return;
            }
            int vId = (int) searchTable.getValueAt(row, 0);
            double price = (double) searchTable.getValueAt(row, 7);
            bookVehicle(vId, price);
        });

        try {
            displayVehicles(vehicleDAO.getAllVehicles());
        } catch (Exception ex) {
        }
        return panel;
    }

    private void showVehicleDetails(int vehicleId) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement vStmt = conn.prepareStatement("SELECT * FROM Vehicle WHERE VehicleID = ?");
            vStmt.setInt(1, vehicleId);
            ResultSet rs = vStmt.executeQuery();
            if (!rs.next())
                return;

            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(rs.getString("Brand")).append(" ").append(rs.getString("Model"))
                    .append(" ===\n\n");
            sb.append("Location: ").append(rs.getString("Location")).append("\n");
            sb.append("Type: ").append(rs.getString("Category")).append("\n");
            sb.append("Price: $").append(rs.getDouble("DailyPrice")).append(" / day\n");
            sb.append("Fuel: ").append(rs.getString("FuelType")).append("\n");
            sb.append("Transmission: ").append(rs.getString("Transmission")).append("\n");
            sb.append("Seats: ").append(rs.getInt("Seats")).append("\n");
            sb.append("Features: ").append(rs.getString("Features")).append("\n\n");
            sb.append("--- Customer Reviews ---\n");

            PreparedStatement rStmt = conn.prepareStatement(
                    "SELECT r.Rating, r.Comment, u.Name FROM Review r JOIN Customer c ON r.CustomerID = c.CustomerID JOIN User u ON c.UserID = u.UserID WHERE r.VehicleID = ?");
            rStmt.setInt(1, vehicleId);
            ResultSet rs2 = rStmt.executeQuery();
            boolean hasReviews = false;
            while (rs2.next()) {
                hasReviews = true;
                sb.append("[").append(rs2.getInt(1)).append("/5] ").append(rs2.getString(3)).append(": ")
                        .append(rs2.getString(2)).append("\n");
            }
            if (!hasReviews)
                sb.append("No reviews yet.");

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setMargin(new java.awt.Insets(10, 10, 10, 10));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Vehicle Details",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    private void bookVehicle(int vehicleId, double price) {
        AvailabilityCalendar cal = new AvailabilityCalendar(this, vehicleId);
        cal.setVisible(true);

        String startStr = cal.getStartDate();
        String endStr = cal.getEndDate();

        if (startStr == null || endStr == null)
            return;

        try {
            Date pickup = Date.valueOf(startStr);
            Date dropoff = Date.valueOf(endStr);

            long diff = dropoff.getTime() - pickup.getTime();
            int days = (int) (diff / (1000 * 60 * 60 * 24)) + 1;

            double total = reservationManager.calculateTotal(price, days);

            String[] methods = { "Credit Card", "Installment" };
            String paymentMethod = (String) JOptionPane.showInputDialog(this,
                    "Total Price (incl. tax/insurance): $" + String.format("%.2f", total) + "\nSelect Payment Method:",
                    "Payment Module", JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);

            if (paymentMethod == null)
                return; //Cancelled

            Reservation r = new Reservation();
            r.customerId = getCustomerId();
            r.vehicleId = vehicleId;
            r.pickupDate = pickup;
            r.returnDate = dropoff;
            r.totalPrice = total;
            r.status = "Pending";

            if (reservationManager.confirmBooking(r)) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO Payment (ReservationID, Amount, PaymentMethod, Status) VALUES (?, ?, ?, 'completed')");
                    pstmt.setInt(1, r.reservationId);
                    pstmt.setDouble(2, total);
                    pstmt.setString(3, paymentMethod.equals("Credit Card") ? "credit_card" : "installment");
                    pstmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Reservation and Payment successful! Awaiting approval.");
                loadMyReservations();
            }
        } catch (IllegalArgumentException iae) {
            showError("Invalid date format. Use YYYY-MM-DD.");
        } catch (Exception ex) {
            showError("Booking failed: " + ex.getMessage());
        }
    }

    private void displayVehicles(List<Vehicle> list) {
        String[] cols = { "ID", "Brand", "Model", "Type", "Location", "Fuel", "Trans.", "Daily Price ($)", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (Vehicle v : list) {
            if ("Available".equals(v.status)) {
                model.addRow(new Object[] { v.vehicleId, v.brand, v.model, v.category, v.location, v.fuelType,
                        v.transmission, v.dailyPrice, v.status });
            }
        }
        searchTable.setModel(model);
    }

    private JPanel createMyReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        myResTable = new JTable();
        myResTable.setRowHeight(25);
        myResTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(new JScrollPane(myResTable), BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton reviewBtn = new JButton("Leave Review");
        JButton cancelBtn = new JButton("Cancel Reservation");
        JButton refreshBtn = new JButton("Refresh");

        bot.add(reviewBtn);
        bot.add(cancelBtn);
        bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadMyReservations());
        cancelBtn.addActionListener(e -> cancelSelectedReservation());
        reviewBtn.addActionListener(e -> {
            int row = myResTable.getSelectedRow();
            if (row == -1) {
                showError("Select a reservation.");
                return;
            }
            String status = (String) myResTable.getValueAt(row, 5);
            if (!"Completed".equals(status)) {
                showError("You can only review completed rentals.");
                return;
            }
            leaveReview((int) myResTable.getValueAt(row, 1));
        });

        loadMyReservations();
        return panel;
    }

    private void cancelSelectedReservation() {
        int row = myResTable.getSelectedRow();
        if (row == -1)
            return;
        int resId = (int) myResTable.getValueAt(row, 0);
        String status = (String) myResTable.getValueAt(row, 5);

        if (!status.equals("Pending")) {
            showError("You can only cancel pending reservations.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Cancel this reservation?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn
                    .prepareStatement("UPDATE Reservation SET Status = 'Canceled' WHERE ReservationID = ?");
            stmt.setInt(1, resId);
            stmt.executeUpdate();
            loadMyReservations();
            JOptionPane.showMessageDialog(this, "Reservation canceled successfully.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void leaveReview(int vehicleId) {
        String ratingStr = getInput("Rate this vehicle (1-5):");
        if (ratingStr == null)
            return;
        String comment = getInput("Enter your comment:");
        if (comment == null)
            return;
        try {
            int rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5)
                throw new NumberFormatException();
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Review (VehicleID, CustomerID, Rating, Comment) VALUES (?, ?, ?, ?)");
                stmt.setInt(1, vehicleId);
                stmt.setInt(2, getCustomerId());
                stmt.setInt(3, rating);
                stmt.setString(4, comment);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Review submitted!");
            }
        } catch (Exception ex) {
            showError("Invalid input.");
        }
    }

    private void loadMyReservations() {
        try {
            List<Reservation> list = reservationDAO.getReservationsByCustomer(getCustomerId());
            String[] cols = { "Res ID", "Vehicle ID", "Pickup Date", "Return Date", "Total ($)", "Status" };
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            for (Reservation r : list) {
                model.addRow(new Object[] { r.reservationId, r.vehicleId, r.pickupDate, r.returnDate, r.totalPrice,
                        r.status });
            }
            myResTable.setModel(model);
        } catch (Exception e) {
        }
    }

    private JPanel createStatsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridLayout(2, 2, 20, 20));
        center.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        Runnable loadStats = () -> {
            center.removeAll();
            try (Connection conn = DBConnection.getConnection()) {
                int cId = getCustomerId();
                String q1 = "SELECT SUM(TotalPrice), COUNT(ReservationID) FROM Reservation WHERE CustomerID = ? AND Status IN ('Completed', 'Approved')";
                PreparedStatement stmt1 = conn.prepareStatement(q1);
                stmt1.setInt(1, cId);
                ResultSet rs1 = stmt1.executeQuery();
                String spent = "$0.00", rented = "0";
                if (rs1.next()) {
                    spent = "$" + String.format("%.2f", rs1.getDouble(1));
                    rented = String.valueOf(rs1.getInt(2));
                }

                String fav = "-";
                String q2 = "SELECT v.Brand FROM Reservation r JOIN Vehicle v ON r.VehicleID = v.VehicleID WHERE r.CustomerID = ? GROUP BY v.Brand ORDER BY COUNT(*) DESC LIMIT 1";
                PreparedStatement stmt2 = conn.prepareStatement(q2);
                stmt2.setInt(1, cId);
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next())
                    fav = rs2.getString(1);

                String pending = "0";
                String q3 = "SELECT COUNT(*) FROM Reservation WHERE CustomerID = ? AND Status = 'Pending'";
                PreparedStatement stmt3 = conn.prepareStatement(q3);
                stmt3.setInt(1, cId);
                ResultSet rs3 = stmt3.executeQuery();
                if (rs3.next())
                    pending = String.valueOf(rs3.getInt(1));

                center.add(createStatCard("Total Spent", spent, new Color(46, 204, 113)));
                center.add(createStatCard("Vehicles Rented", rented, new Color(52, 152, 219)));
                center.add(createStatCard("Favorite Brand", fav, new Color(155, 89, 182)));
                center.add(createStatCard("Pending Res", pending, new Color(230, 126, 34)));

            } catch (Exception ex) {
            }
            center.revalidate();
            center.repaint();
        };

        mainPanel.add(center, BorderLayout.CENTER);
        JButton refreshBtn = new JButton("Refresh Statistics");
        refreshBtn.addActionListener(e -> loadStats.run());
        JPanel bot = new JPanel();
        bot.add(refreshBtn);
        mainPanel.add(bot, BorderLayout.SOUTH);

        loadStats.run();
        return mainPanel;
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

    private int getCustomerId() {
        if (customerId != -1)
            return customerId;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT CustomerID FROM Customer WHERE UserID = ?");
            stmt.setInt(1, AuthController.currentUser.userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                customerId = rs.getInt(1);
        } catch (Exception ex) {
            showError("Customer session error.");
        }
        return customerId;
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
