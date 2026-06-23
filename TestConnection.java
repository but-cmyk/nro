import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1:3306/nro_acc?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                System.out.println("Connected successfully!");
                
                // Test 1: standard TYPE_FORWARD_ONLY statement
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM account LIMIT 1");
                     ResultSet rs = ps.executeQuery()) {
                    System.out.println("Testing TYPE_FORWARD_ONLY:");
                    try {
                        if (rs.next()) {
                            System.out.println("-> rs.next() worked, value: " + rs.getInt(1));
                        }
                    } catch (Exception e) {
                        System.out.println("-> rs.next() failed: " + e.getMessage());
                    }
                    try {
                        rs.first();
                        System.out.println("-> rs.first() worked");
                    } catch (Exception e) {
                        System.out.println("-> rs.first() failed: " + e.getMessage());
                    }
                    try {
                        rs.last();
                        System.out.println("-> rs.last() worked");
                    } catch (Exception e) {
                        System.out.println("-> rs.last() failed: " + e.getMessage());
                    }
                }
                
                // Test 2: TYPE_SCROLL_INSENSITIVE statement
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM account LIMIT 1", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                     ResultSet rs = ps.executeQuery()) {
                    System.out.println("Testing TYPE_SCROLL_INSENSITIVE:");
                    try {
                        if (rs.next()) {
                            System.out.println("-> rs.next() worked, value: " + rs.getInt(1));
                        }
                    } catch (Exception e) {
                        System.out.println("-> rs.next() failed: " + e.getMessage());
                    }
                    try {
                        rs.first();
                        System.out.println("-> rs.first() worked");
                    } catch (Exception e) {
                        System.out.println("-> rs.first() failed: " + e.getMessage());
                    }
                    try {
                        rs.last();
                        System.out.println("-> rs.last() worked");
                    } catch (Exception e) {
                        System.out.println("-> rs.last() failed: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
