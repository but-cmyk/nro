package database.daos;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import database.AlyraManager;
import utils.Logger;

public class EventDAO {

    private static long remainingTimeToIncreasePotentialAndPower = 0;
    private static long remainingTimeToIncreaseHP = 0;
    private static long remainingTimeToIncreaseMP = 0;
    private static long remainingTimeToIncreaseDame = 0;

    public static long getRemainingTimeToIncreasePotentialAndPower() { return remainingTimeToIncreasePotentialAndPower; }
    public static void setRemainingTimeToIncreasePotentialAndPower(long v) { remainingTimeToIncreasePotentialAndPower = v; }

    public static long getRemainingTimeToIncreaseHP() { return remainingTimeToIncreaseHP; }
    public static void setRemainingTimeToIncreaseHP(long v) { remainingTimeToIncreaseHP = v; }

    public static long getRemainingTimeToIncreaseMP() { return remainingTimeToIncreaseMP; }
    public static void setRemainingTimeToIncreaseMP(long v) { remainingTimeToIncreaseMP = v; }

    public static long getRemainingTimeToIncreaseDame() { return remainingTimeToIncreaseDame; }
    public static void setRemainingTimeToIncreaseDame(long v) { remainingTimeToIncreaseDame = v; }

    public static void loadInternationalWomensDayEvent() {
        try (Connection con = AlyraManager.getConnection();) {
            PreparedStatement ps = con.prepareStatement("SELECT `data` FROM `event` WHERE `name` = 'international_womens_day'");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                JsonObject json = new Gson().fromJson(rs.getString("data"), JsonObject.class);
                remainingTimeToIncreasePotentialAndPower = json.get("remaining_time_to_increase_potential_and_power").getAsLong();
                remainingTimeToIncreaseHP = json.get("remaining_time_to_increase_hp").getAsLong();
                remainingTimeToIncreaseMP = json.get("remaining_time_to_increase_mp").getAsLong();
                remainingTimeToIncreaseDame = json.get("remaining_time_to_increase_dame").getAsLong();
            }
        } catch (Exception e) {
            Logger.logException(EventDAO.class, e, "Lỗi load event ngày quốc tế phụ nữ");
        }
    }

    public static void save() {
        saveInternationalWomensDayEvent();
    }

    private static void saveInternationalWomensDayEvent() {
        try (Connection con = AlyraManager.getConnection();) {
            PreparedStatement ps = con.prepareStatement("UPDATE `event` SET `data` = ? WHERE `name` = 'international_womens_day'");
            JsonObject json = new JsonObject();
            json.addProperty("remaining_time_to_increase_potential_and_power", remainingTimeToIncreasePotentialAndPower);
            json.addProperty("remaining_time_to_increase_hp", remainingTimeToIncreaseHP);
            json.addProperty("remaining_time_to_increase_mp", remainingTimeToIncreaseMP);
            json.addProperty("remaining_time_to_increase_dame", remainingTimeToIncreaseDame);
            ps.setString(1, json.toString());
            ps.executeUpdate();
        } catch (Exception e) {
            Logger.logException(EventDAO.class, e, "Lỗi save event ngày quốc tế phụ nữ");
        }
    }
}
