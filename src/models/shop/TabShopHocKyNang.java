package models.shop;

import java.util.ArrayList;
import models.player.Player;
import models.skill.Skill;
import utils.SkillUtil;

public class TabShopHocKyNang extends TabShop {

    public TabShopHocKyNang() {
        super();
    }

    public TabShopHocKyNang(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        if (tabShop == null) {
            return;
        }
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;
        this.index = tabShop.index;

        if (player == null || tabShop.itemShops == null) {
            return;
        }

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop != null && itemShop.temp != null) {
                if ((itemShop.temp.gender == player.gender || itemShop.temp.gender > 2)
                        && !isSkillLearned(player, itemShop)) {
                    this.itemShops.add(new ItemShop(itemShop));
                }
            }
        }
    }

    private boolean isSkillLearned(Player player, ItemShop itemShop) {
        if (player.BoughtSkills != null && player.BoughtSkills.contains((int) itemShop.temp.id)) {
            return true;
        }
        Skill curSkill = SkillUtil.getSkillByItemID(player, itemShop.temp.id);
        if (curSkill != null && curSkill.point > 0) {
            int levelBook = getSkillLevel(itemShop);
            if (curSkill.point >= levelBook) {
                return true;
            }
        }
        return false;
    }

    private int getSkillLevel(ItemShop is) {
        if (is.temp.level > 0) {
            return is.temp.level;
        }
        if (is.temp.name != null && !is.temp.name.isEmpty()) {
            String name = is.temp.name.trim();
            char lastChar = name.charAt(name.length() - 1);
            if (Character.isDigit(lastChar)) {
                return Character.getNumericValue(lastChar);
            }
        }
        return 1;
    }
}
