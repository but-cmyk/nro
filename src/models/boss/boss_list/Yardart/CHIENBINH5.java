package models.boss.boss_list.Yardart;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossID;
import models.boss.BossesData;
import static consts.BossType.YARDART;

public class CHIENBINH5 extends Yardart {

    public CHIENBINH5() throws Exception {
        super(YARDART, BossID.CHIEN_BINH_5, BossesData.CHIEN_BINH_5);
    }

    @Override
    protected void init() {
        x = 1199;
        x2 = 1269;
        y = 456;
        y2 = 432;
        range = 1000;
        range2 = 150;
        timeHoiHP = 20000;
        rewardRatio = 100;
    }

}
