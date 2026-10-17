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
                Card card = cards.stream().filter(c -> c.Id == radar.Id).findFirst().orElse(null);
                if (card == null) {
                    card = new Card(radar.Max, radar.Options);
                }
                m.writer().writeShort(radar.Id);
                m.writer().writeShort(radar.IconId);
                m.writer().writeByte(radar.Rank);
                m.writer().writeByte(card.Amount);  //amount
                m.writer().writeByte(card.MaxAmount);  //max_amount
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
                m.writer().writeUTF(radar.Info);  //info
                m.writer().writeByte(card.Level);  //Level
                m.writer().writeByte(card.Used);  //use
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
}
