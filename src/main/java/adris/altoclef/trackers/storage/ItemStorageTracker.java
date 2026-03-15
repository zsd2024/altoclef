package adris.altoclef.trackers.storage;

import adris.altoclef.AltoClef;
import adris.altoclef.trackers.Tracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 物品存储跟踪器 - 访问所有形式的存储
 */
public class ItemStorageTracker extends Tracker {

    private final InventorySubTracker inventory; // 物品栏子跟踪器
    private final ContainerSubTracker containers; // 容器子跟踪器

    public ItemStorageTracker(AltoClef mod, TrackerManager manager, Consumer<ContainerSubTracker> containerTrackerConsumer) {
        super(manager);
        inventory = new InventorySubTracker(manager);
        containers = new ContainerSubTracker(manager);
        containerTrackerConsumer.accept(containers);
    }

    /**
     * 获取当前转换槽位（例如合成、熔炉等）
     * @return 当前转换槽位数组
     */
    private static Slot[] getCurrentConversionSlots() {
        // TODO: 铁砧输入，其他任何...
        if (StorageHelper.isPlayerInventoryOpen()) {
            return PlayerSlot.CRAFT_INPUT_SLOTS;
        } else if (StorageHelper.isBigCraftingOpen()) {
            return CraftingTableSlot.INPUT_SLOTS;
        } else if (StorageHelper.isFurnaceOpen()) {
            return new Slot[]{FurnaceSlot.INPUT_SLOT_FUEL, FurnaceSlot.INPUT_SLOT_MATERIALS};
        } else if (StorageHelper.isSmokerOpen()) {
            return new Slot[]{SmokerSlot.INPUT_SLOT_FUEL, SmokerSlot.INPUT_SLOT_MATERIALS};
        } else if (StorageHelper.isBlastFurnaceOpen()) {
            return new Slot[]{BlastFurnaceSlot.INPUT_SLOT_FUEL, BlastFurnaceSlot.INPUT_SLOT_MATERIALS};
        }
        return new Slot[0];
    }

    /**
     * 获取玩家物品栏中的物品数量 或者 如果玩家正在转换过程中使用它
     * （例如合成台槽位/熔炉输入，玩家正在使用的物品）
     */
    public int getItemCount(Item... items) {
        int inConversionSlots = Arrays.stream(getCurrentConversionSlots()).mapToInt(slot -> {
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            if (ArrayUtils.contains(items, stack.getItem())) {
                return stack.getCount();
            }
            return 0;
        }).reduce(0, Integer::sum);
        return inventory.getItemCount(true, false, items) + inConversionSlots;
    }

    /**
     * 获取物品目标的物品数量
     * @param targets 物品目标数组
     * @return 物品数量
     */
    public int getItemCount(ItemTarget... targets) {
        return Arrays.stream(targets).mapToInt(target -> getItemCount(target.getMatches())).reduce(0, Integer::sum);
    }

    /**
     * 获取屏幕上任何槽位中可见的物品数量
     */
    public int getItemCountScreen(Item... items) {
        return inventory.getItemCount(true, true, items);
    }

    /**
     * 严格获取在玩家物品栏中的物品数量。
     * <p>
     * 仅在获取物品是最终目标时使用此方法。此方法将
     * 不计算合成/熔炉槽位中的物品！
     */
    public int getItemCountInventoryOnly(Item... items) {
        return inventory.getItemCount(true, false, items);
    }

    /**
     * 仅获取当前打开的容器中的物品数量，不是玩家的物品栏。
     */
    public int getItemCountContainer(Item... items) {
        return inventory.getItemCount(false, true, items);
    }

    /**
     * 获取物品是否在玩家物品栏中 或者 如果玩家正在转换过程中使用它
     * （例如合成台槽位/熔炉输入，玩家正在使用的物品）
     */
    public boolean hasItem(Item... items) {
        return Arrays.stream(getCurrentConversionSlots()).anyMatch(slot -> {
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            return ArrayUtils.contains(items, stack.getItem());
        }) || inventory.hasItem(true, items);
    }

    /**
     * 检查物品是否存在
     * @param playerInventoryOnly 是否仅检查玩家物品栏
     * @param items 物品数组
     * @return 是否存在
     */
    public boolean hasItem(boolean playerInventoryOnly, Item... items) {
        return inventory.hasItem(playerInventoryOnly, items);
    }

    /**
     * 检查副手是否有指定物品
     * @param item 物品
     * @return 副手是否有指定物品
     */
    public boolean hasItemInOffhand(Item item) {
        ItemStack offhand = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
        return offhand.getItem() == item;
    }

    /**
     * 检查是否拥有所有指定物品
     * @param items 物品数组
     * @return 是否拥有所有物品
     */
    public boolean hasItemAll(Item... items) {
        return Arrays.stream(items).allMatch(this::hasItem);
    }

    /**
     * 检查是否拥有物品目标中的物品
     * @param targets 物品目标数组
     * @return 是否拥有物品
     */
    public boolean hasItem(ItemTarget... targets) {
        return Arrays.stream(targets).anyMatch(target -> hasItem(target.getMatches()));
    }

    /**
     * 返回物品是否在屏幕上任何槽位中可见
     */
    public boolean hasItemScreen(Item... items) {
        return inventory.hasItem(false, items);
    }

    /**
     * 返回玩家是否仅在其物品栏中有物品。
     * <p>
     * 仅在获取物品是最终目标时使用此方法。此方法将
     * 不计算合成/熔炉槽位中的物品！
     */
    public boolean hasItemInventoryOnly(Item... items) {
        return inventory.hasItem(true, items);
    }

    /**
     * 返回包含任何给定物品的所有槽位。
     */
    public List<Slot> getSlotsWithItemScreen(Item... items) {
        return inventory.getSlotsWithItems(true, true, items);
    }

    /**
     * 返回不包含在玩家物品栏中的所有包含给定物品的槽位。
     */
    public List<Slot> getSlotsWithItemContainer(Item... items) {
        return inventory.getSlotsWithItems(false, true, items);
    }

    /**
     * 返回我们玩家物品栏中包含任何给定物品的所有槽位。
     */
    public List<Slot> getSlotsWithItemPlayerInventory(boolean includeCraftArmorOffhand, Item... items) {
        List<Slot> results = inventory.getSlotsWithItems(true, false, items);
        // 检查其他槽位
        if (includeCraftArmorOffhand) {
            HashSet<Item> toCheck = new HashSet<>(Arrays.asList(items));
            for (Slot otherSlot : StorageHelper.INACCESSIBLE_PLAYER_SLOTS) {
                if (toCheck.contains(StorageHelper.getItemStackInSlot(otherSlot).getItem())) {
                    results.add(otherSlot);
                }
            }
        }
        return results;
    }

    /**
     * 获取玩家物品栏中的物品堆栈列表
     * @param includeCursorSlot 是否包含光标槽位
     * @return 物品堆栈列表
     */
    public List<ItemStack> getItemStacksPlayerInventory(boolean includeCursorSlot) {
        return inventory.getInventoryStacks(includeCursorSlot);
    }

    /**
     * 获取玩家物品栏中可以容纳物品堆的所有槽位。
     *
     * @param stack         要"容纳"/放置在物品栏中的物品堆。
     * @param acceptPartial 如果为true，可以容纳物品堆的部分。如果为false，则要求100%的物品堆都能容纳。
     */
    public List<Slot> getSlotsThatCanFitInPlayerInventory(ItemStack stack, boolean acceptPartial) {
        return inventory.getSlotsThatCanFit(true, false, stack, acceptPartial);
    }

    /**
     * 获取玩家物品栏中可以容纳物品堆的槽位
     * @param stack 物品堆
     * @param acceptPartial 是否接受部分容纳
     * @return 可以容纳物品堆的槽位
     */
    public Optional<Slot> getSlotThatCanFitInPlayerInventory(ItemStack stack, boolean acceptPartial) {
        List<Slot> slots = getSlotsThatCanFitInPlayerInventory(stack, acceptPartial);
        if (!slots.isEmpty()) {
            for (Slot slot : slots) {
                return Optional.ofNullable(slot);
            }
        }
        return Optional.empty();
    }

    /**
     * 获取当前打开的容器中可以容纳物品堆的所有槽位，不包括玩家物品栏。
     *
     * @param stack         要"容纳"/放置在容器中的物品堆。
     * @param acceptPartial 如果为true，可以容纳物品堆的部分。如果为false，则要求100%的物品堆都能容纳。
     */
    public List<Slot> getSlotsThatCanFitInOpenContainer(ItemStack stack, boolean acceptPartial) {
        return inventory.getSlotsThatCanFit(false, true, stack, acceptPartial);
    }

    /**
     * 获取当前打开的容器中可以容纳物品堆的槽位
     * @param stack 物品堆
     * @param acceptPartial 是否接受部分容纳
     * @return 可以容纳物品堆的槽位
     */
    public Optional<Slot> getSlotThatCanFitInOpenContainer(ItemStack stack, boolean acceptPartial) {
        List<Slot> slots = getSlotsThatCanFitInOpenContainer(stack, acceptPartial);
        if (!slots.isEmpty()) {
            for (Slot slot : slots) {
                return Optional.ofNullable(slot);
            }
        }
        return Optional.empty();
    }

    /**
     * 获取可以容纳物品堆的所有槽位。
     *
     * @param stack         要"容纳"/放置在物品栏中的物品堆。
     * @param acceptPartial 如果为true，可以容纳物品堆的部分。如果为false，则要求100%的物品堆都能容纳。
     */
    public List<Slot> getSlotsThatCanFitScreen(ItemStack stack, boolean acceptPartial) {
        return inventory.getSlotsThatCanFit(true, true, stack, acceptPartial);
    }

    /**
     * 检查是否有空的物品栏槽位
     * @return 是否有空的物品栏槽位
     */
    public boolean hasEmptyInventorySlot() {
        return inventory.hasEmptySlot(true);
    }

    /**
     * 注册槽位操作
     */
    public void registerSlotAction() {
        inventory.setDirty();
    }

    /**
     * 返回物品是否存在于某个容器中。你可以过滤掉
     * 你不喜欢的容器。
     */
    public boolean hasItemContainer(Predicate<ContainerCache> accept, Item... items) {
        return containers.hasItem(accept, items);
    }

    /**
     * 返回物品是否存在于任何容器中，无论距离多远。
     */
    public boolean hasItemContainer(Item... items) {
        return containers.hasItem(items);
    }

    /**
     * 获取指定位置的容器缓存
     * @param pos 位置
     * @return 容器缓存
     */
    public Optional<ContainerCache> getContainerAtPosition(BlockPos pos) {
        return containers.getContainerAtPosition(pos);
    }

    /**
     * 检查容器是否已缓存
     * @param pos 位置
     * @return 是否已缓存
     */
    public boolean isContainerCached(BlockPos pos) {
        return getContainerAtPosition(pos).isPresent();
    }

    /**
     * 获取末影箱存储缓存
     * @return 末影箱存储缓存
     */
    public Optional<ContainerCache> getEnderChestStorage() {
        return containers.getEnderChestStorage();
    }

    /**
     * 获取满足条件的已缓存容器列表
     * @param accept 接受条件
     * @return 容器缓存列表
     */
    public List<ContainerCache> getCachedContainers(Predicate<ContainerCache> accept) {
        return containers.getCachedContainers(accept);
    }

    /**
     * 获取指定类型的已缓存容器列表
     * @param types 容器类型数组
     * @return 容器缓存列表
     */
    public List<ContainerCache> getCachedContainers(ContainerType... types) {
        return containers.getCachedContainers(types);
    }

    /**
     * 获取所有已缓存的容器列表
     * @return 容器缓存列表
     */
    public List<ContainerCache> getCachedContainers() {
        return getCachedContainers(cache -> true);
    }

    /**
     * 获取距离指定位置最近的满足条件的容器
     * @param pos 位置
     * @param accept 接受条件
     * @return 最近的容器缓存
     */
    public Optional<ContainerCache> getContainerClosestTo(Vec3d pos, Predicate<ContainerCache> accept) {
        return containers.getClosestTo(pos, accept);
    }

    /**
     * 获取距离指定位置最近的指定类型容器
     * @param pos 位置
     * @param types 容器类型数组
     * @return 最近的容器缓存
     */
    public Optional<ContainerCache> getContainerClosestTo(Vec3d pos, ContainerType... types) {
        return containers.getClosestTo(pos, types);
    }

    /**
     * 获取距离指定位置最近的容器
     * @param pos 位置
     * @return 最近的容器缓存
     */
    public Optional<ContainerCache> getContainerClosestTo(Vec3d pos) {
        return getContainerClosestTo(pos, cache -> true);
    }

    /**
     * 获取包含指定物品的容器列表
     * @param items 物品数组
     * @return 容器缓存列表
     */
    public List<ContainerCache> getContainersWithItem(Item... items) {
        return containers.getContainersWithItem(items);
    }

    /**
     * 获取距离指定位置最近的包含指定物品的容器
     * @param pos 位置
     * @param items 物品数组
     * @return 最近的容器缓存
     */
    public Optional<ContainerCache> getClosestContainerWithItem(Vec3d pos, Item... items) {
        return containers.getClosestWithItem(pos, items);
    }

    /**
     * 获取最后一次方块位置交互
     * @return 最后一次方块位置交互
     */
    public Optional<BlockPos> getLastBlockPosInteraction() {
        return Optional.ofNullable(containers.getLastBlockPosInteraction());
    }

    @Override
    protected void updateState() {
        inventory.updateState();
        containers.updateState();
    }

    @Override
    protected void reset() {
        inventory.reset();
        containers.reset();
    }
}

