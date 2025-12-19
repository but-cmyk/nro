package event;

import event.event_list.TopUp;
import event.event_list.TrungThu;
import event.event_list.HungVuong;
import event.event_list.Christmas;
import event.event_list.Halloween;
import event.event_list.LunarNewYear;
import event.event_list.Default;
import event.event_list.InternationalWomensDay;

public class EventManager {

    private static EventManager instance;

    public static boolean LUNNAR_NEW_YEAR = false;

    public static boolean INTERNATIONAL_WOMANS_DAY = false;

    public static boolean CHRISTMAS = false;

    public static boolean HALLOWEEN = false;

    public static boolean HUNG_VUONG = false;

    public static boolean TRUNG_THU = false;

    public static boolean TOP_UP = true;

    public static EventManager gI() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void init() {
        new Default().init();
        if (LUNNAR_NEW_YEAR) {
            new LunarNewYear().init();
        }
        if (INTERNATIONAL_WOMANS_DAY) {
            new InternationalWomensDay().init();
        }
        if (HALLOWEEN) {
            new Halloween().init();
        }
        if (CHRISTMAS) {
            new Christmas().init();
        }
        if (HUNG_VUONG) {
            new HungVuong().init();
        }
        if (TRUNG_THU) {
            new TrungThu().init();
        }
        if (TOP_UP) {
            new TopUp().init();
        }
    }
}
