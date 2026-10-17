package tools;

import models.map.Map;
import models.map.Zone;
import models.phoban.RedRibbonHQ;
import server.Manager;
import services.phoban.RedRibbonHQService;
import managers.map.TileMapDataLoader;
import database.loaders.DatabaseTemplateLoader;
import database.AlyraManager;

public class TestMobRuntime {
    public static void main(String[] args) {
        try {
            System.out.println("Loading templates...");
            DatabaseTemplateLoader.gI().loadAllTemplates();
            System.out.println("Init TileMapDataLoader...");
            TileMapDataLoader.gI().initAllMaps();
            
            RedRibbonHQ dt0 = RedRibbonHQService.gI().doanhTrais.get(0);
            System.out.println("DoanhTrai 0 total zones: " + dt0.zones.size());
            for (Zone z : dt0.zones) {
                System.out.println("  Map " + z.map.mapId + " (" + z.map.mapName + "): mobs in zone = " + z.mobs.size() + ", mobs in map = " + (z.map.zones.isEmpty() ? 0 : z.map.zones.get(0).mobs.size()));
            }
            
            Zone z62 = dt0.getMapById(62);
            if (z62 != null) {
                System.out.println("Zone map 62 found! mobs count = " + z62.mobs.size());
            }

            // Test simulate player open Doanh Trai
            models.player.Player pl = new models.player.Player();
            pl.id = 1000;
            pl.name = "Tester";
            pl.clan = new models.clan.Clan();
            pl.clan.id = 1;
            pl.clan.name = "TestClan";
            pl.nPoint = new models.player.NPoint(pl);
            pl.nPoint.dame = 10000;
            pl.nPoint.hpMax = 50000;
            pl.clan.membersInGame.add(pl);
            pl.zone = z62;
            pl.location = new models.player.Location();
            pl.location.x = 1300;
            pl.location.y = 400;

            System.out.println("Opening Doanh Trai for player...");
            dt0.openDoanhTrai(pl);
            System.out.println("Doanh Trai opened. isOpened = " + dt0.isOpened);
            System.out.println("Bosses count: " + dt0.bosses.size());
            for (models.boss.Boss b : dt0.bosses) {
                System.out.println("  Boss: " + b.name + ", zoneMap: " + (b.zone != null ? b.zone.map.mapId : "null"));
            }
            System.out.println("Zone 62 mobs after init: count = " + z62.mobs.size());
            for (int i = 0; i < Math.min(5, z62.mobs.size()); i++) {
                models.mob.Mob m = z62.mobs.get(i);
                System.out.println("   mob " + i + ": id=" + m.id + ", tempId=" + m.tempId + ", hp=" + m.point.hp + ", maxHp=" + m.point.maxHp + ", status=" + m.status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
