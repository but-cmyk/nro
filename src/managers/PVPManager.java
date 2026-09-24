package managers;

import models.matches.PVP;
import models.player.Player;
import server.ServerManager;
import utils.Logger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PVPManager implements Runnable {

    private static PVPManager i;

    public static PVPManager gI() {
        if (i == null) {
            i = new PVPManager();
        }
        return i;
    }

    private final List<PVP> pvps;

    public PVPManager() {
        this.pvps = new CopyOnWriteArrayList<>();
        Thread pvpThread = new Thread(this, "Update-PVP-Worker");
        pvpThread.setDaemon(true);
        pvpThread.start();
    }

    public void removePVP(PVP pvp) {
        this.pvps.remove(pvp);
    }

    public void addPVP(PVP pvp) {
        this.pvps.add(pvp);
    }

    public PVP getPVP(Player player) {
        if (player == null) {
            return null;
        }
        for (PVP pvp : this.pvps) {
            if (pvp != null && (player.equals(pvp.p1) || player.equals(pvp.p2))) {
                return pvp;
            }
        }
        return null;
    }

    @Override
    public void run() {
        this.update();
    }

    private void update() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                for (PVP pvp : pvps) {
                    if (pvp != null) {
                        pvp.update();
                    }
                }
                long elapsed = System.currentTimeMillis() - st;
                long sleepTime = Math.max(50, 1000 - elapsed);
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Logger.logException(PVPManager.class, e, "Error in PVPManager update loop");
            }
        }
    }

}
