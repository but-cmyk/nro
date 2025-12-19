package models.boss.boss_list.Yardart;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossID;
import models.boss.BossesData;
import static consts.BossType.YARDART;

public class TAPSU1 extends Yardart {

    public TAPSU1() throws Exception {
        super(YARDART, BossID.TAP_SU_1, BossesData.TAP_SU_1);
    }

    @Override
    protected void init() {
        x = 376;
        x2 = 446;
        y = 456;
        y2 = 432;
        range = 1000;
        range2 = 150;
        timeHoiHP = 30000;
        rewardRatio = 100;
    }
}
