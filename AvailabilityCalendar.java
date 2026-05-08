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
private void updateCalendar() {
        calendarPanel.removeAll();
        monthLabel.setText(displayDate.getMonth().name() + " " + displayDate.getYear());

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            calendarPanel.add(lbl);
        }

        int startOffset = displayDate.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < startOffset; i++) calendarPanel.add(new JLabel(""));

        int daysInMonth = displayDate.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = displayDate.withDayOfMonth(i);
            JButton btn = new JButton(String.valueOf(i));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            if (bookedDates.contains(date)) {
                btn.setBackground(new Color(231, 76, 60)); // Red
                btn.setForeground(Color.WHITE);
                btn.setEnabled(false);
            } else if (date.equals(selectedStart) || date.equals(selectedEnd)) {
                btn.setBackground(new Color(52, 152, 219)); // Blue
                btn.setForeground(Color.WHITE);
            } else if (selectedStart != null && selectedEnd != null && date.isAfter(selectedStart) && date.isBefore(selectedEnd)) {
                btn.setBackground(new Color(174, 214, 241)); // Light Blue
            } else {
                btn.setBackground(Color.WHITE);
            }

            btn.addActionListener(e -> {
                if (selectedStart == null || (selectedStart != null && selectedEnd != null)) {
                    selectedStart = date;
                    selectedEnd = null;
                } else if (date.isBefore(selectedStart)) {
                    selectedStart = date;
                } else {
                    // Check if any date in between is booked
                    LocalDate temp = selectedStart;
                    boolean conflict = false;
                    while (!temp.isAfter(date)) {
                        if (bookedDates.contains(temp)) { conflict = true; break; }
                        temp = temp.plusDays(1);
                    }
                    if (conflict) {
                        JOptionPane.showMessageDialog(this, "Selected range contains booked dates!");
                    } else {
                        selectedEnd = date;
                    }
                }
                updateCalendar();
            });

            calendarPanel.add(btn);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    public String getStartDate() { return selectedStart != null ? selectedStart.toString() : null; }
    public String getEndDate() { return selectedEnd != null ? selectedEnd.toString() : null; }
}
