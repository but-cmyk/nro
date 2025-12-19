package models.boss.boss_list.Yardart;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossID;
import models.boss.BossesData;
import static consts.BossType.YARDART;

public class CHIENBINH3 extends Yardart {

    public CHIENBINH3() throws Exception {
        super(YARDART, BossID.CHIEN_BINH_3, BossesData.CHIEN_BINH_3);
    }

    @Override
    protected void init() {
        x = 787;
        x2 = 857;
        y = 456;
        y2 = 456;
        range = 1000;
        range2 = 150;
        timeHoiHP = 20000;
        rewardRatio = 100;
    }

}
