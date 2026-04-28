package adris.altoclef.util.slots;

import adris.altoclef.Debug;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;

import java.util.Iterator;
import java.util.Objects;

// 非常有用的链接
// 容器窗口槽位（用于在所有容器中移动物品，包括玩家）：
//      https://wiki.vg/Inventory
// 玩家库存槽位（仅用于获取库存物品）：
//      https://minecraft.gamepedia.com/Inventory

/**
 * 槽位基类
 * 抽象类，表示Minecraft中的各种槽位，提供库存槽位和窗口槽位之间的转换功能
 */
public abstract class Slot {

    /** 光标槽位索引（-1表示光标槽位，即光标持有物品时的槽位） */
    public static final int CURSOR_SLOT_INDEX = -1;
    /** 未定义槽位索引 */
    private static final int UNDEFINED_SLOT_INDEX = -999;
    /** 未定义槽位实例 */
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static Slot UNDEFINED = new PlayerSlot(UNDEFINED_SLOT_INDEX);
    /** 库存槽位索引 */
    private final int _inventorySlot;
    /** 窗口槽位索引 */
    private final int _windowSlot;
    /** 是否为库存槽位 */
    private final boolean _isInventory;

    /**
     * 构造函数
     * 
     * @param slot 箱格索引
     * @param inventory 是否为库存槽位
     */
    public Slot(int slot, boolean inventory) {
        _isInventory = inventory;
        if (inventory) {
            _inventorySlot = slot;
            _windowSlot = UNDEFINED_SLOT_INDEX;
            //_windowSlot = inventorySlotToWindowSlot(slot);
        } else {
            //_inventorySlot = windowSlotToInventorySlot(slot);
            _inventorySlot = UNDEFINED_SLOT_INDEX;
            _windowSlot = slot;
        }
    }

    /**
     * 从当前屏幕获取槽位（抽象方法）
     * 
     * @param slot 箱格索引
     * @param inventory 是否为库存槽位
     * @return 对应的槽位实例
     */
    private static Slot getFromCurrentScreenAbstract(int slot, boolean inventory) {
        switch (getCurrentType()) {
            case PLAYER:
                return new PlayerSlot(slot, inventory);
            case CRAFTING_TABLE:
                return new CraftingTableSlot(slot, inventory);
            case FURNACE_OR_SMITH_OR_SMOKER_OR_BLAST:
                return new FurnaceSlot(slot, inventory);
            case CHEST_LARGE:
                return new ChestSlot(slot, true, inventory);
            case CHEST_SMALL:
                return new ChestSlot(slot, false, inventory);
            default:
                Debug.logWarning("未处理的库存检查槽位: " + getCurrentType());
                return null;
        }
    }

    /**
     * 从当前屏幕获取窗口槽位
     * 
     * @param windowSlot 窗口槽位索引
     * @return 对应的槽位实例
     */
    public static Slot getFromCurrentScreen(int windowSlot) {
        return getFromCurrentScreenAbstract(windowSlot, false);
    }

    /**
     * 从当前屏幕获取库存槽位
     * 
     * @param inventorySlot 库存槽位索引
     * @return 对应的槽位实例
     */
    public static Slot getFromCurrentScreenInventory(int inventorySlot) {
        return getFromCurrentScreenAbstract(inventorySlot, true);
    }

    /**
     * 获取当前屏幕类型
     * 
     * @return 当前屏幕的容器类型
     */
    private static ContainerType getCurrentType() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof FurnaceScreen || screen instanceof SmithingScreen || screen instanceof SmokerScreen ||
                screen instanceof BlastFurnaceScreen) {
            return ContainerType.FURNACE_OR_SMITH_OR_SMOKER_OR_BLAST;
        }
        if (screen instanceof GenericContainerScreen) {
            GenericContainerScreenHandler handler = ((GenericContainerScreen) screen).getScreenHandler();
            boolean big = (handler.getRows() == 6);
            return big ? ContainerType.CHEST_LARGE : ContainerType.CHEST_SMALL;
        }
        if (screen instanceof CraftingScreen) {
            return ContainerType.CRAFTING_TABLE;
        }
        return ContainerType.PLAYER;
    }

    /**
     * 判断是否为光标槽位
     * 
     * @param slot 槽位实例
     * @return 如果是光标槽位则返回true，否则返回false
     */
    public static boolean isCursor(Slot slot) {
        return slot instanceof CursorSlot;
    }

    /**
     * 获取当前屏幕的所有槽位
     * 
     * @return 当前屏幕所有槽位的可迭代对象
     */
    public static Iterable<Slot> getCurrentScreenSlots() {
        return () -> new Iterator<>() {
            final ClientPlayerEntity player = MinecraftClient.getInstance().player;
            final ScreenHandler handler = player != null ? player.currentScreenHandler : null;
            final int MAX = handler != null ? handler.slots.size() : 0;
            int i = -1;

            @Override
            public boolean hasNext() {
                return i < MAX;
            }

            @Override
            public Slot next() {
                if (i == -1) {
                    ++i;
                    return new CursorSlot();
                }
                return Slot.getFromCurrentScreen(i++);
            }
        };
    }

    /**
     * 获取库存槽位索引
     * 
     * @return 库存槽位索引
     */
    public int getInventorySlot() {
        if (!_isInventory) {
            return windowSlotToInventorySlot(_windowSlot);
        }
        return _inventorySlot;
    }

    /**
     * 获取窗口槽位索引
     * 
     * @return 窗口槽位索引
     */
    public int getWindowSlot() {
        if (_isInventory) {
            return inventorySlotToWindowSlot(_inventorySlot);
        }
        return _windowSlot;
    }

    /**
     * 将库存槽位索引转换为窗口槽位索引
     * 
     * @param inventorySlot 库存槽位索引
     * @return 对应的窗口槽位索引
     */
    protected abstract int inventorySlotToWindowSlot(int inventorySlot);

    /**
     * 将窗口槽位索引转换为库存槽位索引
     * 
     * @param windowSlot 窗口槽位索引
     * @return 对应的库存槽位索引
     */
    protected abstract int windowSlotToInventorySlot(int windowSlot);

    /**
     * 获取槽位名称
     * 
     * @return 槽位名称
     */
    protected abstract String getName();

    @Override
    public String toString() {
        return getName() + (_isInventory ? "InventorySlot" : "Slot") + "{" +
                "inventory slot = " + getInventorySlot() +
                ", window slot = " + getWindowSlot() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Slot slot) {
            return getInventorySlot() == slot.getInventorySlot() && getWindowSlot() == slot.getWindowSlot();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInventorySlot(), getWindowSlot());
    }

    /**
     * 判断此槽位是否存在于玩家库存中或与玩家库存断开连接的容器中
     * 
     * @return 如果槽位在玩家库存中则返回true，否则返回false
     */
    public boolean isSlotInPlayerInventory() {
        ScreenHandler handler = MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.currentScreenHandler : null;
        int windowSlot = getWindowSlot();
        if (handler instanceof PlayerScreenHandler) {
            // 所有可见内容都是玩家库存
            return true;
        }
        int slotCount = handler != null ? handler.slots.size() : 0;
        return windowSlot >= (slotCount - (4 * 9));
    }

    /** 容器类型枚举 */
    enum ContainerType {
        PLAYER,
        CRAFTING_TABLE,
        CHEST_SMALL,
        CHEST_LARGE,
        FURNACE_OR_SMITH_OR_SMOKER_OR_BLAST
    }
}
