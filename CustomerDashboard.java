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
    
//add codes here
}
