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
//add codes here
}
