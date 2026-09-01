package models.player;

public class PlayerEffect {

    public static int baseDaiGiaMoiNhu = 1000;
    public static int baseTrumUocRong = 10;
    public static int baseTrumSanBoss = 30;
    public static int baseThanhDapDo = 1000;
    public static int baseNongDanChamChi = 20;
    public static int baseOngThanVeChai = 100;
    public static int baseBiMocSachTui = 1;
    public static int basePhanCung = 100;

    private int pointDaiGiaMoiNhu;
    private int pointTrumUocRong;
    private int pointTrumSanBoss;
    private int pointThanhDapDo;
    private int pointNongDanChamChi;
    private int pointOngThanVeChai;
    private int pointBiMocSachTui;
    private int pointPhanCung;
    private Player player;

    public PlayerEffect(Player player) {
        this.player = player;
    }

    public int getPointDaiGiaMoiNhu() { return pointDaiGiaMoiNhu; }
    public void setPointDaiGiaMoiNhu(int pointDaiGiaMoiNhu) { this.pointDaiGiaMoiNhu = pointDaiGiaMoiNhu; }

    public int getPointTrumUocRong() { return pointTrumUocRong; }
    public void setPointTrumUocRong(int pointTrumUocRong) { this.pointTrumUocRong = pointTrumUocRong; }

    public int getPointTrumSanBoss() { return pointTrumSanBoss; }
    public void setPointTrumSanBoss(int pointTrumSanBoss) { this.pointTrumSanBoss = pointTrumSanBoss; }

    public int getPointThanhDapDo() { return pointThanhDapDo; }
    public void setPointThanhDapDo(int pointThanhDapDo) { this.pointThanhDapDo = pointThanhDapDo; }

    public int getPointNongDanChamChi() { return pointNongDanChamChi; }
    public void setPointNongDanChamChi(int pointNongDanChamChi) { this.pointNongDanChamChi = pointNongDanChamChi; }

    public int getPointOngThanVeChai() { return pointOngThanVeChai; }
    public void setPointOngThanVeChai(int pointOngThanVeChai) { this.pointOngThanVeChai = pointOngThanVeChai; }

    public int getPointBiMocSachTui() { return pointBiMocSachTui; }
    public void setPointBiMocSachTui(int pointBiMocSachTui) { this.pointBiMocSachTui = pointBiMocSachTui; }

    public int getPointPhanCung() { return pointPhanCung; }
    public void setPointPhanCung(int pointPhanCung) { this.pointPhanCung = pointPhanCung; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public void addPointDaiGiamMoiNhu(int value) {
        this.pointDaiGiaMoiNhu += value;
    }

    public void addPointTrumUocRong() {
        this.pointTrumUocRong++;
    }

    public void addPointTrumSanBoss() {
        this.pointTrumSanBoss++;
    }

    public void addPointThanhDapDo() {
        this.pointThanhDapDo++;
    }

    public void addPointNongDanChamChi() {
        this.pointNongDanChamChi++;
    }

    public void addPointOngThanVeChai() {
        this.pointOngThanVeChai++;
    }

    public void addPointBiMocSachTui() {
        this.pointBiMocSachTui++;
    }

    public void addPointPhanCung() {
        this.pointPhanCung++;
    }
}
