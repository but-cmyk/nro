package services;
import models.radar.Card;
import models.radar.OptionCard;
import models.radar.RadarCard;
import models.player.Player;
import network.io.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utils.Util;

public class RadarService {

    public List<RadarCard> RADAR_TEMPLATE = new ArrayList<>();
    private final Map<Byte, List<RadarCard>> cardsByRank = new HashMap<>();

    private static RadarService instance;

    public static RadarService gI() {
        if (instance == null) {
            instance = new RadarService();
        }
        return instance;
    }

    public synchronized void initCardsByRank() {
        cardsByRank.clear();
        for (RadarCard r : RADAR_TEMPLATE) {
            if (r != null) {
                cardsByRank.computeIfAbsent(r.Rank, k -> new ArrayList<>()).add(r);
            }
        }
    }

    public List<RadarCard> getCardsByRank(byte rank) {
        if (cardsByRank.isEmpty() && !RADAR_TEMPLATE.isEmpty()) {
            initCardsByRank();
        }
        return cardsByRank.getOrDefault(rank, Collections.emptyList());
    }

    public RadarCard getRandomCardByRank(byte rank) {
        List<RadarCard> list = getCardsByRank(rank);
        if (list.isEmpty()) {
            // Fallback nếu rank yêu cầu chưa có thẻ thì tìm bất kỳ thẻ nào có trong template
            if (!RADAR_TEMPLATE.isEmpty()) {
                return RADAR_TEMPLATE.get(Util.nextInt(RADAR_TEMPLATE.size()));
            }
            return null;
        }
        return list.get(Util.nextInt(list.size()));
    }

    public void sendRadar(Player pl, List<Card> cards) {
        try {
            Message m = new Message(127);
            m.writer().writeByte(0);
            m.writer().writeShort(RadarService.gI().RADAR_TEMPLATE.size());
            for (RadarCard radar : RadarService.gI().RADAR_TEMPLATE) {
                Card card = cards.stream().filter(c -> c != null && c.Id == radar.Id).findFirst().orElse(null);
                int maxLevel = radar.getMaxLevel();
                int maxPerLevel = radar.Max / (maxLevel > 0 ? maxLevel : 1);
                if (maxPerLevel <= 0) {
                    maxPerLevel = radar.Max > 0 ? radar.Max : 60;
                }

                byte cardLevel = 0;
                byte cardAmount = 0;
                byte cardUsed = 0;

                if (card != null) {
                    card.MaxAmount = (byte) maxPerLevel;
                    cardLevel = card.Level;
                    cardAmount = card.Amount;
                    cardUsed = card.Used;
                }

                m.writer().writeShort(radar.Id);
                m.writer().writeShort(radar.IconId);
                m.writer().writeByte(radar.Rank);
                m.writer().writeByte(cardAmount);  //amount
                m.writer().writeByte((byte) maxPerLevel);  //max_amount
                m.writer().writeByte(radar.Type);  //type 0: monster, 1: charpart
                switch (radar.Type) {
                    case 0:
                        m.writer().writeShort(radar.Template); //Monster
                        break;
                    case 1:
                        m.writer().writeShort(radar.Head); //Head
                        m.writer().writeShort(radar.Body); //Body
                        m.writer().writeShort(radar.Leg); //Leg
                        m.writer().writeShort(radar.Bag); //bag
                        break;
                }
                m.writer().writeUTF(radar.Name);  //name
                m.writer().writeUTF(getCardRichInfo(radar));  //info
                m.writer().writeByte(cardLevel);  //Level
                m.writer().writeByte(cardUsed);  //use
                m.writer().writeByte(radar.Options.size());  //option radar
                for (OptionCard option : radar.Options) {
                    m.writer().writeByte(option.id);  //id
                    m.writer().writeShort(option.param);  //param
                    m.writer().writeByte(option.active);  //ActiveCard
                }
            }
            m.writer().flush();
            pl.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void Radar1(Player pl, short id, int use) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(1);
            message.writer().writeShort(id);
            message.writer().writeByte(use);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
        }
    }

    public void RadarSetLevel(Player pl, int id, int level) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(2);
            message.writer().writeShort(id);
            message.writer().writeByte(level);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
        }
    }

    public void RadarSetAmount(Player pl, int id, int amount, int max_amount) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(3);
            message.writer().writeShort(id);
            message.writer().writeByte(amount);
            message.writer().writeByte(max_amount);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendAura(Player pl, int id_Aura, int id_Eff_Set_Item) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(4);
            message.writer().writeInt((int) pl.id);
            message.writer().writeShort(id_Aura);
            message.writer().writeByte(id_Eff_Set_Item);
            Service.gI().sendMessAllPlayerInMap(pl, message);
            message.cleanup();
        } catch (Exception e) {
        }
    }

    public String getCardRichInfo(RadarCard radar) {
        if (radar == null) {
            return "";
        }
        String source = switch (radar.Id) {
            case 828 -> "[Nguồn]: Đánh quái Khủng long (Trái Đất)";
            case 829 -> "[Nguồn]: Đánh quái Lợn lòi (Namếc)";
            case 830 -> "[Nguồn]: Đánh quái Quỷ đất (Xayda)";
            case 831 -> "[Nguồn]: Đánh Khủng long mẹ (Trái Đất)";
            case 832 -> "[Nguồn]: Đánh Lợn lòi mẹ (Namếc)";
            case 833 -> "[Nguồn]: Đánh Quỷ đất mẹ (Xayda)";
            case 834 -> "[Nguồn]: Đánh Thằn lằn bay (Trái Đất)";
            case 835 -> "[Nguồn]: Đánh Phi long (Namếc)";
            case 836 -> "[Nguồn]: Đánh Quỷ bay (Xayda)";
            case 837 -> "[Nguồn]: Quái Lính Độc Nhãn (Doanh Trại & BDKB)";
            case 838 -> "[Nguồn]: Quái Lính Độc Nhãn (Doanh Trại & BDKB)";
            case 839 -> "[Nguồn]: Quái Sói xám (Bản Đồ Kho Báu & Doanh Trại)";
            case 840 -> "[Nguồn]: Boss Trung Úy Trắng (Doanh Trại)";
            case 841 -> "[Nguồn]: Boss Ninja Áo Tím (Doanh Trại)";
            case 842 -> "[Nguồn]: Boss Trung Úy Xanh Lơ (Bản Đồ Kho Báu & Doanh Trại)";
            case 859 -> "[Nguồn]: Thắng Doanh Trại (NPC Độc Nhãn)";
            case 956 -> "[Nguồn]: Vòng Quay Thượng Đế";
            case 1278 -> "[Nguồn]: Đánh Khỉ Lông Đen (Núi khỉ đen)";
            case 1276 -> "[Nguồn]: Đánh Khỉ Lông Vàng (Núi khỉ vàng)";
            case 1277 -> "[Nguồn]: Jackpot Vòng Quay Thượng Đế";
            default -> "";
        };

        if (source.isEmpty()) {
            return radar.Info != null ? radar.Info : "";
        }
        return source;
    }

    public static boolean isCardMaxed(Player pl, short cardId) {
        if (pl == null || pl.Cards == null) {
            return false;
        }
        Card card = pl.Cards.stream().filter(c -> c != null && c.Id == cardId).findFirst().orElse(null);
        if (card == null) {
            return false;
        }
        RadarCard rTemplate = RadarService.gI().RADAR_TEMPLATE.stream()
                .filter(r -> r.Id == cardId).findFirst().orElse(null);
        int maxLevel = rTemplate != null ? rTemplate.getMaxLevel() : 2;
        return card.Level >= maxLevel || (card.Level == 1 && card.Amount >= card.MaxAmount && maxLevel <= 1);
    }

    public static boolean isCompletedDocNhanRequirement(Player pl) {
        if (pl == null || pl.Cards == null) {
            return false;
        }
        boolean hasLinhDocNhan = isCardMaxed(pl, (short) 837) || isCardMaxed(pl, (short) 838);
        boolean hasSoiXam = isCardMaxed(pl, (short) 839);
        boolean hasTrungUyTrang = isCardMaxed(pl, (short) 840);
        boolean hasNinjaAoTim = isCardMaxed(pl, (short) 841);
        boolean hasTrungUyXanhLo = isCardMaxed(pl, (short) 842);

        return hasLinhDocNhan && hasSoiXam && hasTrungUyTrang && hasNinjaAoTim && hasTrungUyXanhLo;
    }

    private static String getCardProgressStr(Player pl, short... cardIds) {
        int highestProgress = 0;
        boolean maxed = false;
        if (pl != null && pl.Cards != null) {
            for (short id : cardIds) {
                Card c = pl.Cards.stream().filter(card -> card != null && card.Id == id).findFirst().orElse(null);
                if (c != null) {
                    if (c.Level >= 2) {
                        maxed = true;
                        break;
                    }
                    int totalConsumed = c.Level * 60 + c.Amount;
                    if (totalConsumed >= 120) {
                        maxed = true;
                        break;
                    }
                    if (totalConsumed > highestProgress) {
                        highestProgress = totalConsumed;
                    }
                }
            }
        }
        if (maxed) {
            return "120/120 (Đã xong)";
        }
        return highestProgress + "/120";
    }

    public static String getDocNhanRequirementProgress(Player pl) {
        return "Cần dùng đủ 120 thẻ của 5 loại thẻ Doanh Trại để có thể sử dụng Thẻ Độc Nhãn:\n"
                + "• Lính Độc Nhãn: " + getCardProgressStr(pl, (short) 837, (short) 838) + "\n"
                + "• Sói Xám: " + getCardProgressStr(pl, (short) 839) + "\n"
                + "• Trung Úy Trắng: " + getCardProgressStr(pl, (short) 840) + "\n"
                + "• Ninja Áo Tím: " + getCardProgressStr(pl, (short) 841) + "\n"
                + "• Trung Úy Xanh Lơ: " + getCardProgressStr(pl, (short) 842);
    }
}
