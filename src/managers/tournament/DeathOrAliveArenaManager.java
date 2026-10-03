package managers.tournament;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import models.map.Zone;
import models.tournament.DeathOrAliveArena;
import server.Maintenance;
import utils.Util;

public class DeathOrAliveArenaManager implements Runnable {

    private static DeathOrAliveArenaManager instance;
    private volatile long lastUpdate;
    private static final List<DeathOrAliveArena> list = new CopyOnWriteArrayList<>();

    public static DeathOrAliveArenaManager gI() {
        if (instance == null) {
            instance = new DeathOrAliveArenaManager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long start = System.currentTimeMillis();
                update();
                long timeUpdate = System.currentTimeMillis() - start;
                if (1000 - timeUpdate > 0) {
                    Thread.sleep(1000 - timeUpdate);
                }
            } catch (InterruptedException ex) {
            }
        }
    }

    public void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            for (DeathOrAliveArena vdst : list) {
                if (vdst != null) {
                    try {
                        vdst.update();
                    } catch (Exception e) {
                        utils.Logger.logException(DeathOrAliveArenaManager.class, e, "Lỗi update VDST");
                    }
                }
            }
        }
    }

    public void add(DeathOrAliveArena vdst) {
        if (vdst != null && !list.contains(vdst)) {
            list.add(vdst);
        }
    }

    public void remove(DeathOrAliveArena vdst) {
        if (vdst != null) {
            list.remove(vdst);
        }
    }

    public DeathOrAliveArena getVDST(Zone zone) {
        if (zone == null) {
            return null;
        }
        for (DeathOrAliveArena vdst : list) {
            if (vdst != null && vdst.getZone() != null && vdst.getZone().equals(zone)) {
                return vdst;
            }
        }
        return null;
    }
}
