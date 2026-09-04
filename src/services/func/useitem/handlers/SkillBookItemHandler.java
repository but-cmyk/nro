package services.func.useitem.handlers;

import models.item.Item;
import models.player.Player;
import models.skill.Skill;
import network.io.Message;
import services.Service;
import services.func.useitem.ItemActionHandler;
import services.player.InventoryService;
import utils.Logger;
import utils.SkillUtil;

/**
 * Xử lý sử dụng Sách Học Kỹ Năng (type 7) và Sách Nâng Cấp Chiêu Đệ Tử (402, 403, 404, 759).
 */
public class SkillBookItemHandler implements ItemActionHandler {

    @Override
    public boolean canHandle(Player player, Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        if (item.template.type == 7) {
            return true;
        }
        int id = item.template.id;
        return id == 402 || id == 403 || id == 404 || id == 759;
    }

    @Override
    public void handle(Player player, Item item, int bagIndex) {
        if (item.template.type == 7) {
            learnSkill(player, item);
        } else {
            upSkillPet(player, item);
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                            pl.BoughtSkills.add((int) item.template.id);
                        } else {
                            Skill skillNeed = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                            pl.BoughtSkills.add((int) item.template.id);
                        } else {
                            Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp " + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryService.gI().sendItemBags(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            Logger.logException(SkillBookItemHandler.class, e);
        }
    }

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            int skillSlot = switch (item.template.id) {
                case 402 -> 0;
                case 403 -> 1;
                case 404 -> 2;
                case 759 -> 3;
                default -> -1;
            };

            if (skillSlot != -1 && SkillUtil.upSkillPet(pl.pet.playerSkill.skills, skillSlot)) {
                Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }
}
