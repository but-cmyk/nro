package models.phoban;



import utils.Functions;
import java.util.ArrayList;
import java.util.List;
import models.map.Zone;
import models.map.MaBuHold;
import models.player.Player;
import server.Maintenance;
import services.map.MapService;
import services.map.ChangeMapService;
import utils.TimeUtil;

public final class MajinBuu14H {

    public static final int AVAILABLE = 7;
    public int id;
    public final List<Zone> zones;

    public List<Zone> getZones() {
        return this.zones;
    }

    public int getId() {
        return this.id;
    }

    public MajinBuu14H(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }



    public void update() {
        if (!TimeUtil.isMabu14HOpen()) {
            finish();
            return;
        }
        for (int j = zones.size() - 1; j >= 0; j--) {
            Zone zone = zones.get(j);
            if (zone != null && zone.maBuHolds != null) {
                synchronized (zone.maBuHolds) {
                    for (MaBuHold hold : zone.maBuHolds) {
                        if (hold != null && hold.player != null && hold.player.maBuHold == null && hold.player.zone != null) {
                            hold.player = null;
                        }
                    }
                }
            }
        }
    }

//    public MaBuHold getMaBuHold() {
//        for (Zone zone : this.zones) {
//            if (zone.map.mapId == 128) {
//                for (MaBuHold hold : zone.maBuHolds) {
//                    if (hold.player == null) {
//                        return hold;
//                    }
//                }
//            }
//        }
//        return null;
//    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    private void finish() {
        for (int j = zones.size() - 1; j >= 0; j--) {
            Zone zone = zones.get(j);
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOut(pl);
                }
            }
        }
    }

    private void kickOut(Player player) {
        if (MapService.gI().isMapMabu2H(player.zone.map.mapId) && !player.isAdmin()) {
            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 336);
        }
    }

}
