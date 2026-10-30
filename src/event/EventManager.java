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

    public synchronized boolean setEventState(String eventName, boolean active) {
        if (eventName == null) return false;
        switch (eventName.toUpperCase()) {
            case "LUNNAR_NEW_YEAR":
                LUNNAR_NEW_YEAR = active;
                if (active) new LunarNewYear().init();
                return true;
            case "INTERNATIONAL_WOMANS_DAY":
                INTERNATIONAL_WOMANS_DAY = active;
                if (active) new InternationalWomensDay().init();
                return true;
            case "CHRISTMAS":
                CHRISTMAS = active;
                if (active) new Christmas().init();
                return true;
            case "HALLOWEEN":
                HALLOWEEN = active;
                if (active) new Halloween().init();
                return true;
            case "HUNG_VUONG":
                HUNG_VUONG = active;
                if (active) new HungVuong().init();
                return true;
            case "TRUNG_THU":
                TRUNG_THU = active;
                if (active) new TrungThu().init();
                return true;
            case "TOP_UP":
                TOP_UP = active;
                if (active) new TopUp().init();
                return true;
            default:
                return false;
        }
    }

    public java.util.Map<String, Boolean> getAllEventStates() {
        java.util.Map<String, Boolean> map = new java.util.LinkedHashMap<>();
        map.put("LUNNAR_NEW_YEAR", LUNNAR_NEW_YEAR);
        map.put("INTERNATIONAL_WOMANS_DAY", INTERNATIONAL_WOMANS_DAY);
        map.put("CHRISTMAS", CHRISTMAS);
        map.put("HALLOWEEN", HALLOWEEN);
        map.put("HUNG_VUONG", HUNG_VUONG);
        map.put("TRUNG_THU", TRUNG_THU);
        map.put("TOP_UP", TOP_UP);
        return map;
    }
}
