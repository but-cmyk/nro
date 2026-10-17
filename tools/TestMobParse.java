package tools;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestMobParse {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3308/nro_data", "root", "root");
            Statement stmt = conn.createStatement();
            server.Manager.gI();
            models.phoban.RedRibbonHQ dt = services.phoban.RedRibbonHQService.gI().doanhTrais.get(0);
            System.out.println("DT 0 zones count: " + dt.zones.size());
            for (models.map.Zone z : dt.zones) {
                System.out.println("  DT Zone mapId=" + z.map.mapId + " (" + z.map.mapName + ") -> mobs count: " + z.mobs.size());
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
