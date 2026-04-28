package adris.altoclef.util.slots;

/**
 * 锻造台槽位类
 * 表示Minecraft锻造台界面中的各种槽位
 */
public class SmithingTableSlot extends Slot {
    /** 模板输入槽位 */
    public static final SmithingTableSlot INPUT_SLOT_TEMPLATE = new SmithingTableSlot(0);
    /** 工具输入槽位 */
    public static final SmithingTableSlot INPUT_SLOT_TOOL = new SmithingTableSlot(1);
    /** 材料输入槽位 */
    public static final SmithingTableSlot INPUT_SLOT_MATERIALS = new SmithingTableSlot(2);
    /** 输出槽位 */
    public static final SmithingTableSlot OUTPUT_SLOT = new SmithingTableSlot(3);

    /**
     * 构造函数
     *
     * @param slot 槽位索引
     */
    public SmithingTableSlot(int slot) {
        this(slot, false);
    }

    /**
     * 构造函数
     *
     * @param slot      槽位索引
     * @param inventory 是否为库存槽位
     */
    SmithingTableSlot(int slot, boolean inventory) {
        super(slot, inventory);
    }

    /**
     * 将库存槽位索引转换为窗口槽位索引
     *
     * @param inventorySlot 库存槽位索引
     * @return 窗口槽位索引
     */
    @Override
    public int inventorySlotToWindowSlot(int inventorySlot) {
        if (inventorySlot < 9) {
            return inventorySlot + 30;
        }
        return inventorySlot - 6;
    }

    /**
     * 将窗口槽位索引转换为库存槽位索引
     *
     * @param windowSlot 窗口槽位索引
     * @return 库存槽位索引
     */
    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        if (windowSlot >= 30) {
            return windowSlot - 30;
        }
        return windowSlot + 6;
    }

    /**
     * 获取槽位名称
     *
     * @return 槽位名称
     */
    @Override
    protected String getName() {
        return "锻造台";
    }
}
