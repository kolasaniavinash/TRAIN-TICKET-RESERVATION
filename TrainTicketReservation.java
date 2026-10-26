import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class TrainTicketReservation {
    private static final Scanner SC = new Scanner(System.in);
    private static final String DB_URL = "jdbc:mysql://localhost:3306/train_reservation";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            initializeDatabase(con);
            seedTrains(con);
            System.out.println("=== TRAIN TICKET RESERVATION SYSTEM ===");
            boolean running = true;
            while (running) {
                System.out.println("\n1. View trains\n2. Search trains\n3. Book ticket\n4. Check PNR\n5. Cancel ticket\n6. View all bookings (admin/demo)\n0. Exit");
                int choice = readInt("Choose: ");
                try {
                    switch (choice) {
                        case 1 -> viewTrains(con);
                        case 2 -> searchTrains(con);
                        case 3 -> bookTicket(con);
                        case 4 -> checkPnr(con);
                        case 5 -> cancelTicket(con);
                        case 6 -> viewBookings(con);
                        case 0 -> running = false;
                        default -> System.out.println("Invalid option.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database operation failed: " + e.getMessage());
                }
            }
            System.out.println("Thank you for using the system.");
        } catch (SQLException e) {
            System.out.println("Could not connect to MySQL. Check database name, username, password, and Connector/J.");
            System.out.println("Details: " + e.getMessage());
        }
    }

    private static void initializeDatabase(Connection con) throws SQLException {
        String trains = "CREATE TABLE IF NOT EXISTS trains (" +
                "train_id INT PRIMARY KEY AUTO_INCREMENT, train_name VARCHAR(100) NOT NULL," +
                "source_city VARCHAR(80) NOT NULL, destination_city VARCHAR(80) NOT NULL," +
                "fare DECIMAL(10,2) NOT NULL, total_seats INT NOT NULL, available_seats INT NOT NULL)";
        String bookings = "CREATE TABLE IF NOT EXISTS bookings (" +
                "booking_id BIGINT PRIMARY KEY AUTO_INCREMENT, pnr VARCHAR(20) UNIQUE NOT NULL," +
                "train_id INT NOT NULL, passenger_name VARCHAR(100) NOT NULL, passenger_age INT NOT NULL," +
                "seats INT NOT NULL, total_fare DECIMAL(10,2) NOT NULL, status VARCHAR(20) NOT NULL," +
                "booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (train_id) REFERENCES trains(train_id))";
        try (Statement st = con.createStatement()) {
            st.executeUpdate(trains);
            st.executeUpdate(bookings);
        }
    }

    private static void seedTrains(Connection con) throws SQLException {
        try (Statement check = con.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM trains")) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }
        String sql = "INSERT INTO trains(train_name, source_city, destination_city, fare, total_seats, available_seats) VALUES(?,?,?,?,?,?)";
        Object[][] data = {
                {"Godavari Express", "Hyderabad", "Visakhapatnam", new BigDecimal("650.00"), 100, 100},
                {"Charminar Express", "Hyderabad", "Chennai", new BigDecimal("520.00"), 80, 80},
                {"East Coast Express", "Visakhapatnam", "Chennai", new BigDecimal("430.00"), 90, 90},
                {"Konark Express", "Bhubaneswar", "Hyderabad", new BigDecimal("720.00"), 75, 75}
        };
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setBigDecimal(4, (BigDecimal) row[3]);
                ps.setInt(5, (int) row[4]);
                ps.setInt(6, (int) row[5]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void viewTrains(Connection con) throws SQLException {
        String sql = "SELECT * FROM trains ORDER BY train_id";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            printTrainHeader();
            while (rs.next()) printTrain(rs);
        }
    }

    private static void searchTrains(Connection con) throws SQLException {
        String source = readLine("Source city: ").trim();
        String destination = readLine("Destination city: ").trim();
        String sql = "SELECT * FROM trains WHERE LOWER(source_city)=LOWER(?) AND LOWER(destination_city)=LOWER(?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, source);
            ps.setString(2, destination);
            try (ResultSet rs = ps.executeQuery()) {
                printTrainHeader();
                boolean found = false;
                while (rs.next()) { printTrain(rs); found = true; }
                if (!found) System.out.println("No direct trains found for that route.");
            }
        }
    }

    private static void bookTicket(Connection con) throws SQLException {
        viewTrains(con);
        int trainId = readInt("Enter train ID: ");
        String name = readLine("Passenger full name: ").trim();
        int age = readInt("Passenger age: ");
        int seats = readInt("Number of seats (this demo stores one lead passenger): ");
        if (name.isBlank() || age < 1 || age > 120 || seats < 1) {
            System.out.println("Please enter valid passenger details.");
            return;
        }

        boolean autoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            String select = "SELECT fare, available_seats FROM trains WHERE train_id=? FOR UPDATE";
            BigDecimal fare;
            int available;
            try (PreparedStatement ps = con.prepareStatement(select)) {
                ps.setInt(1, trainId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { System.out.println("Train ID not found."); con.rollback(); return; }
                    fare = rs.getBigDecimal("fare");
                    available = rs.getInt("available_seats");
                }
            }
            if (seats > available) {
                System.out.println("Only " + available + " seat(s) are available.");
                con.rollback();
                return;
            }
            BigDecimal total = fare.multiply(BigDecimal.valueOf(seats));
            String pnr = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bookings(pnr, train_id, passenger_name, passenger_age, seats, total_fare, status) VALUES(?,?,?,?,?,?, 'CONFIRMED')")) {
                ps.setString(1, pnr);
                ps.setInt(2, trainId);
                ps.setString(3, name);
                ps.setInt(4, age);
                ps.setInt(5, seats);
                ps.setBigDecimal(6, total);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE trains SET available_seats=available_seats-? WHERE train_id=?")) {
                ps.setInt(1, seats);
                ps.setInt(2, trainId);
                ps.executeUpdate();
            }
            con.commit();
            System.out.println("\n*** BOOKING CONFIRMED ***");
            System.out.println("PNR: " + pnr);
            System.out.println("Passenger: " + name);
            System.out.println("Seats: " + seats);
            System.out.println("Total fare: ₹" + total);
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommit);
        }
    }

    private static void checkPnr(Connection con) throws SQLException {
        String pnr = readLine("Enter PNR: ").trim().toUpperCase();
        String sql = "SELECT b.pnr, b.passenger_name, b.passenger_age, b.seats, b.total_fare, b.status, b.booked_at, t.train_name, t.source_city, t.destination_city " +
                "FROM bookings b JOIN trains t ON b.train_id=t.train_id WHERE b.pnr=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { System.out.println("PNR not found."); return; }
                System.out.println("PNR: " + rs.getString("pnr"));
                System.out.println("Passenger: " + rs.getString("passenger_name") + ", age " + rs.getInt("passenger_age"));
                System.out.println("Train: " + rs.getString("train_name"));
                System.out.println("Route: " + rs.getString("source_city") + " -> " + rs.getString("destination_city"));
                System.out.println("Seats: " + rs.getInt("seats"));
                System.out.println("Fare: ₹" + rs.getBigDecimal("total_fare"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Booked at: " + rs.getTimestamp("booked_at"));
            }
        }
    }

    private static void cancelTicket(Connection con) throws SQLException {
        String pnr = readLine("Enter PNR to cancel: ").trim().toUpperCase();
        boolean autoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            int trainId, seats;
            String status;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT train_id, seats, status FROM bookings WHERE pnr=? FOR UPDATE")) {
                ps.setString(1, pnr);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { System.out.println("PNR not found."); con.rollback(); return; }
                    trainId = rs.getInt("train_id");
                    seats = rs.getInt("seats");
                    status = rs.getString("status");
                }
            }
            if ("CANCELLED".equals(status)) {
                System.out.println("This ticket is already cancelled.");
                con.rollback();
                return;
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE bookings SET status='CANCELLED' WHERE pnr=?")) {
                ps.setString(1, pnr);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE trains SET available_seats=available_seats+? WHERE train_id=?")) {
                ps.setInt(1, seats);
                ps.setInt(2, trainId);
                ps.executeUpdate();
            }
            con.commit();
            System.out.println("Ticket cancelled. Seats have been released.");
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommit);
        }
    }

    private static void viewBookings(Connection con) throws SQLException {
        String sql = "SELECT b.pnr, b.passenger_name, t.train_name, b.seats, b.total_fare, b.status " +
                "FROM bookings b JOIN trains t ON b.train_id=t.train_id ORDER BY b.booking_id DESC";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-12s %-20s %-24s %-6s %-12s %-12s%n", "PNR", "PASSENGER", "TRAIN", "SEATS", "FARE", "STATUS");
            while (rs.next()) {
                System.out.printf("%-12s %-20s %-24s %-6d %-12s %-12s%n",
                        rs.getString("pnr"), rs.getString("passenger_name"), rs.getString("train_name"),
                        rs.getInt("seats"), rs.getBigDecimal("total_fare"), rs.getString("status"));
            }
        }
    }

    private static void printTrainHeader() {
        System.out.printf("%-5s %-24s %-18s %-18s %-10s %-10s%n", "ID", "TRAIN", "SOURCE", "DESTINATION", "FARE", "SEATS");
    }

    private static void printTrain(ResultSet rs) throws SQLException {
        System.out.printf("%-5d %-24s %-18s %-18s ₹%-9s %-10d%n",
                rs.getInt("train_id"), rs.getString("train_name"), rs.getString("source_city"),
                rs.getString("destination_city"), rs.getBigDecimal("fare"), rs.getInt("available_seats"));
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SC.nextLine().trim();
            try { return Integer.parseInt(value); }
            catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine();
    }
}
