package managers;

import database.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import models.item.Item.ItemOption;

import models.GiftCode;
import models.player.Player;
import services.map.NpcService;
import services.Service;
import java.util.ArrayList;
import services.player.InventoryService;

public class GiftCodeManager {

    public String name;
    public final ArrayList<GiftCode> listGiftCode = new ArrayList<>();

    private static GiftCodeManager instance;

    public static GiftCodeManager gI() {
        if (instance == null) {
            instance = new GiftCodeManager();
        }
        return instance;
    }

    public synchronized GiftCode checkUseGiftCode(Player player, String code) {
        for (GiftCode giftCode : listGiftCode) {
            if (giftCode.code.equals(code)) {
                if (giftCode.countLeft <= 0) {
                    Service.gI().sendThongBaoOK(player, "Giftcode đã hết hạn");
                    return null;
                }
                if (giftCode.isUsedGiftCode(player)) {
                    Service.gI().sendThongBaoOK(player, "Bạn đã sử dụng code này trước đó rồi!");
                    return null;
                }
                if (InventoryService.gI().getCountEmptyBag(player) < giftCode.detail.size()) {
                    Service.gI().sendThongBaoOK(player, "Cần tối thiểu " + giftCode.detail.size() + " ô hành trang trống");
                    return null;
                }
                giftCode.countLeft -= 1;
                player.giftCode.add(code);
                updateGiftCode(giftCode);
                return giftCode;
            }
        }
        return null;
    }

    public void updateGiftCode(GiftCode giftcode) {
        try {
            AlyraManager.executeUpdate("update giftcode set count_left = ? where id = ?", giftcode.countLeft, giftcode.id);
        } catch (Exception e) {
        }
    }

    public void checkInfomationGiftCode(Player p) {
        StringBuilder sb = new StringBuilder();
        for (GiftCode giftCode : listGiftCode) {
            sb.append("Code: ").append(giftCode.code).append(", Số lượng còn lại: ").append(giftCode.countLeft).append("\b")
                    .append("Ngày tạo: ")
                    .append(giftCode.datecreate).append(", Ngày hết hạn: ").append(giftCode.dateexpired)
                    .append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        NpcService.gI().createTutorial(p, 5073, sb.toString());
    }


    public synchronized void reload() {
        this.listGiftCode.clear();
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM giftcode ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                GiftCode giftcode = new GiftCode();
                giftcode.code = rs.getString("code");
                giftcode.id = rs.getInt("id");
                giftcode.countLeft = rs.getInt("count_left");
                String ag = rs.getString("allGender");
                giftcode.allGender = "all".equalsIgnoreCase(ag) || rs.getBoolean("allGender");
                if (giftcode.countLeft == -1) {
                    giftcode.countLeft = 999999999;
                }
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);
                        int id = Integer.parseInt(jsonObj.get("id").toString());
                        int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                        JSONArray option = (JSONArray) jsonObj.get("options");
                        ArrayList<ItemOption> optionList = new ArrayList<>();
                        if (option != null) {
                            for (int u = 0; u < option.size(); u++) {
                                JSONObject jsonobject = (JSONObject) option.get(u);
                                int optionId = Integer.parseInt(jsonobject.get("id").toString());
                                int param = Integer.parseInt(jsonobject.get("param").toString());
                                optionList.add(new ItemOption(optionId, param));
                            }
                        }
                        giftcode.option.put(id, optionList);
                        giftcode.detail.put(id, quantity);
                    }
                }
                this.listGiftCode.add(giftcode);
            }
            utils.Logger.log("GiftCodeManager: Reloaded " + this.listGiftCode.size() + " giftcodes.");
        } catch (Exception e) {
            utils.Logger.logException(GiftCodeManager.class, e, "Lỗi reload giftcode từ database");
        }
    }

    public synchronized boolean createGiftCode(String code, int count, int days, String detailJson, String allGender) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        long now = System.currentTimeMillis();
        long expiredMillis = now + ((long) Math.max(days, 1) * 24L * 60L * 60L * 1000L);
        Timestamp createTs = new Timestamp(now);
        Timestamp expiredTs = new Timestamp(expiredMillis);
        String gender = (allGender == null || allGender.trim().isEmpty()) ? "all" : allGender;

        String sql = "INSERT INTO giftcode (code, count_left, detail, allGender, datecreate, expired) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim().toLowerCase());
            ps.setInt(2, count);
            ps.setString(3, detailJson);
            ps.setString(4, gender);
            ps.setTimestamp(5, createTs);
            ps.setTimestamp(6, expiredTs);
            int row = ps.executeUpdate();
            if (row > 0) {
                reload();
                return true;
            }
        } catch (Exception e) {
            utils.Logger.logException(GiftCodeManager.class, e, "Lỗi tạo giftcode: " + code);
        }
        return false;
    }

    public synchronized boolean deleteGiftCode(int id) {
        String sql = "DELETE FROM giftcode WHERE id = ?";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int row = ps.executeUpdate();
            if (row > 0) {
                this.listGiftCode.removeIf(gc -> gc.id == id);
                return true;
            }
        } catch (Exception e) {
            utils.Logger.logException(GiftCodeManager.class, e, "Lỗi xóa giftcode id: " + id);
        }
        return false;
    }
}
