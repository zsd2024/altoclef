package adris.altoclef.util.slots;

/**
 * 熔炉槽位类
 * 表示熔炉界面中的各种槽位，包括燃料输入槽、材料输入槽和输出槽
 */
public class FurnaceSlot extends Slot {
    /** 燃料输入槽位（窗口槽位索引为1） */
    public static final FurnaceSlot INPUT_SLOT_FUEL = new FurnaceSlot(1);
    /** 材料输入槽位（窗口槽位索引为0） */
    public static final FurnaceSlot INPUT_SLOT_MATERIALS = new FurnaceSlot(0);
    /** 输出槽位（窗口槽位索引为2） */
    public static final FurnaceSlot OUTPUT_SLOT = new FurnaceSlot(2);

    /**
     * 构造函数
     * 
     * @param windowSlot 窗口槽位索引
     */
    public FurnaceSlot(int windowSlot) {
        this(windowSlot, false);
    }

    /**
     * 受保护的构造函数
     * 
     * @param slot 箱格索引
     * @param inventory 是否为库存槽位
     */
    protected FurnaceSlot(int slot, boolean inventory) {
        super(slot, inventory);
    }

    @Override
    public int inventorySlotToWindowSlot(int inventorySlot) {
        // 将库存槽位索引转换为窗口槽位索引
        // 库存前9个槽位（快捷栏）对应窗口槽位30-38
        if (inventorySlot < 9) {
            return inventorySlot + 30;
        }
        // 其他库存槽位对应窗口槽位减6（因为熔炉有3个专用槽位+27个主库存槽位）
        return inventorySlot - 6;
    }

    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        // 将窗口槽位索引转换为库存槽位索引
        // 窗口槽位30及以上对应库存前9个槽位（快捷栏）
        if (windowSlot >= 30) {
            return windowSlot - 30;
        }
        // 其他窗口槽位对应库存槽位加6
        return windowSlot + 6;
    }

    @Override
    protected String getName() {
        return "熔炉";
    }
}
