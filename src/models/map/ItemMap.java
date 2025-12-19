
package models.map;

import consts.ConstItem;
import consts.ConstMap;
import java.util.ArrayList;
import java.util.List;
import models.Template.ItemTemplate;
import models.clan.Clan;
import models.item.Item.ItemOption;
import models.player.Pet;
import models.player.Player;
import utils.Util;
import services.map.ItemMapService;
import services.ItemService;
import services.map.MapService;
import services.player.PlayerService;
import services.Service;

public class ItemMap {

    // Constants
    private static final int MAX_ITEM_MAP_ID = 2000000000;
    private static final long ITEM_OWNERSHIP_TIMEOUT = 45000;
    private static final long ITEM_REMOVAL_TIMEOUT = 50000;
    private static final long EXTENDED_REMOVAL_TIMEOUT = 180000;
    private static final long BLACK_BALL_MOVE_INTERVAL = 10000;
    private static final long SPECIAL_ITEM_REMOVAL_TIMEOUT = 5000;
    
    private static final int SATELLITE_RANGE = 200;
    private static final int PLAYER_Y_OFFSET = 24;
    
    // Special map IDs
    private static final int[] PROTECTED_MAP_IDS = {21, 22, 23};
    private static final int DHVT_MAP_ID = 183;
    
    // Special item IDs  
    private static final int ITEM_78_ID = 78;
    private static final int ITEM_726_ID = 726;
    private static final int ITEM_673_ID = 673;
    private static final int ITEM_460_ID = 460;
    private static final int ITEM_992_ID = 992;
    private static final int SATELLITE_TYPE = 22;
    private static final long SPECIAL_PLAYER_ID = 123456789;
    
    // Satellite item IDs
    private static final int SATELLITE_MP_ID = 342;
    private static final int SATELLITE_INTELLIGENT_ID = 343;
    private static final int SATELLITE_DEFEND_ID = 344;
    private static final int SATELLITE_HP_ID = 345;

    // Instance variables
    public Zone zone;
    public int itemMapId;
    public ItemTemplate itemTemplate;
    public int quantity;
    public int x;
    public int y;
    public long playerId;
    public List<ItemOption> options;
    public long createTime;
    public int clanId = -1;
    public boolean isBlackBall;
    public boolean isNamecBall;
    public Clan clan;
    
    private long lastTimeMoveToPlayer;

    // Main constructor - others delegate to this one
    public ItemMap(Zone zone, ItemTemplate itemTemplate, int quantity, int x, int y, long playerId, Clan clan) {
        this.zone = zone;
        this.itemMapId = generateItemMapId(zone);
        this.itemTemplate = itemTemplate;
        this.quantity = quantity;
        this.x = x;
        this.y = y;
        this.playerId = playerId != -1 ? Math.abs(playerId) : playerId;
        this.createTime = System.currentTimeMillis();
        this.options = new ArrayList<>();
        this.clan = clan;
        
        initializeItemProperties();
        this.zone.addItem(this);
    }

    // Delegating constructors
    public ItemMap(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        this(zone, ItemService.gI().getTemplate((short) tempId), quantity, x, y, playerId, null);
    }

    public ItemMap(Zone zone, ItemTemplate temp, int quantity, int x, int y, long playerId) {
        this(zone, temp, quantity, x, y, playerId, null);
    }

    // Copy constructor
    public ItemMap(ItemMap itemMap) {
        this.zone = itemMap.zone;
        this.itemMapId = itemMap.itemMapId;
        this.itemTemplate = itemMap.itemTemplate;
        this.quantity = itemMap.quantity;
        this.x = itemMap.x;
        this.y = itemMap.y;
        this.playerId = itemMap.playerId;
        this.options = itemMap.options;
        this.isBlackBall = itemMap.isBlackBall;
        this.isNamecBall = itemMap.isNamecBall;
        this.lastTimeMoveToPlayer = itemMap.lastTimeMoveToPlayer;
        this.createTime = System.currentTimeMillis();
        this.zone.addItem(this);
    }

    private int generateItemMapId(Zone zone) {
        int id = zone.countItemAppeaerd++;
        if (zone.countItemAppeaerd >= MAX_ITEM_MAP_ID) {
            zone.countItemAppeaerd = 0;
        }
        return id;
    }

    private void initializeItemProperties() {
        this.isBlackBall = ItemMapService.gI().isBlackBall(this.itemTemplate.id);
        this.isNamecBall = ItemMapService.gI().isNamecBall(this.itemTemplate.id);
        this.lastTimeMoveToPlayer = System.currentTimeMillis();
    }

    public void update() {
        if (!isNotNullItem()) {
            return;
        }

        if (this.isBlackBall) {
            updateBlackBall();
            return;
        }

        updateItemOwnership();
        updateItemRemoval();
        updateSpecialItems();
    }

    private void updateBlackBall() {
        if (Util.canDoWithTime(lastTimeMoveToPlayer, BLACK_BALL_MOVE_INTERVAL)) {
            if (this.zone != null && !this.zone.getPlayers().isEmpty()) {
                Player player = this.zone.getPlayers().get(0);
                if (player.zone != null && player.zone.equals(this.zone)) {
                    moveToPlayer(player);
                }
            }
        }
    }

    private void moveToPlayer(Player player) {
        this.x = player.location.x;
        this.y = this.zone.map.yPhysicInTop(this.x, player.location.y - PLAYER_Y_OFFSET);
        reAppearItem();
        this.lastTimeMoveToPlayer = System.currentTimeMillis();
    }

    private void updateItemOwnership() {
        if (Util.canDoWithTime(createTime, ITEM_OWNERSHIP_TIMEOUT)) {
            if (shouldRemoveOwnership()) {
                this.playerId = -1;
            }
        }
    }

    private boolean shouldRemoveOwnership() {
        return this.itemTemplate.type != SATELLITE_TYPE 
                && this.itemTemplate.id != ITEM_726_ID 
                && this.itemTemplate.id != ITEM_992_ID;
    }

    private void updateItemRemoval() {
        boolean shouldRemove = false;
        
        // Standard removal timeout
        if (Util.canDoWithTime(createTime, ITEM_REMOVAL_TIMEOUT) 
                && isNotNullItem() 
                && itemTemplate.type != SATELLITE_TYPE) {
            shouldRemove = true;
        }
        
        // Extended removal timeout
        if (Util.canDoWithTime(createTime, EXTENDED_REMOVAL_TIMEOUT)) {
            shouldRemove = true;
        }

        if (shouldRemove && !this.isNamecBall && shouldItemBeRemoved()) {
            ItemMapService.gI().removeItemMapAndSendClient(this);
        }
    }

    private boolean shouldItemBeRemoved() {
        if (this.zone == null) {
            return true;
        }
        
        // Protected maps
        if (isInProtectedMap()) {
            return false;
        }
        
        // Protected items
        if (isProtectedItem()) {
            return false;
        }
        
        // Doanh trai items
        if (isDoanhtTraiItem()) {
            return false;
        }
        
        return true;
    }

    private boolean isInProtectedMap() {
        for (int mapId : PROTECTED_MAP_IDS) {
            if (this.zone.map.mapId == mapId) {
                return true;
            }
        }
        return false;
    }

    private boolean isProtectedItem() {
        return this.itemTemplate.id == ITEM_78_ID || this.itemTemplate.id == ITEM_726_ID;
    }

    private boolean isDoanhtTraiItem() {
        return MapService.gI().isMapDoanhTrai(this.zone.map.mapId) 
                && this.itemTemplate.id >= 14 
                && this.itemTemplate.id <= 20;
    }

    private void updateSpecialItems() {
        // DHVT item 673
        updateDHVTItem();
        
        // Special item 460
        updateSpecialItem460();
    }

    private void updateDHVTItem() {
        if (this.zone != null 
                && this.zone.map.mapId == DHVT_MAP_ID 
                && isNotNullItem() 
                && this.itemTemplate.id == ITEM_673_ID) {
            if (!findPlayerByID(this.playerId)) {
                ItemMapService.gI().removeItemMapAndSendClient(this);
            }
        }
    }

    private void updateSpecialItem460() {
        if (this.zone != null 
                && isNotNullItem() 
                && this.itemTemplate.id == ITEM_460_ID 
                && this.playerId == SPECIAL_PLAYER_ID 
                && Util.canDoWithTime(createTime, SPECIAL_ITEM_REMOVAL_TIMEOUT)) {
            ItemMapService.gI().removeItemMapAndSendClient(this);
        }
    }

    private boolean findPlayerByID(long id) {
        for (Player pl : this.zone.getPlayers()) {
            if (pl.id == id) {
                return true;
            }
        }
        return false;
    }

//    private void satelliteUpdate() {
//        for (Player pl : this.zone.getPlayers()) {
//            if (canUseSatellite(pl)) {
//                processSatelliteEffect(pl);
//            }
//        }
//    }

//    private boolean canUseSatellite(Player player) {
//        return !player.isDie()
//                && Util.getDistance(player.location.x, player.location.y, x, y) < SATELLITE_RANGE
//                && player.satellite != null
//                && (player.id == this.playerId || isPlayerInClan(player));
//    }

    private boolean isPlayerInClan(Player player) {
        return this.clanId != -1 
                && player.clan != null 
                && player.clan.id == this.clanId;
    }

//    private void processSatelliteEffect(Player player) {
//        switch (this.itemTemplate.id) {
//            case SATELLITE_MP_ID -> processMPSatellite(player);
//            case SATELLITE_INTELLIGENT_ID -> processIntelligentSatellite(player);
//            case SATELLITE_DEFEND_ID -> processDefendSatellite(player);
//            case SATELLITE_HP_ID -> processHPSatellite(player);
//        }
//    }

    private void processMPSatellite(Player player) {
        if (!player.satellite.isMP) {
            player.satellite.isMP = true;
            player.satellite.lastMPTime = System.currentTimeMillis();
            if (player.nPoint.mp < player.nPoint.mpMax) {
                player.nPoint.addMp(player.nPoint.mpMax / 10);
                PlayerService.gI().sendInfoMp(player);
            }
        }
    }

//    private void processIntelligentSatellite(Player player) {
//        if (!player.satellite.isIntelligent) {
//            player.satellite.isIntelligent = true;
//            player.satellite.lastIntelligentTime = System.currentTimeMillis();
//        }
//    }
//
//    private void processDefendSatellite(Player player) {
//        if (!player.satellite.isDefend) {
//            player.satellite.isDefend = true;
//            player.satellite.lastDefendTime = System.currentTimeMillis();
//        }
//    }
//
//    private void processHPSatellite(Player player) {
//        if (!player.satellite.isHP) {
//            player.satellite.isHP = true;
//            player.satellite.lastHPTime = System.currentTimeMillis();
//            if (player.nPoint.hp < player.nPoint.hpMax) {
//                player.nPoint.addHp(player.nPoint.hpMax / 10);
//                PlayerService.gI().sendInfoHp(player);
//                Service.gI().Send_Info_NV(player);
//            }
//        }
//    }

    private void reAppearItem() {
        ItemMapService.gI().sendItemMapDisappear(this);
        Service.gI().dropItemMap(this.zone, this);
    }

    public boolean isNotNullItem() {
        return itemTemplate != null;
    }

    public void dispose() {
        this.zone = null;
        this.itemTemplate = null;
        this.options = null;
    }

    public boolean isBelongToMe(Player player) {
        try {
            if (player == null) {
                return false;
            }
            
            // Check if item belongs to player or their master/pet
            if (this.playerId == Math.abs(player.id)) {
                return true;
            }
            
            // Check clan ownership
            if (this.clan != null) {
                if (isPlayerInSameClan(player)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isPlayerInSameClan(Player player) {
        // Direct clan check
        if (player.clan != null && this.clan.id == player.clan.id) {
            return true;
        }
        
        // Pet's master clan check
        if (player.isPet) {
            Pet pet = (Pet) player;
            if (pet.master.clan != null && pet.master.clan.id == this.clan.id) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean isInVeTinhRange(Player pl, int code, int locationX, int locationY) {
        if (pl == null) {
            return false;
        }
        
        ItemMap itemMap = getVeTinhItem(pl, code);
        if (itemMap == null || itemMap.itemTemplate == null) {
            return false;
        }

        boolean isInRange = Util.myGetDistance(itemMap.x, itemMap.y, locationX, locationY) <= ConstMap.RANGE_VE_TINH;
        return !pl.isDie() && isInRange;
    }

    private static ItemMap getVeTinhItem(Player player, int code) {
        switch (code) {
            case ConstItem.VE_TINH_TRI_LUC:
                return player.veTinhTriLuc;
            case ConstItem.VE_TINH_TRI_TUE:
                return player.veTinhTriTue;
            case ConstItem.VE_TINH_PHONG_THU:
                return player.veTinhPhongThu;
            case ConstItem.VE_TINH_SINH_LUC:
                return player.veTinhSinhLuc;
            default:
                return null;
        }
    }
}