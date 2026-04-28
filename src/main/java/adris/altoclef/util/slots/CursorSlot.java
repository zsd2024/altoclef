package adris.altoclef.util.slots;

/**
 * 光标槽位类
 * 表示玩家鼠标光标持有的物品槽位
 */
public class CursorSlot extends Slot {

    /** 光标槽位的单例实例 */
    public static final CursorSlot SLOT = new CursorSlot();

    /**
     * 构造函数
     * 初始化光标槽位，使用预定义的光标槽位索引
     */
    public CursorSlot() {
        super(Slot.CURSOR_SLOT_INDEX, true);
    }

    @Override
    protected int inventorySlotToWindowSlot(int inventorySlot) {
        // 光标槽位在库存槽位和窗口槽位中都使用相同的索引（-1）
        return Slot.CURSOR_SLOT_INDEX;
    }

    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        // 光标槽位在库存槽位和窗口槽位中都使用相同的索引（-1）
        return Slot.CURSOR_SLOT_INDEX;
    }

    @Override
    protected String getName() {
        return "光标槽位";
    }
}
