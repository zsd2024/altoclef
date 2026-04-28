package adris.altoclef.util.slots;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;

import java.util.stream.IntStream;

/**
 * 玩家槽位类
 * 表示玩家界面中的各种槽位，包括合成输出槽、盔甲槽、副手槽等
 */
public class PlayerSlot extends Slot {
    /** 合成输出槽位（窗口槽位索引为0） */
    public static final PlayerSlot CRAFT_OUTPUT_SLOT = new PlayerSlot(0);
    // 盔甲槽位在合成/熔炉界面中不可见（会导致问题），因此不安全使用。
    /** 头盔槽位（窗口槽位索引为5） */
    public static final PlayerSlot ARMOR_HELMET_SLOT = new PlayerSlot(5);
    /** 胸甲槽位（窗口槽位索引为6） */
    public static final PlayerSlot ARMOR_CHESTPLATE_SLOT = new PlayerSlot(6);
    /** 护腿槽位（窗口槽位索引为7） */
    public static final PlayerSlot ARMOR_LEGGINGS_SLOT = new PlayerSlot(7);
    /** 靴子槽位（窗口槽位索引为8） */
    public static final PlayerSlot ARMOR_BOOTS_SLOT = new PlayerSlot(8);
    /** 所有盔甲槽位数组 */
    public static final PlayerSlot[] ARMOR_SLOTS = new PlayerSlot[]{
            ARMOR_HELMET_SLOT,
            ARMOR_CHESTPLATE_SLOT,
            ARMOR_LEGGINGS_SLOT,
            ARMOR_BOOTS_SLOT
    };
    /** 副手槽位（窗口槽位索引为45） */
    public static final PlayerSlot OFFHAND_SLOT = new PlayerSlot(45);

    /** 合成输入槽位数组（4个槽位） */
    public static final PlayerSlot[] CRAFT_INPUT_SLOTS = IntStream.range(0, 4).mapToObj(PlayerSlot::getCraftInputSlot).toArray(PlayerSlot[]::new);

    /**
     * 构造函数
     * 
     * @param windowSlot 窗口槽位索引
     */
    public PlayerSlot(int windowSlot) {
        this(windowSlot, false);
    }

    /**
     * 受保护的构造函数
     * 
     * @param slot 箱格索引
     * @param inventory 是否为库存槽位
     */
    protected PlayerSlot(int slot, boolean inventory) {
        super(slot, inventory);
    }

    /**
     * 获取合成输入槽位（基于坐标）
     * 
     * @param x X坐标（0-1）
     * @param y Y坐标（0-1）
     * @return 对应的合成输入槽位
     */
    public static PlayerSlot getCraftInputSlot(int x, int y) {
        return getCraftInputSlot(y * 2 + x);
    }

    /**
     * 获取合成输入槽位（基于索引）
     * 
     * @param index 索引（0-3）
     * @return 对应的合成输入槽位
     */
    public static PlayerSlot getCraftInputSlot(int index) {
        return new PlayerSlot(index + 1);
    }

    /**
     * 根据装备槽位类型获取对应的槽位
     * 
     * @param equipSlot 装备槽位类型
     * @return 对应的槽位
     */
    public static Slot getEquipSlot(EquipmentSlot equipSlot) {
        switch (equipSlot) {
            case MAINHAND:
                assert MinecraftClient.getInstance().player != null;
                return Slot.getFromCurrentScreenInventory(MinecraftClient.getInstance().player.getInventory().selectedSlot);
            case OFFHAND:
                return OFFHAND_SLOT;
            case FEET:
                return ARMOR_BOOTS_SLOT;
            case LEGS:
                return ARMOR_LEGGINGS_SLOT;
            case CHEST:
                return ARMOR_CHESTPLATE_SLOT;
            case HEAD:
                return ARMOR_HELMET_SLOT;
        }
        return null;
    }

    /**
     * 获取主手装备槽位
     * 
     * @return 主手装备槽位
     */
    public static Slot getEquipSlot() {
        return getEquipSlot(EquipmentSlot.MAINHAND);
    }

    @Override
    public int inventorySlotToWindowSlot(int inventorySlot) {
        // 将库存槽位索引转换为窗口槽位索引
        // 库存前9个槽位（快捷栏）对应窗口槽位36-44
        if (inventorySlot < 9) {
            return inventorySlot + 36;
        }
        // 其他库存槽位在窗口中保持相同索引
        return inventorySlot;
    }

    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        // 将窗口槽位索引转换为库存槽位索引
        // 窗口槽位36及以上对应库存前9个槽位（快捷栏）
        if (windowSlot >= 36) {
            return windowSlot - 36;
        }
        // 其他窗口槽位在库存中保持相同索引
        return windowSlot;
    }

    @Override
    protected String getName() {
        return "玩家";
    }

}
