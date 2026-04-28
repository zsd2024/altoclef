package adris.altoclef.util.slots;

/**
 * 酿造台槽位类
 * 用于处理酿造台容器中的槽位映射和管理
 */
public class BrewingStandSlot extends Slot {
    /** 左侧药水槽位 */
    public static final BrewingStandSlot LEFT_POTION = new BrewingStandSlot(0);
    /** 中间药水槽位 */
    public static final BrewingStandSlot MIDDLE_POTION = new BrewingStandSlot(1);
    /** 右侧药水槽位 */
    public static final BrewingStandSlot RIGHT_POTION = new BrewingStandSlot(2);
    /** 配方材料槽位（顶部） */
    public static final BrewingStandSlot INGREDIENT = new BrewingStandSlot(3);
    /** 燃料槽位（烈焰粉） */
    public static final BrewingStandSlot FUEL = new BrewingStandSlot(4);

    /**
     * 构造酿造台槽位对象
     * @param slot 槽位索引
     */
    public BrewingStandSlot(int slot) {
        this(slot, false);
    }

    /**
     * 受保护的构造函数，用于创建酿造台槽位
     * @param slot 槽位索引
     * @param inventory 是否为物品栏槽位
     */
    protected BrewingStandSlot(int slot, boolean inventory) {
        super(slot, inventory);
    }

    @Override
    public int inventorySlotToWindowSlot(int inventorySlot) {
        if (inventorySlot < 9) {
            return inventorySlot + 32;
        }
        return inventorySlot - 4;
    }

    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        if (windowSlot >= 32) {
            return windowSlot - 32;
        }
        return windowSlot + 4;
    }

    @Override
    protected String getName() {
        return "酿造台";
    }
}
