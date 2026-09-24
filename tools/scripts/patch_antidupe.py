import os
import sys

def patch_trade():
    path = r'src/services/func/Trade.java'
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. openTabTrade
    target1 = '''    public synchronized void openTabTrade() {
        player1.idMark.setAcpTrade(true);
        player2.idMark.setAcpTrade(true);
        this.lastTimeStart = System.currentTimeMillis();'''
    
    replace1 = '''    public synchronized void openTabTrade() {
        player1.idMark.setAcpTrade(true);
        player2.idMark.setAcpTrade(true);
        player1.isTrade = true;
        player2.isTrade = true;
        this.lastTimeStart = System.currentTimeMillis();'''
    
    assert target1 in content, "target1 not found in Trade.java"
    content = content.replace(target1, replace1, 1)

    # 2. closeTab null safety
    target_close = '''    private void closeTab() {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(7);
            player1.sendMessage(msg);
            player2.sendMessage(msg);'''

    replace_close = '''    private void closeTab() {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(7);
            if (player1 != null) {
                player1.sendMessage(msg);
            }
            if (player2 != null) {
                player2.sendMessage(msg);
            }'''
    assert target_close in content, "target_close not found in Trade.java"
    content = content.replace(target_close, replace_close, 1)

    # 3. dispose cleanup
    target_disp = '''    public synchronized void dispose() {
        player1.idMark.setPlayerTradeId(-1);
        player2.idMark.setPlayerTradeId(-1);
        TransactionService.PLAYER_TRADE.remove(player1);
        TransactionService.PLAYER_TRADE.remove(player2);
        this.player1 = null;
        this.player2 = null;
        this.itemsBag1 = null;
        this.itemsBag2 = null;
        this.itemsTrade1 = null;
        this.itemsTrade2 = null;
    }'''

    replace_disp = '''    public synchronized void dispose() {
        this.start = false;
        if (player1 != null) {
            player1.isTrade = false;
            player1.idMark.setPlayerTradeId(-1);
            player1.idMark.setAcpTrade(false);
            TransactionService.PLAYER_TRADE.remove(player1);
        }
        if (player2 != null) {
            player2.isTrade = false;
            player2.idMark.setPlayerTradeId(-1);
            player2.idMark.setAcpTrade(false);
            TransactionService.PLAYER_TRADE.remove(player2);
        }
        this.player1 = null;
        this.player2 = null;
        this.itemsBag1 = null;
        this.itemsBag2 = null;
        this.itemsTrade1 = null;
        this.itemsTrade2 = null;
    }'''
    assert target_disp in content, "target_disp not found in Trade.java"
    content = content.replace(target_disp, replace_disp, 1)

    # 4. startTrade failure & success closeTab & dispose
    target_start = '''                if (tradeStatus != SUCCESS) {
                    sendNotifyTrade(tradeStatus);
                    return;
                }

        for (Item item : itemsTrade1) {
            if (!InventoryService.gI().addItemList(itemsBag2, item)) {
                tradeStatus = FAIL_NOT_ENOUGH_BAG_P1;
                break;
            }
        }

        if (tradeStatus != SUCCESS) {
            sendNotifyTrade(tradeStatus);
            return;
        }

        for (Item item : itemsTrade2) {
            if (!InventoryService.gI().addItemList(itemsBag1, item)) {
                tradeStatus = FAIL_NOT_ENOUGH_BAG_P2;
                break;
            }
        }

        if (tradeStatus != SUCCESS) {
            sendNotifyTrade(tradeStatus);
            return;
        }

        player1.inventory.gold += goldTrade2;
        player2.inventory.gold += goldTrade1;
        player1.inventory.gold -= goldTrade1;
        player2.inventory.gold -= goldTrade2;
        player1.inventory.itemsBag = itemsBag1;
        player2.inventory.itemsBag = itemsBag2;

        InventoryService.gI().sendItemBags(player1);
        InventoryService.gI().sendItemBags(player2);
        PlayerService.gI().sendInfoHpMpMoney(player1);
        PlayerService.gI().sendInfoHpMpMoney(player2);

        HistoryTransactionDAO.insert(player1, player2, goldTrade1, goldTrade2, itemsTrade1, itemsTrade2,
                bag1Before, bag2Before, this.player1.inventory.itemsBag, this.player2.inventory.itemsBag,
                gold1Before, gold2Before, this.player1.inventory.gold, this.player2.inventory.gold);

        database.daos.PlayerDAO.updatePlayerAsync(player1);
        database.daos.PlayerDAO.updatePlayerAsync(player2);

        sendNotifyTrade(SUCCESS);'''

    replace_start = '''                if (tradeStatus != SUCCESS) {
                    sendNotifyTrade(tradeStatus);
                    closeTab();
                    dispose();
                    return;
                }

                for (Item item : itemsTrade1) {
                    if (!InventoryService.gI().addItemList(itemsBag2, item)) {
                        tradeStatus = FAIL_NOT_ENOUGH_BAG_P1;
                        break;
                    }
                }

                if (tradeStatus != SUCCESS) {
                    sendNotifyTrade(tradeStatus);
                    closeTab();
                    dispose();
                    return;
                }

                for (Item item : itemsTrade2) {
                    if (!InventoryService.gI().addItemList(itemsBag1, item)) {
                        tradeStatus = FAIL_NOT_ENOUGH_BAG_P2;
                        break;
                    }
                }

                if (tradeStatus != SUCCESS) {
                    sendNotifyTrade(tradeStatus);
                    closeTab();
                    dispose();
                    return;
                }

                player1.inventory.gold += goldTrade2;
                player2.inventory.gold += goldTrade1;
                player1.inventory.gold -= goldTrade1;
                player2.inventory.gold -= goldTrade2;
                player1.inventory.itemsBag = itemsBag1;
                player2.inventory.itemsBag = itemsBag2;

                InventoryService.gI().sendItemBags(player1);
                InventoryService.gI().sendItemBags(player2);
                PlayerService.gI().sendInfoHpMpMoney(player1);
                PlayerService.gI().sendInfoHpMpMoney(player2);

                HistoryTransactionDAO.insert(player1, player2, goldTrade1, goldTrade2, itemsTrade1, itemsTrade2,
                        bag1Before, bag2Before, this.player1.inventory.itemsBag, this.player2.inventory.itemsBag,
                        gold1Before, gold2Before, this.player1.inventory.gold, this.player2.inventory.gold);

                database.daos.PlayerDAO.updatePlayerAsync(player1);
                database.daos.PlayerDAO.updatePlayerAsync(player2);

                sendNotifyTrade(SUCCESS);
                closeTab();
                dispose();'''
    assert target_start in content, "target_start not found in Trade.java"
    content = content.replace(target_start, replace_start, 1)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched Trade.java successfully.")

def patch_transaction_service():
    path = r'src/services/func/TransactionService.java'
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    target = '''                case ACCEPT:
                    if (Maintenance.isRunning) {
                        trade.cancelTrade();
                        break;
                    }
                    if (trade != null) {
                        trade.acceptTrade(pl);
                        if (trade.accept == 1) {
                            Service.gI().sendThongBao(pl, "Xin chờ đối phương đồng ý");
                        } else if (trade.accept == 2) {
                            trade.dispose();
                        }
                        pl.isTrade = false;
                    }
                    break;'''

    replace = '''                case ACCEPT:
                    if (Maintenance.isRunning) {
                        trade.cancelTrade();
                        break;
                    }
                    if (trade != null) {
                        trade.acceptTrade(pl);
                        if (trade.accept == 1) {
                            Service.gI().sendThongBao(pl, "Xin chờ đối phương đồng ý");
                        }
                    }
                    break;'''
    assert target in content, "target not found in TransactionService.java"
    content = content.replace(target, replace, 1)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched TransactionService.java successfully.")

def patch_shop_service():
    path = r'src/services/ShopService.java'
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # showConfirmSellItem
    target_confirm = '''    public void showConfirmSellItem(Player pl, int where, int index) {
        if (pl.isTrade) {
            return;
        }'''
    replace_confirm = '''    public void showConfirmSellItem(Player pl, int where, int index) {
        if (pl.isTrade || services.func.TransactionService.gI().check(pl)) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện khi đang giao dịch");
            return;
        }'''
    assert target_confirm in content, "target_confirm not found in ShopService.java"
    content = content.replace(target_confirm, replace_confirm, 1)

    # sellItem
    target_sell = '''    public void sellItem(Player pl, int where, int index) {
        if (pl.idMark.getShopOpen() == null || pl.idMark.getTagNameShop() == null) {'''
    replace_sell = '''    public void sellItem(Player pl, int where, int index) {
        if (pl.isTrade || services.func.TransactionService.gI().check(pl)) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện khi đang giao dịch");
            return;
        }
        if (pl.idMark.getShopOpen() == null || pl.idMark.getTagNameShop() == null) {'''
    assert target_sell in content, "target_sell not found in ShopService.java"
    content = content.replace(target_sell, replace_sell, 1)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched ShopService.java successfully.")

def patch_consign_shop_service():
    path = r'src/services/ConsignShopService.java'
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # buyItem
    target_buy = '''    public void buyItem(Player pl, int id) {
        if (pl.nPoint.power < 17_000_000_000L) {'''
    replace_buy = '''    public void buyItem(Player pl, int id) {
        if (pl == null) {
            return;
        }
        if (pl.isTrade || services.func.TransactionService.gI().check(pl)) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện khi đang giao dịch");
            return;
        }
        if (pl.nPoint.power < 17_000_000_000L) {'''
    assert target_buy in content, "target_buy not found in ConsignShopService.java"
    content = content.replace(target_buy, replace_buy, 1)

    # KiGui
    target_kigui = '''    public void KiGui(Player pl, int id, int money, byte moneyType, int quantity) {
        ConsignShopManager manager = ConsignShopManager.gI();'''
    replace_kigui = '''    public void KiGui(Player pl, int id, int money, byte moneyType, int quantity) {
        if (pl == null) {
            return;
        }
        if (pl.isTrade || services.func.TransactionService.gI().check(pl)) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện khi đang giao dịch");
            return;
        }
        ConsignShopManager manager = ConsignShopManager.gI();'''
    assert target_kigui in content, "target_kigui not found in ConsignShopService.java"
    content = content.replace(target_kigui, replace_kigui, 1)

    # claimOrDel
    target_claim = '''    public void claimOrDel(Player pl, byte action, int id) {
        ConsignShopManager manager = ConsignShopManager.gI();'''
    replace_claim = '''    public void claimOrDel(Player pl, byte action, int id) {
        if (pl == null) {
            return;
        }
        if (pl.isTrade || services.func.TransactionService.gI().check(pl)) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện khi đang giao dịch");
            return;
        }
        ConsignShopManager manager = ConsignShopManager.gI();'''
    assert target_claim in content, "target_claim not found in ConsignShopService.java"
    content = content.replace(target_claim, replace_claim, 1)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched ConsignShopService.java successfully.")

if __name__ == '__main__':
    # patch_trade()
    # patch_transaction_service()
    # patch_shop_service()
    patch_consign_shop_service()
    print("ALL ANTI-DUPE PATCHES APPLIED SUCCESSFULLY!")
