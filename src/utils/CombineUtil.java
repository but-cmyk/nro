package utils;
import models.item.Item;

public class CombineUtil {

    public static boolean isTrangBiPhaLeHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            return item.template.type < 5 || item.template.type == 32;
        } else {
            return false;
        }
    }

    public static boolean isDaPhaLe(Item item) {
        return item != null && (item.template.type == 30 || (item.template.id >= 14 && item.template.id <= 20));
    }

    public static int getGemEpSao() {
       
        return 0;
    }

    public static int getOptionDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.getFirst().optionTemplate.id;
        }
        return switch (daPhaLe.template.id) {
            case 20 ->
                77;
            case 19 ->
                103;
            case 18 ->
                80;
            case 17 ->
                81;
            case 16 ->
                50;
            case 15 ->
                94;
            case 14 ->
                108;
            default ->
                -1;
        };
    }

    public static int getParamDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.getFirst().param;
        }
        return switch (daPhaLe.template.id) {
            case 20, 19, 18, 17 ->
                5;
            case 16 ->
                3;
            case 15, 14 ->
                2;
            default ->
                -1;
        };
    }

    public static boolean isCoupleItemNangCapCheck(Item trangBi, Item daNangCap) {
        if (trangBi != null && daNangCap != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else {
                return trangBi.template.type == 4 && daNangCap.template.id == 220;
            }
        } else {
            return false;
        }
    }

    public static int getCountDaNangCapDo(int level) {
        return switch (level) {
            case 0 -> 3;
            case 1 -> 7;
            case 2 -> 11;
            case 3 -> 17;
            case 4 -> 23;
            case 5 -> 35;
            case 6 -> 50;
            case 7 -> 70;
            default -> 0;
        };
    }

    public static int getCountDaBaoVe(int level) {
        return level + 1;
    }

    public static int getGoldNangCapDo(int level) {
        switch (level) {
            case 0 -> {
                return 10000;
            }
            case 1 -> {
                return 70000;
            }
            case 2 -> {
                return 300000;
            }
            case 3 -> {
                return 1500000;
            }
            case 4 -> {
                return 7000000;
            }
            case 5 -> {
                return 23000000;
            }
            case 6 -> {
                return 100000000;
            }
            case 7 -> {
                return 250000000;
            }
        }
        return 0;
    }

    public static double getTileNangCapDo(int level) {
        switch (level) {
            case 0 -> {
                return 80;
            }
            case 1 -> {
                return 50;
            }
            case 2 -> {
                return 20;
            }
            case 3 -> {
                return 10;
            }
            case 4 -> {
                return 5;
            }
            case 5 -> {
                return 3;
            }
            case 6 -> {
                return 2;
            }
            case 7 -> {
                // 7 sao
                return 1;
            }
           
        }
        return 0;
    }

}
