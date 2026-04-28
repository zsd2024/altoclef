package adris.altoclef.util.slots;

/**
 * 箱子槽位类
 * 用于处理箱子容器中的槽位映射和管理，支持普通箱子和大型箱子（双箱）
 */
public class ChestSlot extends Slot {

    /** 是否为大型箱子（双箱） */
    private final boolean big;

    /**
     * 构造箱子槽位对象
     * @param slot 槽位索引
     * @param big 是否为大型箱子
     */
    public ChestSlot(int slot, boolean big) {
        this(slot, big, false);
    }

    /**
     * 构造箱子槽位对象
     * @param slot 槽位索引
     * @param big 是否为大型箱子
     * @param inventory 是否为物品栏槽位
     */
    public ChestSlot(int slot, boolean big, boolean inventory) {
        super(slot, inventory);
        this.big = big;
    }

    @Override
    public int inventorySlotToWindowSlot(int inventorySlot) {
        if (inventorySlot < 9) {
            return inventorySlot + (big ? 81 : 54);
        }
        return (inventorySlot - 9) + (big ? 54 : 27);
    }

    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        int bottomStart = (big ? 81 : 54);
        if (windowSlot >= bottomStart) {
            return windowSlot - bottomStart;
        }
        return (windowSlot + 9) - (big ? 54 : 27);
    }

    @Override
    protected String getName() {
        return "箱子";
    }
}
