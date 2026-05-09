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
//add codes here
}
