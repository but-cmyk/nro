package models.boss;

import consts.AppearType;

public class BossData {

    public static final int DEFAULT_APPEAR = 0;
    public static final int APPEAR_WITH_ANOTHER = 1;
    public static final int ANOTHER_LEVEL = 2;

    private String name;
    private byte gender;
    private short[] outfit;
    private long dame;
    private long[] hp;
    private int[] mapJoin;
    private int[][] skillTemp;
    private String[] textS;
    private String[] textM;
    private String[] textE;
    private int secondsRest;
    private AppearType typeAppear;
    private int[] bossesAppearTogether;

    public BossData() {
    }

    private static long[] toLongArray(int[] arr) {
        if (arr == null) return null;
        long[] res = new long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            res[i] = arr[i];
        }
        return res;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE) {
        this.name = name;
        this.gender = gender;
        this.outfit = outfit;
        this.dame = dame;
        this.hp = hp;
        this.mapJoin = mapJoin;
        this.skillTemp = skillTemp;
        this.textS = textS;
        this.textM = textM;
        this.textE = textE;
        this.secondsRest = 0;
        this.typeAppear = AppearType.DEFAULT_APPEAR;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, int[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE) {
        this(name, gender, outfit, dame, toLongArray(hp), mapJoin, skillTemp, textS, textM, textE);
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE);
        this.secondsRest = secondsRest;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, int[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest) {
        this(name, gender, outfit, dame, toLongArray(hp), mapJoin, skillTemp, textS, textM, textE, secondsRest);
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest, int[] bossesAppearTogether) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE, secondsRest);
        this.bossesAppearTogether = bossesAppearTogether;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, int[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest, int[] bossesAppearTogether) {
        this(name, gender, outfit, dame, toLongArray(hp), mapJoin, skillTemp, textS, textM, textE, secondsRest, bossesAppearTogether);
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, AppearType typeAppear) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE);
        this.typeAppear = typeAppear;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, int[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, AppearType typeAppear) {
        this(name, gender, outfit, dame, toLongArray(hp), mapJoin, skillTemp, textS, textM, textE, typeAppear);
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest, AppearType typeAppear) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE, secondsRest);
        this.typeAppear = typeAppear;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, int[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest, AppearType typeAppear) {
        this(name, gender, outfit, dame, toLongArray(hp), mapJoin, skillTemp, textS, textM, textE, secondsRest, typeAppear);
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public byte getGender() { return gender; }
    public void setGender(byte gender) { this.gender = gender; }

    public short[] getOutfit() { return outfit; }
    public void setOutfit(short[] outfit) { this.outfit = outfit; }

    public long getDame() { return dame; }
    public void setDame(long dame) { this.dame = dame; }

    public long[] getHp() { return hp; }
    public void setHp(long[] hp) { this.hp = hp; }
    public void setHp(int[] hp) { this.hp = toLongArray(hp); }

    public int[] getMapJoin() { return mapJoin; }
    public void setMapJoin(int[] mapJoin) { this.mapJoin = mapJoin; }

    public int[][] getSkillTemp() { return skillTemp; }
    public void setSkillTemp(int[][] skillTemp) { this.skillTemp = skillTemp; }

    public String[] getTextS() { return textS; }
    public void setTextS(String[] textS) { this.textS = textS; }

    public String[] getTextM() { return textM; }
    public void setTextM(String[] textM) { this.textM = textM; }

    public String[] getTextE() { return textE; }
    public void setTextE(String[] textE) { this.textE = textE; }

    public int getSecondsRest() { return secondsRest; }
    public void setSecondsRest(int secondsRest) { this.secondsRest = secondsRest; }

    public AppearType getTypeAppear() { return typeAppear; }
    public void setTypeAppear(AppearType typeAppear) { this.typeAppear = typeAppear; }

    public int[] getBossesAppearTogether() { return bossesAppearTogether; }
    public void setBossesAppearTogether(int[] bossesAppearTogether) { this.bossesAppearTogether = bossesAppearTogether; }
}
