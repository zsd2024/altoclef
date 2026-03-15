package adris.altoclef.trackers.storage;

import adris.altoclef.util.Dimension;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * 容器缓存 - 缓存容器中的物品信息
 */
public class ContainerCache {

    private final BlockPos blockPos; // 方块位置
    private final Dimension dimension; // 维度
    private final ContainerType containerType; // 容器类型

    private final HashMap<Item, Integer> itemCounts = new HashMap<>(); // 物品数量映射
    private int _emptySlots; // 空槽数量

    /**
     * 构造容器缓存
     * @param dimension 维度
     * @param blockPos 方块位置
     * @param containerType 容器类型
     */
    public ContainerCache(Dimension dimension, BlockPos blockPos, ContainerType containerType) {
        this.dimension = dimension;
        this.blockPos = blockPos;
        this.containerType = containerType;
    }

    /**
     * 更新容器缓存
     * @param screenHandler 屏幕处理器
     * @param onStack 物品堆栈回调
     */
    public void update(ScreenHandler screenHandler, Consumer<ItemStack> onStack) {
        itemCounts.clear();
        _emptySlots = 0;
        int start = 0;
        int end = screenHandler.slots.size() - (4 * 9); // 减去玩家物品栏
        // 不要将熔炉输出槽计算为空槽，因为它无法被使用。
        boolean isFurnace = (screenHandler instanceof FurnaceScreenHandler);

        // 遍历所有存储槽
        for (int i = start; i < end; ++i) {
            ItemStack stack = screenHandler.slots.get(i).getStack().copy();

            if (stack.isEmpty()) {
                // 忽略熔炉输出槽
                if (!(isFurnace && i == 2)) {
                    _emptySlots++;
                }
            } else {
                Item item = stack.getItem();
                int count = stack.getCount();
                itemCounts.put(item, itemCounts.getOrDefault(item, 0) + count);
                onStack.accept(stack);
            }
        }
    }

    /**
     * 获取物品数量
     * @param items 物品数组
     * @return 物品数量
     */
    public int getItemCount(Item... items) {
        int result = 0;
        for (Item item : items) {
            result += itemCounts.getOrDefault(item, 0);
        }
        return result;
    }

    /**
     * 检查是否包含指定物品
     * @param items 物品数组
     * @return 是否包含
     */
    public boolean hasItem(Item... items) {
        for (Item item : items) {
            if (itemCounts.containsKey(item) && itemCounts.get(item) > 0)
                return true;
        }
        return false;
    }

    /**
     * 获取空槽数量
     * @return 空槽数量
     */
    public int getEmptySlotCount() {
        return _emptySlots;
    }

    /**
     * 检查是否已满
     * @return 是否已满
     */
    public boolean isFull() {
        return _emptySlots == 0;
    }

    /**
     * 获取方块位置
     * @return 方块位置
     */
    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * 获取容器类型
     * @return 容器类型
     */
    public ContainerType getContainerType() {
        return containerType;
    }

    /**
     * 获取维度
     * @return 维度
     */
    public Dimension getDimension() {
        return dimension;
    }
}
