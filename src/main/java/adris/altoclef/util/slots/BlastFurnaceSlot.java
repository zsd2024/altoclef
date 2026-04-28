package adris.altoclef.util.slots;

/**
 * 高炉槽位类
 * 用于处理高炉容器中的槽位映射和管理
 */
public class BlastFurnaceSlot extends Slot {
    /** 燃料输入槽位（高炉右侧） */
    public static final BlastFurnaceSlot INPUT_SLOT_FUEL = new BlastFurnaceSlot(1);
    /** 材料输入槽位（高炉左侧） */
    public static final BlastFurnaceSlot INPUT_SLOT_MATERIALS = new BlastFurnaceSlot(0);
    /** 输出槽位（高炉底部） */
    public static final BlastFurnaceSlot OUTPUT_SLOT = new BlastFurnaceSlot(2);

    /**
     * 构造高炉槽位对象
     * @param windowSlot 窗口槽位索引
     */
    public BlastFurnaceSlot(int windowSlot) {
        this(windowSlot, false);
    }

    /**
     * 受保护的构造函数，用于创建高炉槽位
     * @param slot 槽位索引
     * @param inventory 是否为物品栏槽位
     */
    protected BlastFurnaceSlot(int slot, boolean inventory) {
        super(slot, inventory);
    }

    @Override
    public int inventorySlotToWindowSlot(int inventorySlot) {
        if (inventorySlot < 9) {
            return inventorySlot + 30;
        }
        return inventorySlot - 6;
    }

    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        if (windowSlot >= 30) {
            return windowSlot - 30;
        }
        return windowSlot + 6;
    }

    @Override
    protected String getName() {
        return "高炉";
    }
}
