import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityCalendar extends JDialog {
    private int vehicleId;
    private LocalDate selectedStart;
    private LocalDate selectedEnd;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private LocalDate displayDate;
    private List<LocalDate> bookedDates = new ArrayList<>();

    public AvailabilityCalendar(JFrame parent, int vehicleId) {
        super(parent, "Select Dates", true);
        this.vehicleId = vehicleId;
        this.displayDate = LocalDate.now().withDayOfMonth(1);
        
        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        fetchBookedDates();

        JPanel header = new JPanel(new BorderLayout());
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        header.add(prev, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        calendarPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        add(calendarPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(2, 1));
        JLabel selectionLabel = new JLabel("Select Start and End dates", SwingConstants.CENTER);
        JButton confirmBtn = new JButton("Confirm Reservation Dates");
        footer.add(selectionLabel);
        footer.add(confirmBtn);
        add(footer, BorderLayout.SOUTH);

        prev.addActionListener(e -> { displayDate = displayDate.minusMonths(1); updateCalendar(); });
        next.addActionListener(e -> { displayDate = displayDate.plusMonths(1); updateCalendar(); });
        
        confirmBtn.addActionListener(e -> {
            if (selectedStart != null && selectedEnd != null) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please select both start and end dates.");
            }
        });

        updateCalendar();
    }

    private void fetchBookedDates() {
        try (Connection conn = DBConnection.getConnection()) {
            String q = "SELECT PickupDate, ReturnDate FROM Reservation WHERE VehicleID = ? AND Status NOT IN ('Rejected', 'Canceled')";
            PreparedStatement stmt = conn.prepareStatement(q);
            stmt.setInt(1, vehicleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate start = rs.getDate(1).toLocalDate();
                LocalDate end = rs.getDate(2).toLocalDate();
                while (!start.isAfter(end)) {
                    bookedDates.add(start);
                    start = start.plusDays(1);
                }
            }
        } catch (Exception ex) {}
    }
  
}
