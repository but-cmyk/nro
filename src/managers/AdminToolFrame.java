package managers;

import data.DataGame;
import data.ItemData;
import database.AlyraManager;
import database.daos.NDVSqlFetcher;
import database.daos.PlayerDAO;
import managers.boss.BossManager;
import managers.boss.BrolyManager;
import models.item.Item;
import models.player.Player;
import server.Client;
import server.Manager;
import server.ServerManager;
import server.ServerNotify;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import services.map.ChangeMapService;
import utils.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class AdminToolFrame extends JFrame {

    private final JLabel lastSaveLabel = new JLabel("Lần lưu gần nhất: chưa có");
    private Timer autoSaveTimer = null;
    private boolean isAutoSaveEnabled = false;

    // Biến lưu người chơi đang được chọn để thao tác
    private Player selectedPlayer = null;
    // --- Code thêm mới ---
    private static AdminToolFrame instance;

    public static void showFrame() {
        // Chạy trên luồng giao diện để tránh lỗi xung đột luồng
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (instance == null || !instance.isDisplayable()) {
                instance = new AdminToolFrame();
            }
            instance.setVisible(true);
            instance.setState(java.awt.Frame.NORMAL); // Khôi phục nếu đang bị thu nhỏ
            instance.toFront(); // Đưa cửa sổ lên trên cùng
        });
    }
    // ---------------------

    public AdminToolFrame() {
        setTitle("Admin Tool - Ngọc Rồng (Pro Version)");
        setSize(600, 600); // Tăng kích thước
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Sử dụng JTabbedPane để chia các nhóm chức năng
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Quản lý Server
        tabbedPane.addTab("Server", createServerPanel());

        // Tab 2: Quản lý Player
        tabbedPane.addTab("Player", createPlayerPanel());

        // Tab 3: Quản lý Boss
        tabbedPane.addTab("Boss", createBossPanel());

        add(tabbedPane);
    }

    // =====================================================================
    // PANEL QUẢN LÝ SERVER
    // =====================================================================
    private JPanel createServerPanel() {
        JPanel panel = createStyledPanel("Quản Lý Server");

        addButton(panel, "Tải Lại Dữ Liệu Game (Reload)", e -> reloadGameData());
        addButton(panel, "Lưu Toàn Bộ Player", e -> saveAllPlayers());
        addButton(panel, "Bật/Tắt Auto Save (10 phút)", e -> toggleAutoSave());
        addButton(panel, "Thông Báo Server Nâng Cao", e -> sendAdvancedAnnouncement());
        addButton(panel, "Hiển Thị Thống Kê Server", e -> showServerStats());

        panel.add(Box.createVerticalStrut(20));
        panel.add(lastSaveLabel);

        // Thêm một panel trống để đẩy các nút lên trên
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // =====================================================================
    // PANEL QUẢN LÝ PLAYER
    // =====================================================================
    private JPanel createPlayerPanel() {
        JPanel mainPanel = createStyledPanel("Quản Lý Player");

        // --- Panel tìm kiếm ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Tìm/Chọn Player");
        JLabel selectedPlayerLabel = new JLabel("Chưa chọn player nào.");
        searchPanel.add(new JLabel("Tên Player:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(selectedPlayerLabel);
        mainPanel.add(searchPanel);

        // --- Panel các nút chức năng ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 2, 10, 10)); // Grid layout 2 cột
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Thao tác với Player đã chọn"));

        addButton(buttonPanel, "Buff Vật Phẩm...", e -> buffItemToSelectedPlayer());
        addButton(buttonPanel, "Thêm Vàng/Ngọc...", e -> addGoldGemToSelectedPlayer());
        addButton(buttonPanel, "Teleport Về Làng", e -> teleportSelectedPlayerToVillage());
        addButton(buttonPanel, "Kick Player", e -> kickSelectedPlayer());
        addButton(buttonPanel, "Ban Player", e -> banSelectedPlayer());
        // addButton(buttonPanel, "Triệu Hồi Tới Đây", e -> summonSelectedPlayer()); // Cần Player admin

        mainPanel.add(buttonPanel);

        // --- Panel chức năng chung ---
        JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new GridLayout(0, 2, 10, 10));
        generalPanel.setBorder(BorderFactory.createTitledBorder("Chức năng chung"));

        addButton(generalPanel, "Kick Tất Cả Player", e -> kickAllPlayers());
        addButton(generalPanel, "Teleport Tất Cả Về Làng", e -> teleportAllPlayers());
        addButton(generalPanel, "Kick Clone (Session rỗng)", e -> kickCloneSessions());
        addButton(generalPanel, "Xem Danh Sách Online", e -> showPlayerList());

        mainPanel.add(generalPanel);

        // --- Logic nút tìm kiếm ---
        searchButton.addActionListener(e -> {
            String name = searchField.getText().trim();
            if (!name.isEmpty()) {
                Player p = Client.gI().getPlayer(name);
                if (p != null) {
                    selectedPlayer = p;
                    selectedPlayerLabel.setText("Đã chọn: " + p.name + " (ID: " + p.id + ")");
                    selectedPlayerLabel.setForeground(Color.BLUE);
                } else {
                    selectedPlayer = null;
                    selectedPlayerLabel.setText("Không tìm thấy player!");
                    selectedPlayerLabel.setForeground(Color.RED);
                }
            }
        });

        mainPanel.add(Box.createVerticalGlue());
        return mainPanel;
    }

    // =====================================================================
    // PANEL QUẢN LÝ BOSS
    // =====================================================================
    private JPanel createBossPanel() {
        JPanel panel = createStyledPanel("Quản Lý Boss");

        addButton(panel, "Reset Toàn Bộ Boss", e -> resetAllBoss());
        addButton(panel, "Trạng Thái Boss", e -> showBossStatus());

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // =====================================================================
    // CÁC HÀM TIỆN ÍCH CHO GIAO DIỆN
    // =====================================================================
    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16), Color.DARK_GRAY
        ));
        return panel;
    }

    private void addButton(JPanel panel, String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(220, 240, 255)); // Màu xanh nhạt
        btn.addActionListener(action);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height));
        panel.add(btn);
    }

    // =====================================================================
    // LOGIC CÁC CHỨC NĂNG (Bao gồm cả cũ và mới)
    // =====================================================================
    private void reloadGameData() {
        if (ServerManager.isReloading) {
            JOptionPane.showMessageDialog(this, "Server đang trong quá trình tải lại dữ liệu, vui lòng đợi.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Hành động này có thể gây lag server.\nBạn có chắc chắn muốn tải lại dữ liệu game không?", "Xác Nhận", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    ServerManager.isReloading = true;
                    Logger.log(Logger.YELLOW, "[AdminTool] Bắt đầu RELOAD DATA...");
                    Thread.sleep(1000);
                    AlyraManager.reloadData();
                    Manager.gI().reloadData();
                } catch (Exception e) {
                    Logger.logException(AdminToolFrame.class, e, "[AdminTool] Lỗi khi reload.");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi khi tải lại dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE));
                } finally {
                    ServerManager.isReloading = false;
                    Logger.log(Logger.YELLOW, "[AdminTool] Reload hoàn tất.");
                }

                List<Player> playersToUpdate = new ArrayList<>(Client.gI().getPlayers());
                for (Player player : playersToUpdate) {
                    if (player != null && player.getSession() != null && player.isPl()) {
                        try {
                            DataGame.sendVersionGame(player.getSession());
                            DataGame.updateMap(player.getSession());
                            DataGame.updateSkill(player.getSession());
                            ItemData.updateItem(player.getSession());
                            Service.gI().player(player);
                            Service.gI().point(player);
                            player.playerSkill.sendSkillShortCut();
                            Service.gI().Send_Caitrang(player);
                            Service.gI().sendThongBao(player, "Dữ liệu game vừa được quản trị viên làm mới.");
                        } catch (Exception e) {
                            Logger.error("[AdminTool] Lỗi khi đẩy dữ liệu cho: " + player.name);
                        }
                    }
                }
                long duration = System.currentTimeMillis() - startTime;
                String message = String.format("Hoàn tất! Quá trình mất %d ms.\nĐã cập nhật cho %d người chơi.", duration, playersToUpdate.size());
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, "Thành Công", JOptionPane.INFORMATION_MESSAGE));
            }).start();
        }
    }

    private void buffItemToSelectedPlayer() {
        // Hàm này giờ sẽ hỏi tên người chơi thay vì dùng selectedPlayer
        String name = JOptionPane.showInputDialog(this, "Nhập tên người chơi cần buff vật phẩm (online hoặc offline):");
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        // Tạo một panel tùy chỉnh cho hộp thoại
        JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField itemIdField = new JTextField();
        JTextField quantityField = new JTextField("1");
        JTextField optionsField = new JTextField("73-1");

        dialogPanel.add(new JLabel("ID Vật phẩm:"));
        dialogPanel.add(itemIdField);
        dialogPanel.add(new JLabel("Số lượng:"));
        dialogPanel.add(quantityField);
        dialogPanel.add(new JLabel("Chỉ số (VD: 50-20v77-10):"));
        dialogPanel.add(optionsField);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Buff Vật Phẩm cho " + name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            new Thread(() -> { // Chạy trên luồng riêng để không treo tool
                try {
                    short itemId = Short.parseShort(itemIdField.getText());
                    int quantity = Integer.parseInt(quantityField.getText());
                    String optionsStr = optionsField.getText();

                    // Bước 1: Tìm người chơi (ưu tiên online, nếu không thấy thì load từ DB)
                    Player playerToBuff = Client.gI().getPlayer(name);
                    boolean isOnline = true;
                    if (playerToBuff == null) {
                        playerToBuff = NDVSqlFetcher.loadPlayerByName(name);
                        isOnline = false;
                    }

                    if (playerToBuff == null) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Không tìm thấy người chơi có tên: " + name, "Lỗi", JOptionPane.ERROR_MESSAGE));
                        return;
                    }
                    if (playerToBuff.inventory == null) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Không thể thao tác với hành trang của người chơi này.", "Lỗi", JOptionPane.ERROR_MESSAGE));
                        return;
                    }

                    // Bước 2: Tạo vật phẩm
                    Item item = ItemService.gI().createNewItem(itemId, quantity);
                    if (!optionsStr.trim().isEmpty()) {
                        String[] optionPairs = optionsStr.split("v");
                        for (String pair : optionPairs) {
                            String[] opt = pair.split("-");
                            item.itemOptions.add(new Item.ItemOption(Integer.parseInt(opt[0]), Integer.parseInt(opt[1])));
                        }
                    }

                    // Bước 3: Thêm vào hành trang
                    InventoryService.gI().addItemBag(playerToBuff, item);

                    // Bước 4: Xử lý tùy theo trạng thái online/offline
                    if (isOnline) {
                        // Nếu online, gửi packet cập nhật
                        InventoryService.gI().sendItemBags(playerToBuff);
                        Service.gI().sendThongBao(playerToBuff, "Bạn vừa nhận được vật phẩm từ Admin.");
                    }

                    // Bước 5: Lưu vào database (BẮT BUỘC cho cả online và offline)
                    PlayerDAO.updatePlayer(playerToBuff);

                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Buff vật phẩm thành công cho " + name + "!", "Thành Công", JOptionPane.INFORMATION_MESSAGE));

                } catch (Exception e) {
                    Logger.logException(AdminToolFrame.class, e, "[AdminTool] Lỗi khi buff item");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Dữ liệu nhập không hợp lệ hoặc có lỗi xảy ra!", "Lỗi", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        }
    }

    private void addGoldGemToSelectedPlayer() {
        // Hàm này cũng sẽ hỏi tên người chơi
        String name = JOptionPane.showInputDialog(this, "Nhập tên người chơi cần thêm Vàng/Ngọc (online hoặc offline):");
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        String goldInput = JOptionPane.showInputDialog(this, "Nhập số vàng cần thêm cho " + name + ":");
        String gemInput = JOptionPane.showInputDialog(this, "Nhập số ngọc cần thêm cho " + name + ":");

        new Thread(() -> { // Chạy trên luồng riêng
            try {
                long gold = (goldInput != null && !goldInput.trim().isEmpty()) ? Long.parseLong(goldInput.trim()) : 0;
                int gem = (gemInput != null && !gemInput.trim().isEmpty()) ? Integer.parseInt(gemInput.trim()) : 0;

                // Bước 1: Tìm người chơi
                Player playerToBuff = Client.gI().getPlayer(name);
                boolean isOnline = true;
                if (playerToBuff == null) {
                    playerToBuff = NDVSqlFetcher.loadPlayerByName(name);
                    isOnline = false;
                }

                if (playerToBuff == null) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Không tìm thấy người chơi có tên: " + name, "Lỗi", JOptionPane.ERROR_MESSAGE));
                    return;
                }
                if (playerToBuff.inventory == null) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Không thể thao tác với hành trang của người chơi này.", "Lỗi", JOptionPane.ERROR_MESSAGE));
                    return;
                }

                // Bước 2: Cộng tiền
                playerToBuff.inventory.gold += gold;
                playerToBuff.inventory.gem += gem;

                // Bước 3: Xử lý online/offline
                if (isOnline) {
                    Service.gI().sendMoney(playerToBuff);
                }

                // Bước 4: Lưu vào database
                PlayerDAO.updatePlayer(playerToBuff);

                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Đã thêm " + gold + " vàng và " + gem + " ngọc cho " + name));

            } catch (Exception e) {
                Logger.logException(AdminToolFrame.class, e, "[AdminTool] Lỗi khi thêm vàng ngọc");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Giá trị không hợp lệ hoặc có lỗi xảy ra."));
            }
        }).start();
    }

    private void kickSelectedPlayer() {
        if (selectedPlayer != null) {
            Client.gI().kickSession(selectedPlayer.getSession());
            JOptionPane.showMessageDialog(this, "Đã kick player: " + selectedPlayer.name);
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm và chọn một người chơi trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void banSelectedPlayer() {
        if (selectedPlayer != null) {
            // PlayerService.gI().banPlayer(selectedPlayer); // Logic ban thực sự
            Client.gI().kickSession(selectedPlayer.getSession());
            JOptionPane.showMessageDialog(this, "Đã ban (kick) player: " + selectedPlayer.name);
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm và chọn một người chơi trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void teleportSelectedPlayerToVillage() {
        if (selectedPlayer != null) {
            int mapId = 21 + selectedPlayer.gender;
            ChangeMapService.gI().changeMapBySpaceShip(selectedPlayer, mapId, -1, -1);
            JOptionPane.showMessageDialog(this, "Đã gửi yêu cầu đưa " + selectedPlayer.name + " về làng.");
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm và chọn một người chơi trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kickAllPlayers() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn kick TOÀN BỘ người chơi?", "Xác Nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int kickCount = 0;
            List<Player> playersToKick = new ArrayList<>(Client.gI().getPlayers());
            for (Player pl : playersToKick) {
                if (pl != null && !pl.isAdmin()) {
                    Client.gI().kickSession(pl.getSession());
                    kickCount++;
                }
            }
            JOptionPane.showMessageDialog(this, "Đã kick " + kickCount + " người chơi.");
        }
    }

    private void teleportAllPlayers() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đưa TOÀN BỘ người chơi về làng?", "Xác Nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int teleCount = 0;
            List<Player> playersToTele = new ArrayList<>(Client.gI().getPlayers());
            for (Player pl : playersToTele) {
                if (pl != null) {
                    try {
                        int mapId = 21 + pl.gender;
                        ChangeMapService.gI().changeMapBySpaceShip(pl, mapId, -1, -1);
                        teleCount++;
                    } catch (Exception e) {
                        /* Bỏ qua */ }
                }
            }
            JOptionPane.showMessageDialog(this, "Đã gửi yêu cầu đưa " + teleCount + " người chơi về làng.");
        }
    }

    private void sendAdvancedAnnouncement() {
        String[] options = {"Chat Thế Giới", "Thông Báo Đỏ"};
        String type = (String) JOptionPane.showInputDialog(this, "Chọn loại thông báo:", "Thông Báo Server", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (type != null) {
            String message = JOptionPane.showInputDialog(this, "Nhập nội dung thông báo:");
            if (message != null && !message.trim().isEmpty()) {
                if (type.equals("Thông Báo Đỏ")) {
                    Service.gI().sendThongBaoAllPlayer(message);
                } else {
                    ServerNotify.gI().notify(message);
                }
                Logger.success("[AdminTool] Đã gửi thông báo server: " + message);
                JOptionPane.showMessageDialog(this, "Đã gửi thông báo.");
            }
        }
    }

    // Tìm và thay thế hàm này trong file AdminToolFrame.java
    private void addGoldGemToPlayer(Player pl) {
        // === KIỂM TRA AN TOÀN - BẮT ĐẦU ===
        if (pl == null) {
            JOptionPane.showMessageDialog(this, "Người chơi không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pl.inventory == null) {
            JOptionPane.showMessageDialog(this, "Không thể thao tác với hành trang của người chơi này.\nCó thể người chơi chưa được nạp đầy đủ dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // === KIỂM TRA AN TOÀN - KẾT THÚC ===

        String goldInput = JOptionPane.showInputDialog(this, "Nhập số vàng cần thêm cho " + pl.name + ":");
        String gemInput = JOptionPane.showInputDialog(this, "Nhập số ngọc cần thêm cho " + pl.name + ":");
        try {
            long gold = (goldInput != null && !goldInput.trim().isEmpty()) ? Long.parseLong(goldInput.trim()) : 0;
            int gem = (gemInput != null && !gemInput.trim().isEmpty()) ? Integer.parseInt(gemInput.trim()) : 0;

            // Code logic chính không thay đổi
            pl.inventory.gold += gold;
            pl.inventory.gem += gem;

            // Chỉ gửi packet nếu người chơi đang online và có session
            if (pl.getSession() != null) {
                Service.gI().sendMoney(pl);
            }

            JOptionPane.showMessageDialog(this, "Đã thêm " + gold + " vàng và " + gem + " ngọc cho " + pl.name);

            // Gợi ý: Nếu muốn thay đổi có hiệu lực ngay cả khi người chơi offline,
            // bạn cần gọi hàm lưu dữ liệu người chơi vào database ở đây.
            PlayerDAO.updatePlayer(pl);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá trị không hợp lệ. Vui lòng nhập số.");
        }
    }

    private void saveAllPlayers() {
        new Thread(() -> {
            try {
                int count = 0;
                for (Player player : Client.gI().getPlayers()) {
                    if (player != null && player.isPl()) {
                        PlayerDAO.updatePlayer(player);
                        count++;
                    }
                }
                final int finalCount = count;
                SwingUtilities.invokeLater(() -> {
                    lastSaveLabel.setText("Lần lưu gần nhất: " + java.time.LocalTime.now().withNano(0));
                    JOptionPane.showMessageDialog(this, "Đã lưu " + finalCount + " player.");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi khi lưu player."));
            }
        }).start();
    }

    private void toggleAutoSave() {
        if (!isAutoSaveEnabled) {
            autoSaveTimer = new Timer(10 * 60 * 1000, e -> saveAllPlayers());
            autoSaveTimer.start();
            isAutoSaveEnabled = true;
            JOptionPane.showMessageDialog(this, "Đã bật Auto Save.");
        } else {
            if (autoSaveTimer != null) {
                autoSaveTimer.stop();
            }
            autoSaveTimer = null;
            isAutoSaveEnabled = false;
            JOptionPane.showMessageDialog(this, "Đã tắt Auto Save.");
        }
    }

    private void showServerStats() {
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / 1048576;
        long total = runtime.totalMemory() / 1048576;
        String msg = String.format("RAM: %d MB / %d MB", used, total);
        JOptionPane.showMessageDialog(this, msg, "Thống kê Server", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetAllBoss() {
        BossManager.gI().loadBoss();
        JOptionPane.showMessageDialog(this, "Đã reset boss.");
    }

   

    private void showBossStatus() {
        // Lấy chuỗi trạng thái boss từ BossManager
        String bossStatusText = BossManager.gI().getStatus();

        // Tạo một JTextArea để chứa nội dung
        JTextArea textArea = new JTextArea(25, 50); // Kích thước: 25 dòng, 50 cột
        textArea.setText(bossStatusText);
        textArea.setEditable(false); // Không cho phép người dùng chỉnh sửa
        textArea.setLineWrap(true); // Tự động xuống dòng khi hết hàng
        textArea.setWrapStyleWord(true); // Xuống dòng tại vị trí từ, không ngắt giữa từ

        // Đặt JTextArea vào một JScrollPane để có thanh cuộn
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Hiển thị JScrollPane bên trong JOptionPane
        JOptionPane.showMessageDialog(this, scrollPane, "Trạng Thái Boss", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showPlayerCount() {
        JOptionPane.showMessageDialog(this, "Số player online: " + Client.gI().getPlayers().size());
    }

    private void showPlayerList() {
        List<Player> players = Client.gI().getPlayers();
        if (players.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có ai online.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Player pl : players) {
            sb.append(pl.name).append(" - Map: ").append(pl.zone.map.mapName).append("\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "Danh sách online (" + players.size() + ")", JOptionPane.INFORMATION_MESSAGE);
    }

    private void kickCloneSessions() {
        Client.gI().cloneMySessionNotConnect();
        JOptionPane.showMessageDialog(this, "Đã kick session clone.");
    }
}
