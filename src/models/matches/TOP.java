package models.matches;

public class TOP {

    private int id_player;
    private long power;
    private long ki;
    private long hp;
    private long sd;
    private byte nv;
    private int sk;
    private int pvp;
    private String info1;
    private String info2;
    public int rank;

    public TOP() {
    }

    public TOP(int id_player, long power, long ki, long hp, long sd, byte nv, int sk, int pvp, String info1, String info2, int rank) {
        this.id_player = id_player;
        this.power = power;
        this.ki = ki;
        this.hp = hp;
        this.sd = sd;
        this.nv = nv;
        this.sk = sk;
        this.pvp = pvp;
        this.info1 = info1;
        this.info2 = info2;
        this.rank = rank;
    }

    public static TOPBuilder builder() {
        return new TOPBuilder();
    }

    public int getId_player() { return id_player; }
    public void setId_player(int id_player) { this.id_player = id_player; }

    public long getPower() { return power; }
    public void setPower(long power) { this.power = power; }

    public long getKi() { return ki; }
    public void setKi(long ki) { this.ki = ki; }

    public long getHp() { return hp; }
    public void setHp(long hp) { this.hp = hp; }

    public long getSd() { return sd; }
    public void setSd(long sd) { this.sd = sd; }

    public byte getNv() { return nv; }
    public void setNv(byte nv) { this.nv = nv; }

    public int getSk() { return sk; }
    public void setSk(int sk) { this.sk = sk; }

    public int getPvp() { return pvp; }
    public void setPvp(int pvp) { this.pvp = pvp; }

    public String getInfo1() { return info1; }
    public void setInfo1(String info1) { this.info1 = info1; }

    public String getInfo2() { return info2; }
    public void setInfo2(String info2) { this.info2 = info2; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public static class TOPBuilder {
        private int id_player;
        private long power;
        private long ki;
        private long hp;
        private long sd;
        private byte nv;
        private int sk;
        private int pvp;
        private String info1;
        private String info2;
        private int rank;

        public TOPBuilder id_player(int id_player) { this.id_player = id_player; return this; }
        public TOPBuilder power(long power) { this.power = power; return this; }
        public TOPBuilder ki(long ki) { this.ki = ki; return this; }
        public TOPBuilder hp(long hp) { this.hp = hp; return this; }
        public TOPBuilder sd(long sd) { this.sd = sd; return this; }
        public TOPBuilder nv(byte nv) { this.nv = nv; return this; }
        public TOPBuilder sk(int sk) { this.sk = sk; return this; }
        public TOPBuilder pvp(int pvp) { this.pvp = pvp; return this; }
        public TOPBuilder info1(String info1) { this.info1 = info1; return this; }
        public TOPBuilder info2(String info2) { this.info2 = info2; return this; }
        public TOPBuilder rank(int rank) { this.rank = rank; return this; }

        public TOP build() {
            return new TOP(id_player, power, ki, hp, sd, nv, sk, pvp, info1, info2, rank);
        }
    }
}
