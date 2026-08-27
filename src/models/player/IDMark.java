package models.player;

import consts.ConstNpc;
import models.npc.Npc;
import models.shop.Shop;
import models.map.Zone;

public class IDMark {

    private int idItemUpTop;
    private int typeChangeMap;
    private int indexMenu;
    private int typeInput;
    private byte typeLuckyRound;

    private long idPlayThachDau;
    private int goldThachDau;
    private long killCharId = -9999;

    private long idEnemy;

    private Shop shopOpen;
    private String tagNameShop;

    private byte idSpaceShip;

    private int mbv;

    private String captcha;
    private long recaptcha;

    private long lastTimeBan;
    private boolean isBan;

    private int ott;

    private int playerTradeId = -1;
    private Player playerTrade;
    private long lastTimeTrade;

    private long lastTimeNotifyTimeHoldBlackBall;
    private long lastTimeHoldBlackBall;
    private int tempIdBlackBallHold = -1;
    private boolean holdBlackBall;

    private int tempIdNamecBallHold = -1;
    private boolean holdNamecBall;

    private boolean loadedAllDataPlayer;

    private long lastTimeChangeFlag;

    private int typeDatXD;
    private int slDatXD;
    private Npc npcXD;

    private int typeDatTX;
    private Npc npcTX;

    private int typeDatBC;
    private Npc npcBC;

    private boolean gotoFuture;
    private long lastTimeGoToFuture;

    private Zone zoneKhiGasHuyDiet;
    private int xMapKhiGasHuyDiet;
    private int yMapKhiGasHuyDiet;
    private boolean goToKGHD;
    private long lastTimeGoToKGHD;

    private long lastTimeChangeZone;
    private long lastTimeChatGlobal;
    private long lastTimeChatPrivate;

    private long lastTimePickItem;

    private boolean goToBDKB;
    private long lastTimeGoToBDKB;
    private long lastTimeAnXienTrapBDKB;

    private int shenronType = -1;

    private Npc npcChose;

    private byte loaiThe;

    private boolean acpTrade;

    private int damePST;

    private long lastTimeRevenge;

    private int menuType;

    private int tangHoaType;

    private boolean transactionWP;

    private boolean transactionWVP;

    private long lastTimeCombine;

    public IDMark() {
    }

    public int getIdItemUpTop() { return idItemUpTop; }
    public void setIdItemUpTop(int idItemUpTop) { this.idItemUpTop = idItemUpTop; }

    public int getTypeChangeMap() { return typeChangeMap; }
    public void setTypeChangeMap(int typeChangeMap) { this.typeChangeMap = typeChangeMap; }

    public int getIndexMenu() { return indexMenu; }
    public void setIndexMenu(int indexMenu) { this.indexMenu = indexMenu; }

    public int getTypeInput() { return typeInput; }
    public void setTypeInput(int typeInput) { this.typeInput = typeInput; }

    public byte getTypeLuckyRound() { return typeLuckyRound; }
    public void setTypeLuckyRound(byte typeLuckyRound) { this.typeLuckyRound = typeLuckyRound; }

    public long getIdPlayThachDau() { return idPlayThachDau; }
    public void setIdPlayThachDau(long idPlayThachDau) { this.idPlayThachDau = idPlayThachDau; }

    public int getGoldThachDau() { return goldThachDau; }
    public void setGoldThachDau(int goldThachDau) { this.goldThachDau = goldThachDau; }

    public long getKillCharId() { return killCharId; }
    public void setKillCharId(long killCharId) { this.killCharId = killCharId; }

    public long getIdEnemy() { return idEnemy; }
    public void setIdEnemy(long idEnemy) { this.idEnemy = idEnemy; }

    public Shop getShopOpen() { return shopOpen; }
    public void setShopOpen(Shop shopOpen) { this.shopOpen = shopOpen; }

    public String getTagNameShop() { return tagNameShop; }
    public void setTagNameShop(String tagNameShop) { this.tagNameShop = tagNameShop; }

    public byte getIdSpaceShip() { return idSpaceShip; }
    public void setIdSpaceShip(byte idSpaceShip) { this.idSpaceShip = idSpaceShip; }

    public int getMbv() { return mbv; }
    public void setMbv(int mbv) { this.mbv = mbv; }

    public String getCaptcha() { return captcha; }
    public void setCaptcha(String captcha) { this.captcha = captcha; }

    public long getRecaptcha() { return recaptcha; }
    public void setRecaptcha(long recaptcha) { this.recaptcha = recaptcha; }

    public long getLastTimeBan() { return lastTimeBan; }
    public void setLastTimeBan(long lastTimeBan) { this.lastTimeBan = lastTimeBan; }

    public boolean isBan() { return isBan; }
    public void setBan(boolean isBan) { this.isBan = isBan; }

    public int getOtt() { return ott; }
    public void setOtt(int ott) { this.ott = ott; }

    public int getPlayerTradeId() { return playerTradeId; }
    public void setPlayerTradeId(int playerTradeId) { this.playerTradeId = playerTradeId; }

    public Player getPlayerTrade() { return playerTrade; }
    public void setPlayerTrade(Player playerTrade) { this.playerTrade = playerTrade; }

    public long getLastTimeTrade() { return lastTimeTrade; }
    public void setLastTimeTrade(long lastTimeTrade) { this.lastTimeTrade = lastTimeTrade; }

    public long getLastTimeNotifyTimeHoldBlackBall() { return lastTimeNotifyTimeHoldBlackBall; }
    public void setLastTimeNotifyTimeHoldBlackBall(long lastTimeNotifyTimeHoldBlackBall) { this.lastTimeNotifyTimeHoldBlackBall = lastTimeNotifyTimeHoldBlackBall; }

    public long getLastTimeHoldBlackBall() { return lastTimeHoldBlackBall; }
    public void setLastTimeHoldBlackBall(long lastTimeHoldBlackBall) { this.lastTimeHoldBlackBall = lastTimeHoldBlackBall; }

    public int getTempIdBlackBallHold() { return tempIdBlackBallHold; }
    public void setTempIdBlackBallHold(int tempIdBlackBallHold) { this.tempIdBlackBallHold = tempIdBlackBallHold; }

    public boolean isHoldBlackBall() { return holdBlackBall; }
    public void setHoldBlackBall(boolean holdBlackBall) { this.holdBlackBall = holdBlackBall; }

    public int getTempIdNamecBallHold() { return tempIdNamecBallHold; }
    public void setTempIdNamecBallHold(int tempIdNamecBallHold) { this.tempIdNamecBallHold = tempIdNamecBallHold; }

    public boolean isHoldNamecBall() { return holdNamecBall; }
    public void setHoldNamecBall(boolean holdNamecBall) { this.holdNamecBall = holdNamecBall; }

    public boolean isLoadedAllDataPlayer() { return loadedAllDataPlayer; }
    public void setLoadedAllDataPlayer(boolean loadedAllDataPlayer) { this.loadedAllDataPlayer = loadedAllDataPlayer; }

    public long getLastTimeChangeFlag() { return lastTimeChangeFlag; }
    public void setLastTimeChangeFlag(long lastTimeChangeFlag) { this.lastTimeChangeFlag = lastTimeChangeFlag; }

    public int getTypeDatXD() { return typeDatXD; }
    public void setTypeDatXD(int typeDatXD) { this.typeDatXD = typeDatXD; }

    public int getSlDatXD() { return slDatXD; }
    public void setSlDatXD(int slDatXD) { this.slDatXD = slDatXD; }

    public Npc getNpcXD() { return npcXD; }
    public void setNpcXD(Npc npcXD) { this.npcXD = npcXD; }

    public int getTypeDatTX() { return typeDatTX; }
    public void setTypeDatTX(int typeDatTX) { this.typeDatTX = typeDatTX; }

    public Npc getNpcTX() { return npcTX; }
    public void setNpcTX(Npc npcTX) { this.npcTX = npcTX; }

    public int getTypeDatBC() { return typeDatBC; }
    public void setTypeDatBC(int typeDatBC) { this.typeDatBC = typeDatBC; }

    public Npc getNpcBC() { return npcBC; }
    public void setNpcBC(Npc npcBC) { this.npcBC = npcBC; }

    public boolean isGotoFuture() { return gotoFuture; }
    public void setGotoFuture(boolean gotoFuture) { this.gotoFuture = gotoFuture; }

    public long getLastTimeGoToFuture() { return lastTimeGoToFuture; }
    public void setLastTimeGoToFuture(long lastTimeGoToFuture) { this.lastTimeGoToFuture = lastTimeGoToFuture; }

    public Zone getZoneKhiGasHuyDiet() { return zoneKhiGasHuyDiet; }
    public void setZoneKhiGasHuyDiet(Zone zoneKhiGasHuyDiet) { this.zoneKhiGasHuyDiet = zoneKhiGasHuyDiet; }

    public int getXMapKhiGasHuyDiet() { return xMapKhiGasHuyDiet; }
    public int getxMapKhiGasHuyDiet() { return xMapKhiGasHuyDiet; }
    public void setXMapKhiGasHuyDiet(int xMapKhiGasHuyDiet) { this.xMapKhiGasHuyDiet = xMapKhiGasHuyDiet; }
    public void setxMapKhiGasHuyDiet(int xMapKhiGasHuyDiet) { this.xMapKhiGasHuyDiet = xMapKhiGasHuyDiet; }

    public int getYMapKhiGasHuyDiet() { return yMapKhiGasHuyDiet; }
    public int getyMapKhiGasHuyDiet() { return yMapKhiGasHuyDiet; }
    public void setYMapKhiGasHuyDiet(int yMapKhiGasHuyDiet) { this.yMapKhiGasHuyDiet = yMapKhiGasHuyDiet; }
    public void setyMapKhiGasHuyDiet(int yMapKhiGasHuyDiet) { this.yMapKhiGasHuyDiet = yMapKhiGasHuyDiet; }

    public boolean isGoToKGHD() { return goToKGHD; }
    public void setGoToKGHD(boolean goToKGHD) { this.goToKGHD = goToKGHD; }

    public long getLastTimeGoToKGHD() { return lastTimeGoToKGHD; }
    public void setLastTimeGoToKGHD(long lastTimeGoToKGHD) { this.lastTimeGoToKGHD = lastTimeGoToKGHD; }

    public long getLastTimeChangeZone() { return lastTimeChangeZone; }
    public void setLastTimeChangeZone(long lastTimeChangeZone) { this.lastTimeChangeZone = lastTimeChangeZone; }

    public long getLastTimeChatGlobal() { return lastTimeChatGlobal; }
    public void setLastTimeChatGlobal(long lastTimeChatGlobal) { this.lastTimeChatGlobal = lastTimeChatGlobal; }

    public long getLastTimeChatPrivate() { return lastTimeChatPrivate; }
    public void setLastTimeChatPrivate(long lastTimeChatPrivate) { this.lastTimeChatPrivate = lastTimeChatPrivate; }

    public long getLastTimePickItem() { return lastTimePickItem; }
    public void setLastTimePickItem(long lastTimePickItem) { this.lastTimePickItem = lastTimePickItem; }

    public boolean isGoToBDKB() { return goToBDKB; }
    public void setGoToBDKB(boolean goToBDKB) { this.goToBDKB = goToBDKB; }

    public long getLastTimeGoToBDKB() { return lastTimeGoToBDKB; }
    public void setLastTimeGoToBDKB(long lastTimeGoToBDKB) { this.lastTimeGoToBDKB = lastTimeGoToBDKB; }

    public long getLastTimeAnXienTrapBDKB() { return lastTimeAnXienTrapBDKB; }
    public void setLastTimeAnXienTrapBDKB(long lastTimeAnXienTrapBDKB) { this.lastTimeAnXienTrapBDKB = lastTimeAnXienTrapBDKB; }

    public int getShenronType() { return shenronType; }
    public void setShenronType(int shenronType) { this.shenronType = shenronType; }

    public Npc getNpcChose() { return npcChose; }
    public void setNpcChose(Npc npcChose) { this.npcChose = npcChose; }

    public byte getLoaiThe() { return loaiThe; }
    public void setLoaiThe(byte loaiThe) { this.loaiThe = loaiThe; }

    public boolean isAcpTrade() { return acpTrade; }
    public void setAcpTrade(boolean acpTrade) { this.acpTrade = acpTrade; }

    public int getDamePST() { return damePST; }
    public void setDamePST(int damePST) { this.damePST = damePST; }

    public long getLastTimeRevenge() { return lastTimeRevenge; }
    public void setLastTimeRevenge(long lastTimeRevenge) { this.lastTimeRevenge = lastTimeRevenge; }

    public int getMenuType() { return menuType; }
    public void setMenuType(int menuType) { this.menuType = menuType; }

    public int getTangHoaType() { return tangHoaType; }
    public void setTangHoaType(int tangHoaType) { this.tangHoaType = tangHoaType; }

    public boolean isTransactionWP() { return transactionWP; }
    public void setTransactionWP(boolean transactionWP) { this.transactionWP = transactionWP; }

    public boolean isTransactionWVP() { return transactionWVP; }
    public void setTransactionWVP(boolean transactionWVP) { this.transactionWVP = transactionWVP; }

    public long getLastTimeCombine() { return lastTimeCombine; }
    public void setLastTimeCombine(long lastTimeCombine) { this.lastTimeCombine = lastTimeCombine; }

    public boolean isBaseMenu() {
        return this.indexMenu == ConstNpc.BASE_MENU;
    }

    public void dispose() {
        if (this.shopOpen != null) {
            this.shopOpen.dispose();
            this.shopOpen = null;
        }
        this.npcChose = null;
        this.tagNameShop = null;
        this.playerTrade = null;
        this.npcXD = null;
        this.npcTX = null;
        this.npcBC = null;
        this.zoneKhiGasHuyDiet = null;
    }
}
