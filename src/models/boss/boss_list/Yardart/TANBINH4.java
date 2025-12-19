package models.boss.boss_list.Yardart;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossID;
import models.boss.BossesData;
import static consts.BossType.YARDART;

public class TANBINH4 extends Yardart {

    public TANBINH4() throws Exception {
        super(YARDART, BossID.TAN_BINH_4, BossesData.TAN_BINH_4);
    }

    @Override
    protected void init() {
        x = 993;
        x2 = 1063;
        y = 456;
        y2 = 456;
        range = 1000;
        range2 = 150;
        timeHoiHP = 25000;
        rewardRatio = 100;
    }
}
