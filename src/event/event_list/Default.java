package event.event_list;
import consts.BossID;
import event.Event;

public class Default extends Event {

    @Override
    public void boss() {
        createBoss(BossID.BROLY, 50);
    }

}
