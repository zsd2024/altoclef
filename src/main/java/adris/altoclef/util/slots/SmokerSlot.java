package adris.altoclef.util.slots;

/**
 * 烟熏炉槽位类
 * 表示Minecraft烟熏炉中的各种槽位，包括燃料输入槽、材料输入槽和输出槽
 */
public class SmokerSlot extends Slot {
    /** 燃料输入槽位（窗口槽位索引为1） */
    public static final SmokerSlot INPUT_SLOT_FUEL = new SmokerSlot(1);
    /** 材料输入槽位（窗口槽位索引为0） */
    public static final SmokerSlot INPUT_SLOT_MATERIALS = new SmokerSlot(0);
    /** 输出槽位（窗口槽位索引为2） */
    public static final SmokerSlot OUTPUT_SLOT = new SmokerSlot(2);

    /**
     * 构造函数（窗口槽位模式）
     * 
     * @param windowSlot 窗口槽位索引
     */
    public SmokerSlot(int windowSlot) {
        this(windowSlot, false);
    }

    /**
     * 构造函数（通用模式）
     * 
     * @param slot 箱格索引
     * @param inventory 是否为库存槽位
     */
    protected SmokerSlot(int slot, boolean inventory) {
        super(slot, inventory);
    }

    /**
     * 将库存槽位索引转换为窗口槽位索引
     * 烟熏炉的库存槽位布局：
     * - 库存槽位0-8对应窗口槽位30-38（玩家热键栏）
     * - 其他库存槽位对应窗口槽位减6
     * 
     * @param inventorySlot 库存槽位索引
     * @return 对应的窗口槽位索引
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
     * 烟熏炉的窗口槽位布局：
     * - 窗口槽位30及以上对应库存槽位减30（玩家热键栏）
     * - 其他窗口槽位对应库存槽位加6
     * 
     * @param windowSlot 窗口槽位索引
     * @return 对应的库存槽位索引
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
     * @return 槽位名称 "烟熏炉"
     */
    @Override
    protected String getName() {
        return "烟熏炉";
    }
}
