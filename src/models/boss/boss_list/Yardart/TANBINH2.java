package models.boss.boss_list.Yardart;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossID;
import models.boss.BossesData;
import static consts.BossType.YARDART;

public class TANBINH2 extends Yardart {

    public TANBINH2() throws Exception {
        super(YARDART, BossID.TAN_BINH_2, BossesData.TAN_BINH_2);
    }

    @Override
    protected void init() {
        x = 582;
        x2 = 652;
        y = 432;
        y2 = 432;
        range = 1000;
        range2 = 150;
        timeHoiHP = 25000;
        rewardRatio = 100;
    }
}
