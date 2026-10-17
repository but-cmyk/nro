package database.daos;

import models.item.Item;
import models.shop.ItemShop;
import models.shop.Shop;
import models.shop.TabShop;
import services.ItemService;
import utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ShopDAO {

    private static final int[] MASENKO_BOOK_DAMES = {115, 130, 150, 170, 190, 210, 230};
    private static final int[] ANTOMIC_BOOK_DAMES = {125, 140, 170, 200, 230, 260, 290};
    private static final int[] GALICK_BOOK_DAMES  = {110, 120, 130, 140, 150, 160, 170};

    public static List<Shop> getShops(Connection con) {
        List<Shop> list = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("select * from shop order by npc_id asc");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Shop shop = new Shop();
                shop.id = rs.getInt("id");
                shop.npcId = rs.getByte("npc_id");
                shop.tagName = rs.getString("tag_name");
                shop.typeShop = rs.getByte("type_shop");
                loadShopTab(con, shop);
                list.add(shop);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return list;
    }

    private static void loadShopTab(Connection con, Shop shop) {
        try {
            PreparedStatement ps = con
                    .prepareStatement("select * from tab_shop where shop_id = ? order by tab_index asc");
            ps.setInt(1, shop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TabShop tab = new TabShop();
                tab.shop = shop;
                tab.id = rs.getInt("id");
                tab.name = rs.getString("tab_name").replaceAll("<>", "\n");
                tab.index = rs.getInt("tab_index");
                loadItemShop(con, tab);
                shop.tabShops.add(tab);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

    private static void loadItemShop(Connection con, TabShop tabShop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from tab_shop where tab_index = ? and shop_id = ?");
            ps.setInt(1, tabShop.index);
            ps.setInt(2, tabShop.shop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONArray dataArray;
                JSONValue jv = new JSONValue();
                JSONObject dataObject;
                dataArray = (JSONArray) jv.parse(rs.getString("items"));
                for (Object o : dataArray) {
                    Item item = null;
                    dataObject = (JSONObject) o;
                    ItemShop itemShop = new ItemShop();
                    itemShop.tabShop = tabShop;
                    itemShop.id = tabShop.itemShops.size() + 1;
                    itemShop.temp = ItemService.gI()
                            .getTemplate(Short.parseShort(String.valueOf(dataObject.get("temp_id"))));
                    itemShop.isNew = Boolean.parseBoolean(String.valueOf(dataObject.get("is_new")));
                    itemShop.cost = Long.parseLong(String.valueOf(dataObject.get("cost")));
                    itemShop.iconSpec = Integer.parseInt(String.valueOf(dataObject.get("item_spec")));
                    itemShop.typeSell = Byte.parseByte(String.valueOf(dataObject.get("type_sell")));
                    JSONArray options = (JSONArray) dataObject.get("options");
                    for (int j = 0; j < options.size(); j++) {
                        JSONObject opt = (JSONObject) options.get(j);
                        int optId = Integer.parseInt(String.valueOf(opt.get("id")));
                        int optParam = Integer.parseInt(String.valueOf(opt.get("param")));
                        if (optId == 50 && itemShop.temp != null) {
                            int tempId = itemShop.temp.id;
                            if (tempId >= 101 && tempId <= 107) {
                                optParam = MASENKO_BOOK_DAMES[tempId - 101];
                            } else if (tempId >= 108 && tempId <= 114) {
                                optParam = ANTOMIC_BOOK_DAMES[tempId - 108];
                            } else if (tempId >= 87 && tempId <= 93) {
                                optParam = GALICK_BOOK_DAMES[tempId - 87];
                            }
                        }
                        itemShop.options.add(new Item.ItemOption(optId, optParam));
                    }
                    boolean isSell = Boolean.parseBoolean(String.valueOf(dataObject.get("is_sell")));
                    if (isSell) {
                        tabShop.itemShops.add(itemShop);
                    }
                }
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

}
