package adris.altoclef.util.slots;

import java.util.stream.IntStream;

/**
 * 工作台槽位类
 * 用于处理工作台容器中的槽位映射和管理
 */
public class CraftingTableSlot extends Slot {
    /** 合成输出槽位 */
    public static final CraftingTableSlot OUTPUT_SLOT = new CraftingTableSlot(0);

    /** 3x3合成输入槽位数组 */
    public static final CraftingTableSlot[] INPUT_SLOTS = IntStream.range(0, 9).mapToObj(ind -> getInputSlot(ind, true)).toArray(CraftingTableSlot[]::new);

    /**
     * 构造工作台槽位对象
     * @param windowSlot 窗口槽位索引
     */
    public CraftingTableSlot(int windowSlot) {
        this(windowSlot, false);
    }

    /**
     * 受保护的构造函数，用于创建工作台槽位
     * @param slot 槽位索引
     * @param inventory 是否为物品栏槽位
     */
    protected CraftingTableSlot(int slot, boolean inventory) {
        super(slot, inventory);
    }

    /**
     * 根据坐标获取输入槽位
     * @param x X坐标（0-2）
     * @param y Y坐标（0-2）
     * @return 对应的槽位对象
     */
    public static CraftingTableSlot getInputSlot(int x, int y) {
        return getInputSlot(y * 3 + x, true);
    }

    /**
     * 根据索引获取输入槽位
     * @param index 槽位索引（0-8）
     * @param big 是否为大型合成配方
     * @return 对应的槽位对象
     */
    public static CraftingTableSlot getInputSlot(int index, boolean big) {
        index += 1;
        if (big) {
            // 默认的3x3大型配方
            return new CraftingTableSlot(index);
        } else {
            // 小型配方在大型窗口中的位置
            int x = index % 2;
            int y = index / 2;
            return getInputSlot(x, y);
        }
    }

    @Override
    public int inventorySlotToWindowSlot(int inventorySlot) {
        if (inventorySlot < 9) {
            return inventorySlot + 37;
        }
        return inventorySlot + 1;
    }

    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        if (windowSlot >= 37) {
            return windowSlot - 37;
        }
        return windowSlot - 1;
    }

    @Override
    protected String getName() {
        return "工作台";
    }
}
