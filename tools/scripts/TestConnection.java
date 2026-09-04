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
                
                try (PreparedStatement ps = conn.prepareStatement("SELECT ip_address FROM account LIMIT 1");
                     ResultSet rs = ps.executeQuery()) {
                    System.out.println("Successfully queried ip_address from account table!");
                } catch (Exception e) {
                    System.out.println("ip_address column MISSING or ERROR: " + e.getMessage());
                }
                
                try (PreparedStatement ps = conn.prepareStatement("SELECT is_admin FROM account LIMIT 1");
                     ResultSet rs = ps.executeQuery()) {
                    System.out.println("Successfully queried is_admin from account table!");
                } catch (Exception e) {
                    System.out.println("is_admin column MISSING or ERROR: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
