package models.map;


import consts.ConstTask;
import models.boss.Boss;
import consts.BossID;
import models.item.Item;
import models.mob.Mob;
import models.npc.Npc;
import services.map.NpcManager;
import models.player.Player;
import network.io.Message;
import models.boss.boss_list.Training.TrainingBoss;
import consts.ConstMob;
import services.map.ItemMapService;
import services.ItemService;
import services.map.MapService;
import services.player.PlayerService;
import services.Service;
import services.TaskService;
import services.player.InventoryService;
import utils.FileIO;
import utils.Logger;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import models.map.EffectMap;
import models.map.vetinh.Satellite;
import models.npc.NonInteractiveNPC;

public class Zone {

    public static final byte PLAYERS_TIEU_CHUAN_TRONG_MAP = 7;

    public int countItemAppeaerd = 0;
    public long lastTimeUpdateEmpty = 0;

    public Map map;
    public int zoneId;
    public int maxPlayer;
    public int shenronType = -1;

    public List<Player> getNonInteractiveNPCs() { return this.nonInteractiveNPCs; }
    public List<Player> getHumanoids() { return this.humanoids; }
    public List<Player> getNotBosses() { return this.notBosses; }
    public List<Player> getPlayers() { return this.players; }
    public List<Player> getBosses() { return this.bosses; }
    public List<Player> getPets() { return this.pets; }

    private final List<Player> nonInteractiveNPCs; //npc
    private final List<Player> humanoids; //player, boss, pet
    private final List<Player> notBosses; //player, pet
    private final List<Player> players; //player
    private final List<Player> bosses; //boss
    private final List<Player> pets; //pet

    public final List<Mob> mobs;
    public final List<ItemMap> items;

    public long lastTimeDropBlackBall;
    public boolean finishBlackBallWar;
//    public boolean finishMapMaBu;

    public boolean isbulon1Alive = true;
    public boolean isbulon2Alive = true;
    public boolean isTUTAlive = true;
    public boolean isGoldenFriezaAlive;

    public boolean isKhongTacAlive = true;

    public boolean isCompeting;
    public String rankName1;
    public String rankName2;
    public int rank1;
    public int rank2;

    public List<TrapMap> trapMaps;
    public List<MaBuHold> maBuHolds;
    public boolean finishMap22h;

    public Player Npc;

    public Player getNpc() { return this.Npc; }
    public void setNpc(Player Npc) { this.Npc = Npc; }

    public boolean isFullPlayer() {
        return this.players.size() >= this.maxPlayer;
    }

    // Phương thức lấy danh sách các item trong zone
    public List<ItemMap> getItemsInZone() {
        return this.items;
    }

    public long getTotalHP() {
        long total = 0;
        synchronized (mobs) {
            for (Mob mob : mobs) {
                if (!mob.isDie()) {
                    total += mob.point.hp;
                }
            }
        }
        synchronized (players) {
            for (Player pl : players) {
                if (pl.nPoint != null && !pl.isDie()) {
                    total += pl.nPoint.hp;
                }
            }
        }
        synchronized (pets) {
            for (Player pl : pets) {
                if (pl.nPoint != null && !pl.isDie()) {
                    total += pl.nPoint.hp;
                }
            }
        }
        return total;
    }

    private void udMob() {
        for (int i = this.mobs.size() - 1; i >= 0; i--) {
            try {
                mobs.get(i).update();
            } catch (Exception e) {
                Logger.logException(Zone.class, e, "Lỗi update mobs");
            }
        }
    }

    private void udNonInteractiveNPC() {
        if (this.nonInteractiveNPCs.isEmpty()) {
            return;
        }
        try {
            for (int i = this.nonInteractiveNPCs.size() - 1; i >= 0; i--) {
                if (i < this.nonInteractiveNPCs.size()) {
                    Player pl = this.nonInteractiveNPCs.get(i);
                    if (pl != null && pl.zone != null) {
                        pl.update();
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Lỗi update npcs");
        }
    }

    private void udItem() {
        if (this.items.isEmpty()) {
            return;
        }
        try {
            synchronized (this.items) {
                for (int i = this.items.size() - 1; i >= 0; i--) {
                    try {
                        if (i < this.items.size()) {
                            ItemMap item = this.items.get(i);
                            if (item != null && item.itemTemplate != null) {
                                item.update();
                            } else {
                                items.remove(i);
                                System.err.println("Remove item " + i);
                            }
                        }
                    } catch (Exception e) {
                        Logger.logException(Zone.class, e, "Lỗi item");
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Lỗi update items");
        }
    }

    private void udPlayer() {
        for (int i = this.notBosses.size() - 1; i >= 0; i--) {
            Player pl = this.notBosses.get(i);
            if (!pl.isPet && !pl.isBot && !pl.isNewPet) {
                this.notBosses.get(i).update();
            }
        }
    }

    public void update() {
        udMob();
        udItem();
        udNonInteractiveNPC();
    }

    public Zone(Map map, int zoneId, int maxPlayer) {
        this.map = map;
        this.zoneId = zoneId;
        this.maxPlayer = maxPlayer;
        this.nonInteractiveNPCs = new CopyOnWriteArrayList<>();
        this.humanoids = new CopyOnWriteArrayList<>();
        this.notBosses = new CopyOnWriteArrayList<>();
        this.players = new CopyOnWriteArrayList<>();
        this.bosses = new CopyOnWriteArrayList<>();
        this.pets = new CopyOnWriteArrayList<>();
        this.mobs = new ArrayList<>();
        this.items = new ArrayList<>();
        this.trapMaps = new ArrayList<>();
        this.maBuHolds = new ArrayList<>();
    }

    public int getNumOfPlayers() {
        return this.players.size();
    }

    public int getNumOfBosses() {
        return this.bosses.size();
    }

    public boolean isBossCanJoin(Boss boss) {
        for (Player b : this.bosses) {
            if (b.id == boss.id) {
                return false;
            }
        }
        return true;
    }

    public void addPlayer(Player player) {
        if (player != null) {
            if (!this.humanoids.contains(player)) {
                this.humanoids.add(player);
            }

            if (player instanceof NonInteractiveNPC) {
                this.nonInteractiveNPCs.add(player);
            }

            if (!player.isBoss && !this.notBosses.contains(player) && !player.isNewPet && !(player instanceof NonInteractiveNPC)) {
                this.notBosses.add(player);
            }

            if (!player.isBoss && !player.isNewPet && !player.isPet && !this.players.contains(player) && !(player instanceof NonInteractiveNPC)) {
                this.players.add(player);
            }

            if (player.isBoss) {
                this.bosses.add(player);
            }
            if (player.isPet || player.isNewPet) {
                this.pets.add(player);
            }

        }
    }

    public void removePlayer(Player player) {
        this.nonInteractiveNPCs.remove(player);
        this.humanoids.remove(player);
        this.notBosses.remove(player);
        this.players.remove(player);
        this.bosses.remove(player);
        this.pets.remove(player);
    }

    public List<ItemMap> getSatellites() {
        return items.stream().filter(i -> i instanceof Satellite).toList();
    }

    public ItemMap getItemMapByItemMapId(int itemId) {
        synchronized (this.items) {
            for (ItemMap item : this.items) {
                if (item != null && item.itemMapId == itemId) {
                    return item;
                }
            }
            return null;
        }
    }


public ItemMap getItemMapByTempId(int tempId) {
    synchronized (this.items) {
        for (ItemMap item : this.items) {
            if (item.itemTemplate.id == tempId) {
                return item;
            }
        }
        return null;
    }
}

    public List<ItemMap> getItemMapsForPlayer(Player player) {
        List<ItemMap> list = new ArrayList<>();
        synchronized (this.items) {
            for (ItemMap item : items) {
                if (item == null || item.itemTemplate == null) {
                    continue;
                }
                if (item.itemTemplate.id == 78) {
                    if (TaskService.gI().getIdTask(player) != ConstTask.TASK_3_1) {
                        continue;
                    }
                }
                if (item.itemTemplate.id == 74) {
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_3_0) {
                        continue;
                    }
                }
                if (item.itemTemplate.id == 726 && item.playerId != player.id) {
                    continue;
                }
                list.add(item);
            }
        }
        return list;
    }

    public Player getPlayerInMap(long idPlayer) {
        for (Player pl : humanoids) {
            if (pl != null && pl.id == idPlayer) {
                return pl;
            }
        }
        return null;
    }

    public Player getPlayerInMapOffline(Player player, long idPlayer) {
        for (Player pl : bosses) {
            if (pl.id == idPlayer && pl instanceof TrainingBoss && ((TrainingBoss) pl).playerAtt.equals(player)) {
                return pl;
            }
        }
        return null;
    }

    public void pickItem(Player player, int itemMapId) {
        pickItem(player, itemMapId, false);
    }

    public void pickItem(Player player, int itemMapId, boolean isThuHut) {
        if (player.isTrade || services.func.TransactionService.gI().check(player)) {
            Service.gI().sendThongBao(player, "Không thể nhặt vật phẩm khi đang giao dịch!");
            return;
        }
        ItemMap itemMap = null;
        boolean isFoodOrKid = false;

        synchronized (this.items) {
            itemMap = getItemMapByItemMapId(itemMapId);
            if (itemMap == null || itemMap.itemTemplate == null) {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
                return;
            }
            if (!isThuHut && player.location != null && utils.Util.getDistance(player.location.x, player.location.y, itemMap.x, itemMap.y) > 120) {
                Service.gI().sendThongBao(player, "Khoảng cách quá xa!");
                return;
            }
            if (itemMap.itemTemplate.type == 22 || (itemMap.itemTemplate.id == 460 && itemMap.playerId == player.id)) {
                return;
            }
            int playerId = Math.abs(itemMap.playerId > 100_000_000 ? 1_000_000_000 - (int) itemMap.playerId : (int) itemMap.playerId);
            if (playerId != player.id && itemMap.playerId != player.id && itemMap.playerId != -1) {
                Service.gI().sendThongBao(player, "Không thể nhặt vật phẩm của người khác");
                return;
            }

            isFoodOrKid = (this.map.mapId >= 21 && this.map.mapId <= 23 && itemMap.itemTemplate.id == 74)
                    || (this.map.mapId >= 42 && this.map.mapId <= 44 && itemMap.itemTemplate.id == 78);

            // Kiểm tra trước ô trống hành trang đối với trang bị/vật phẩm thông thường để chống mất đồ
            int itemType = itemMap.itemTemplate.type;
            boolean isCurrency = (itemType == 9 || itemType == 10 || itemType == 34);
            boolean isBall = ItemMapService.gI().isBlackBall(itemMap.itemTemplate.id)
                    || ItemMapService.gI().isNamecBall(itemMap.itemTemplate.id)
                    || ItemMapService.gI().isNamecBallStone(itemMap.itemTemplate.id);

            if (!isFoodOrKid && !isCurrency && !isBall) {
                if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                    Item existingItem = InventoryService.gI().findItemBagByTemp(player, itemMap.itemTemplate.id);
                    if (existingItem == null || !itemMap.itemTemplate.isUpToUp) {
                        Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống, không thể nhặt thêm");
                        return;
                    }
                }
            }

            if (!isFoodOrKid) {
                this.items.remove(itemMap);
            }
        }

        Item item = ItemService.gI().createItemFromItemMap(itemMap);
        if (item.template.id == 648) {
            if (!InventoryService.gI().findItemTatVoGiangSinh(player)) {
                if (!isFoodOrKid) {
                    synchronized (this.items) {
                        if (!this.items.contains(itemMap)) {
                            this.items.add(0, itemMap);
                        }
                    }
                }
                Service.gI().sendThongBao(player, "Cần thêm Tất,vớ giáng sinh");
                return;
            }
        }

        if (InventoryService.gI().addItemBag(player, item)) {
            int itemType = item.template.type;
            Message msg;
            try {
                msg = new Message(-20);
                msg.writer().writeShort(itemMapId);
                switch (itemType) {
                    case 9, 10, 34 -> {
                        msg.writer().writeUTF(item.quantity > Short.MAX_VALUE ? "Bạn vừa nhận được " + Util.formatNumber(item.quantity) + " " + item.template.name : "");
                        PlayerService.gI().sendInfoHpMpMoney(player);
                    }
                    default -> {
                        switch (item.template.id) {
                            case 73 ->
                                msg.writer().writeUTF("");
                            case 74 ->
                                msg.writer().writeUTF("Bạn mới vừa ăn " + item.template.name);
                            case 78 ->
                                msg.writer().writeUTF("Wow, một cậu bé dễ thương!");
                            default -> {
                                if (item.template.type >= 0 && item.template.type < 5) {
                                    msg.writer().writeUTF(item.template.name);
                                } else {
                                    msg.writer().writeUTF("Bạn nhận được " + item.template.name);
                                }
                                if (item.template.id == 648) {
                                    InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, 649), 1);
                                }
                                InventoryService.gI().sendItemBags(player);
                                player.effect.addPointOngThanVeChai();
                            }
                        }
                    }
                }
                msg.writer().writeShort(item.quantity > Short.MAX_VALUE ? 9999 : item.quantity);
                player.sendMessage(msg);
                msg.cleanup();
                Service.gI().sendToAntherMePickItem(player, itemMapId);
                if (!isFoodOrKid) {
                    itemMap.dispose();
                }
            } catch (Exception e) {
                Logger.logException(Zone.class, e);
            }
        } else {
            if (!isFoodOrKid) {
                synchronized (this.items) {
                    if (!this.items.contains(itemMap)) {
                        this.items.add(0, itemMap);
                    }
                }
            }
            if (!ItemMapService.gI().isBlackBall(item.template.id) && !ItemMapService.gI().isNamecBall(item.template.id) && !ItemMapService.gI().isNamecBallStone(item.template.id)) {
                String text = "Hành trang không còn chỗ trống, không thể nhặt thêm";
                Service.gI().sendThongBao(player, text);
                return;
            }
        }
        TaskService.gI().checkDoneTaskPickItem(player, itemMap);
        TaskService.gI().checkDoneSideTaskPickItem(player, itemMap);
        TaskService.gI().checkDoneClanTaskPickItem(player, itemMap);
    }

    public void addItem(ItemMap itemMap) {
        if (itemMap != null) {
            synchronized (this.items) {
                if (!items.contains(itemMap)) {
                    items.add(0, itemMap);
                }
            }
        }
    }

    public void removeItemMap(ItemMap itemMap) {
        synchronized (this.items) {
            this.items.remove(itemMap);
        }
    }

    public Player getRandomPlayerInMap() {
        List<Player> plNotVoHinh = new ArrayList<Player>();
        //Lỗi
        for (Player pl : this.notBosses) {
            if (pl != null && (pl.effectSkin == null || !pl.effectSkin.isVoHinh) && pl.maBuHold == null && !pl.isMabuHold) {
                plNotVoHinh.add(pl);
            }
        }
        if (!plNotVoHinh.isEmpty()) {
            return plNotVoHinh.get(Util.nextInt(0, plNotVoHinh.size() - 1));
        }
        return null;
    }

    public void load_Me_To_Another(Player player) { //load thông tin người chơi cho những người chơi khác
        try {
            if (player.zone != null) {
                if (MapService.gI().isMapOffline(this.map.mapId)) {
                    //Load boss
                    if (player instanceof TrainingBoss || player instanceof NonInteractiveNPC) {
                        for (int i = players.size() - 1; i >= 0; i--) {
                            Player pl = players.get(i);
                            if (!player.equals(pl) && (player instanceof NonInteractiveNPC
                                    || player instanceof TrainingBoss && ((TrainingBoss) player).playerAtt.equals(pl))) {
                                infoPlayer(pl, player);
                            }
                        }
                    }
                } else {
                    for (int i = players.size() - 1; i >= 0; i--) {
                        Player pl = players.get(i);
                        if (!player.equals(pl)) {
                            infoPlayer(pl, player);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(MapService.class, e);
        }
    }

    public void load_Another_To_Me(Player player) { //load những player trong map và gửi cho player vào map
        try {
            if (MapService.gI().isMapOffline(this.map.mapId)) {
                //Load boss
                for (int i = this.humanoids.size() - 1; i >= 0; i--) {
                    Player pl = this.humanoids.get(i);
                    if (pl != null && (pl instanceof NonInteractiveNPC
                            || pl instanceof TrainingBoss && ((TrainingBoss) pl).playerAtt.equals(player))) {
                        infoPlayer(player, pl);
                    }
                }
            } else {
                for (int i = this.humanoids.size() - 1; i >= 0; i--) {
                    Player pl = this.humanoids.get(i);
                    if (pl != null && !player.equals(pl)) {
                        infoPlayer(player, pl);
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(MapService.class, e);
        }
    }

//    public void loadBoss(Boss boss) {
//        try {
//            if (MapService.gI().isMapOffline(this.map.mapId)) {
//                //Load boss
//                for (Player pl : this.bosses) {
//                    if (!boss.equals(pl) && !pl.isPl() && !pl.isPet && !pl.isNewPet && !pl.isBot) {
//                        infoPlayer(boss, pl);
//                        infoPlayer(pl, boss);
//                    }
//                }
//            } else {
//                for (Player pl : this.bosses) {
//                    if (!boss.equals(pl)) {
//                        infoPlayer(boss, pl);
//                        infoPlayer(pl, boss);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Logger.logException(MapService.class, e);
//        }
//    }

    private void infoPlayer(Player plReceive, Player plInfo) {
        Message msg;
        try {
            msg = new Message(-5);
            msg.writer().writeInt((int) plInfo.id);
            if (plInfo.clan != null) {
                msg.writer().writeInt(plInfo.clan.id);
            } else if (plInfo.isBoss && (plInfo.id == BossID.MABU || plInfo.id == BossID.SUPERBU)) {
                msg.writer().writeInt(-100);
            } else if (plInfo.isCopy) {
                msg.writer().writeInt(-2);
            } else {
                msg.writer().writeInt(-1);
            }
            msg.writer().writeByte(Service.gI().getCurrLevel(plInfo));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(plInfo.typePk);
            msg.writer().writeByte(plInfo.gender);
            msg.writer().writeByte(plInfo.gender);
            msg.writer().writeShort(plInfo.getHead());
            msg.writer().writeUTF(Service.gI().name(plInfo));
            msg.writeFix(plInfo.nPoint != null ? Util.maxIntValue(plInfo.nPoint.hp) : 100);
            msg.writeFix(plInfo.nPoint != null ? Util.maxIntValue(plInfo.nPoint.hpMax) : 100);
            msg.writer().writeShort(plInfo.getBody());
            msg.writer().writeShort(plInfo.getLeg());
            int flagbag = plInfo.getFlagBag();
            if (plReceive.isPl() && plReceive.getSession() != null && plReceive.getSession().version >= 228) {
                switch (flagbag) {
                    case 83:
                        flagbag = 250;
                        break;
                }
            }
            msg.writer().writeByte(flagbag); //bag
            msg.writer().writeByte(-1);
//            msg.writer().writeShort(plInfo.location.x);
//            msg.writer().writeShort(plInfo.location.y);

msg.writer().writeShort(plInfo.location != null ? plInfo.location.x : 0);
msg.writer().writeShort(plInfo.location != null ? plInfo.location.y : 0);

            msg.writer().writeShort(0); // effbuffhp
            msg.writer().writeShort(0); // effbuffmp

            msg.writer().writeByte(0); // num eff

            //byte templateId, int timeStart, int timeLenght, short param
//            msg.writer().writeByte(plInfo.idMark.getIdSpaceShip());

msg.writer().writeByte(plInfo.idMark != null ? plInfo.idMark.getIdSpaceShip() : -1);


            msg.writer().writeByte(plInfo.effectSkill != null && plInfo.effectSkill.isMonkey ? 1 : 0);
            msg.writer().writeShort(plInfo.getMount());
            msg.writer().writeByte(plInfo.cFlag);

            msg.writer().writeByte(0);
            msg.writer().writeShort(plInfo.getAura()); //idauraeff
            msg.writer().writeByte(plInfo.getEffFront()); //seteff
            msg.writer().writeShort(plInfo.getHat()); //id hat

            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Service.gI().sendFlagPlayerToMe(plReceive, plInfo);
        try {
            if (plInfo.isPl()) {
                if (plInfo.effectSkill != null && plInfo.effectSkill.isChibi) {
                    Service.gI().sendChibiFollowToMe(plReceive, plInfo);
                } else {
//                    Service.gI().sendPetFollowToMe(plReceive, plInfo);
                }
            }
        } catch (Exception e) {
        }

        try {
            if (plInfo.isDie()) {
                msg = new Message(-8);
                msg.writer().writeInt((int) plInfo.id);
                msg.writer().writeByte(0);
                msg.writer().writeShort(plInfo.location.x);
                msg.writer().writeShort(plInfo.location.y);
                plReceive.sendMessage(msg);
                msg.cleanup();
            }
        } catch (Exception e) {

        }
    }

    public void mapInfo(Player pl) {
        Message msg;
        try {
            msg = new Message(-24);
            msg.writer().writeByte(this.map.mapId);
            msg.writer().writeByte(this.map.planetId);
            msg.writer().writeByte(this.map.tileId);
            msg.writer().writeByte(this.map.bgId);
            msg.writer().writeByte(this.map.type);
            msg.writer().writeUTF(this.map.mapName);
            msg.writer().writeByte(this.zoneId);

            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);

            // waypoint
            try {
                List<WayPoint> wayPoints = this.map.wayPoints;
                msg.writer().writeByte(wayPoints.size());
                for (WayPoint wp : wayPoints) {
                    msg.writer().writeShort(wp.minX);
                    msg.writer().writeShort(wp.minY);
                    msg.writer().writeShort(wp.maxX);
                    msg.writer().writeShort(wp.maxY);
                    msg.writer().writeBoolean(wp.isEnter);
                    msg.writer().writeBoolean(wp.isOffline);
                    msg.writer().writeUTF(wp.name);
                }
            } catch (Exception e) {
                msg.writer().writeByte(0);
            }

            // mob
            try {
                msg.writer().writeByte(this.mobs.size());
                for (Mob mob : this.mobs) {
                    msg.writer().writeBoolean(false); //is disable
                    msg.writer().writeBoolean(false); //is dont move
                    msg.writer().writeBoolean(false); //is fire
                    msg.writer().writeBoolean(false); //is ice
                    msg.writer().writeBoolean(false); //is wind
                    msg.writer().writeByte(mob.tempId);
                    msg.writer().writeByte(0); // sys
                    msg.writer().writeInt(mob.point.gethp());
                    msg.writer().writeByte(mob.level);
                    msg.writer().writeInt((mob.point.getHpFull()));
                    msg.writer().writeShort(mob.location.x);
                    msg.writer().writeShort(mob.location.y);
                    msg.writer().writeByte(mob.status);
                    msg.writer().writeByte(mob.lvMob);
                    msg.writer().writeBoolean(mob.tempId == ConstMob.GAU_TUONG_CUOP || mob.tempId >= ConstMob.VOI_CHIN_NGA && mob.tempId <= ConstMob.PIANO); //is bigboss
                }
            } catch (Exception e) {
                msg.writer().writeByte(0);
            }

            msg.writer().writeByte(0);

            // npc
            try {
                List<Npc> npcs = NpcManager.getNpcsByMapPlayer(pl);
                msg.writer().writeByte(npcs.size());
                for (Npc npc : npcs) {
                    msg.writer().writeByte(npc.status);
                    msg.writer().writeShort(npc.cx);
                    msg.writer().writeShort(npc.cy);
                    msg.writer().writeByte(npc.tempId);
                    msg.writer().writeShort(npc.avartar);
                }
            } catch (Exception e) {
                msg.writer().writeByte(0);
            }

            // item
            try {
                List<ItemMap> itemsMap = this.getItemMapsForPlayer(pl);
                int size = Math.min(itemsMap.size(), 100);
                msg.writer().writeByte(size);
                for (int i = 0; i < size; i++) {
                    ItemMap it = itemsMap.get(i);
                    msg.writer().writeShort(it.itemMapId);
                    msg.writer().writeShort(it.itemTemplate.id);
                    msg.writer().writeShort(it.x);
                    msg.writer().writeShort(it.y);
                    msg.writer().writeInt((int) it.playerId);
                    if (it.playerId == -2) {
                        msg.writer().writeShort((short) (it instanceof Satellite satellite ? satellite.range : 250));
                    }
                }
            } catch (Exception e) {
                msg.writer().writeByte(0);
            }

            // bg item
            try {
                byte[] bgItem = this.map.bgItemData != null ? this.map.bgItemData : managers.map.MapDataManager.gI().getBgItemData(this.map.mapId);
                if (bgItem != null && bgItem.length > 0) {
                    msg.writer().write(bgItem);
                } else {
                    msg.writer().writeShort(0);
                }
            } catch (Exception e) {
                msg.writer().writeShort(0);
            }

            // eff item
//            try {
//                final byte[] effItem = FileIO.readFile("data/map/eff_map/" + this.map.mapId);
//                msg.writer().write(effItem);
//            } catch (Exception e) {
//                msg.writer().writeShort(0);
//            }
            // eff item
            List<EffectMap> em = this.map.effMap;
            msg.writer().writeShort(em.size());
            for (EffectMap e : em) {
                msg.writer().writeUTF(e.getKey());
                msg.writer().writeUTF(e.getValue());
            }
            msg.writer().writeByte(this.map.bgType);
            msg.writer().writeByte(pl.idMark.getIdSpaceShip());
            msg.writer().writeByte(this.map.mapId == 148 ? 1 : 0);
            pl.sendMessage(msg);
            msg.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public TrapMap isInTrap(Player player) {
        for (TrapMap trap : this.trapMaps) {
            if (player.location.x >= trap.x && player.location.x <= trap.x + trap.w
                    && player.location.y >= trap.y && player.location.y <= trap.y + trap.h) {
                return trap;
            }
        }
        return null;
    }

    public void sendBigBoss(Player player) {
        for (Mob mob : this.mobs) {
            if (!mob.isDie() && mob.tempId == ConstMob.HIRUDEGARN) {
                if (mob.lvMob >= 1) {
                    Service.gI().sendBigBoss2(player, 6, mob);
                }
                if (mob.lvMob >= 2) {
                    Service.gI().sendBigBoss2(player, 5, mob);
                }
                break;
            }
        }
    }

    public MaBuHold getMaBuHold() {
        var map128 = MapService.gI().getMapById(128);
        if (map128 == null || this.zoneId < 0 || this.zoneId >= map128.zones.size()) {
            return null;
        }
        Zone z128 = map128.zones.get(this.zoneId);
        if (z128 == null || z128.maBuHolds == null) {
            return null;
        }
        synchronized (z128.maBuHolds) {
            for (MaBuHold hold : z128.maBuHolds) {
                if (hold != null && hold.player == null) {
                    return hold;
                }
            }
        }
        return null;
    }

    public void setMaBuHold(int slot, int zoneId, Player player) {
        var map128 = MapService.gI().getMapById(128);
        if (map128 == null || zoneId < 0 || zoneId >= map128.zones.size()) {
            return;
        }
        Zone z128 = map128.zones.get(zoneId);
        if (z128 == null || z128.maBuHolds == null) {
            return;
        }
        synchronized (z128.maBuHolds) {
            if (slot >= 0 && slot < z128.maBuHolds.size()) {
                z128.maBuHolds.set(slot, new MaBuHold(slot, player));
            }
        }
    }

    public boolean isKhongCoTrongTaiTrongKhu() {
        boolean kovao = true;
        for (Player pl : players) {
            if (!pl.isPl()) {
                kovao = false;
                break;
            }
            if (pl.zone == null || pl.zone.map == null) {
                continue;
            }
            if ((pl.zone.map.mapId >= 21 && pl.zone.map.mapId <= 23)
                    || pl.zone.map.mapId == 170
                    || pl.zone.map.mapId == 153
                    || pl.zone.map.mapId == 183
                    || pl.zone.map.mapId == 113
                    || pl.zone.map.mapId == 129
                    || MapService.gI().isMapDoanhTrai(pl.zone.map.mapId)
                    || MapService.gI().isMapBlackBallWar(pl.zone.map.mapId)
                    || MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)
                    || MapService.gI().isMapKhiGasHuyDiet(pl.zone.map.mapId)
                    || MapService.gI().isMapMaBu(pl.zone.map.mapId)
                    || MapService.gI().isMapOffline(pl.zone.map.mapId)) {
                kovao = false;
            }
        }
        return kovao;
    }
    // Đặt phương thức này vào trong file Zone.java của bạn
public void removeBoss(Boss boss) {
    if (boss != null) {
        this.bosses.remove(boss);
        this.humanoids.remove(boss); // Boss cũng là một humanoid, cần xóa ở đây nữa
    }
}
public void sendMessage(Message msg) {
    if (msg == null) {
        return;
    }
    // this.players là CopyOnWriteArrayList, duyệt trực tiếp thread-safe, loại bỏ cấp phát rác GC
    for (Player pl : this.players) {
        if (pl != null && pl.getSession() != null) {
            pl.sendMessage(msg);
        }
    }
}

}
