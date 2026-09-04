package services.func.useitem;

import models.item.Item;
import models.player.Player;
import services.func.useitem.handlers.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry & Dispatcher điều phối xử lý hành động sử dụng vật phẩm trong NRO.
 * Áp dụng Chain of Responsibility & Strategy Pattern.
 * Đạt chuẩn Clean Code & Open-Closed Principle (OCP).
 */
public class ItemActionManager {

    private static ItemActionManager instance;
    private final List<ItemActionHandler> handlers;

    // Direct handler references for Facade delegators
    private final ConsumableItemHandler consumableHandler;
    private final CapsuleItemHandler capsuleHandler;
    private final SpecialItemHandler specialHandler;
    private final SkillBookItemHandler skillBookHandler;
    private final SatelliteItemHandler satelliteHandler;
    private final ChestBoxItemHandler chestBoxHandler;
    private final MountPetItemHandler mountPetHandler;

    private ItemActionManager() {
        this.handlers = new ArrayList<>();

        this.consumableHandler = new ConsumableItemHandler();
        this.capsuleHandler = new CapsuleItemHandler();
        this.specialHandler = new SpecialItemHandler();
        this.skillBookHandler = new SkillBookItemHandler();
        this.satelliteHandler = new SatelliteItemHandler();
        this.chestBoxHandler = new ChestBoxItemHandler();
        this.mountPetHandler = new MountPetItemHandler();

        // Thứ tự ưu tiên đăng ký:
        // 1. Consumable (thức ăn, đậu thần, TDLT, item time,..)
        // 2. Skill book (sách võ công)
        // 3. Capsule (capsule bay map)
        // 4. Mount & Pet & Flag (thú cưỡi, cờ, pet theo sau, danh hiệu)
        // 5. Satellite (vệ tinh)
        // 6. Special (bông tai, rada, ngọc rồng, capsule kỳ bí, ...)
        // 7. ChestBox (rương gỗ, capsule kì bí, hộp quà, phụ kiện, SKH)
        registerHandler(consumableHandler);
        registerHandler(skillBookHandler);
        registerHandler(capsuleHandler);
        registerHandler(mountPetHandler);
        registerHandler(satelliteHandler);
        registerHandler(specialHandler);
        registerHandler(chestBoxHandler);
    }

    public static synchronized ItemActionManager gI() {
        if (instance == null) {
            instance = new ItemActionManager();
        }
        return instance;
    }

    /**
     * Đăng ký thêm handler mới phục vụ mở rộng (Open-Closed Principle).
     */
    public void registerHandler(ItemActionHandler handler) {
        if (handler != null && !handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    /**
     * Điều phối item tới handler tương ứng xử lý.
     * @param player Người chơi sử dụng
     * @param item Vật phẩm sử dụng
     * @param bagIndex Vị trí ô đồ
     * @return true nếu có handler tiếp nhận và xử lý
     */
    public boolean dispatch(Player player, Item item, int bagIndex) {
        if (player == null || item == null || item.template == null) {
            return false;
        }

        for (ItemActionHandler handler : handlers) {
            if (handler.canHandle(player, item)) {
                handler.handle(player, item, bagIndex);
                return true;
            }
        }
        return false;
    }

    public List<ItemActionHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    // Direct accessors cho Facade methods
    public ConsumableItemHandler getConsumableHandler() {
        return consumableHandler;
    }

    public CapsuleItemHandler getCapsuleHandler() {
        return capsuleHandler;
    }

    public SpecialItemHandler getSpecialHandler() {
        return specialHandler;
    }

    public SkillBookItemHandler getSkillBookHandler() {
        return skillBookHandler;
    }

    public SatelliteItemHandler getSatelliteHandler() {
        return satelliteHandler;
    }

    public ChestBoxItemHandler getChestBoxHandler() {
        return chestBoxHandler;
    }

    public MountPetItemHandler getMountPetHandler() {
        return mountPetHandler;
    }
}
