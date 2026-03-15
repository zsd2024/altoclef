package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.BotBehaviour;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.container.PickupFromContainerTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.resources.MineAndCollectTask;
import adris.altoclef.tasks.slot.EnsureFreePlayerCraftingGridTask;
import adris.altoclef.tasks.slot.MoveInaccessibleItemToInventoryTask;
import adris.altoclef.tasksystem.ITaskCanForce;
import adris.altoclef.tasksystem.ITaskUsesCraftingGrid;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 所有"收集物品"任务的父类
 * <p>
 * 如果目标物品在地面上或在箱子中，会优先从这些来源获取
 */
public abstract class ResourceTask extends Task implements ITaskCanForce {

    // 物品目标数组
    protected final ItemTarget[] itemTargets;

    // 拾取掉落物品任务
    private final PickupDroppedItemTask pickupTask;
    // 确保玩家制作网格空闲任务
    private final EnsureFreePlayerCraftingGridTask ensureFreeCraftingGridTask = new EnsureFreePlayerCraftingGridTask();
    // 当前容器缓存
    private ContainerCache currentContainer;
    // 额外资源参数
    // 如果存在则挖掘的方块
    private Block[] mineIfPresent = null;
    // 是否强制维度
    private boolean forceDimension = false;
    // 是否允许使用容器
    private boolean allowContainers = false;
    // 目标维度
    private Dimension targetDimension;
    // 最近挖掘位置
    private BlockPos mineLastClosest = null;

    /**
     * 构造函数，指定物品目标数组
     * @param itemTargets 物品目标数组
     */
    public ResourceTask(ItemTarget[] itemTargets) {
        this.itemTargets = itemTargets;
        pickupTask = new PickupDroppedItemTask(this.itemTargets, true);
    }

    /**
     * 构造函数，指定单一物品目标
     * @param target 物品目标
     */
    public ResourceTask(ItemTarget target) {
        this(new ItemTarget[]{target});
    }

    /**
     * 构造函数，指定物品和目标数量
     * @param item 物品
     * @param targetCount 目标数量
     */
    public ResourceTask(Item item, int targetCount) {
        this(new ItemTarget(item, targetCount));
    }

    @Override
    public boolean isFinished() {
        // 检查是否已完成任务（除了光标槽外的物品栏已满足目标）
        return StorageHelper.itemTargetsMetInventoryNoCursor(itemTargets);
    }

    @Override
    public boolean shouldForce(Task interruptingCandidate) {
        // 如果光标槽中有重要的目标物品，应该强制执行
        return StorageHelper.itemTargetsMetInventory(itemTargets) && !isFinished()
                // 这应该是多余的，但为了确保100%正确而设置的保护
                && Arrays.stream(itemTargets).anyMatch(target -> target.matches(StorageHelper.getItemStackInCursorSlot().getItem()));
    }

    @Override
    protected void onStart() {
        BotBehaviour botBehaviour = AltoClef.getInstance().getBehaviour();

        botBehaviour.push();
        //removeThrowawayItems(_itemTargets);
        botBehaviour.addProtectedItems(ItemTarget.getMatches(itemTargets));

        onResourceStart(AltoClef.getInstance());
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果物品在不可访问的物品栏槽位中
        if (!(thisOrChildSatisfies(task -> task instanceof ITaskUsesCraftingGrid)) || ensureFreeCraftingGridTask.isActive()) {
            for (ItemTarget target : itemTargets) {
                if (StorageHelper.isItemInaccessibleToContainer(mod, target)) {
                    setDebugState("从特殊物品栏槽位移动物品");
                    return new MoveInaccessibleItemToInventoryTask(target);
                }
            }
        }
        // 如果物品栏中已满足目标（包含光标槽），只需从光标移动物品
        if (StorageHelper.itemTargetsMetInventory(itemTargets) && Arrays.stream(itemTargets).anyMatch(target -> target.matches(StorageHelper.getItemStackInCursorSlot().getItem()))) {
            setDebugState("从光标槽移动物品");
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(StorageHelper.getItemStackInCursorSlot(), false);
            if (moveTo.isPresent()) {
                mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                return null;
            }
            if (ItemHelper.canThrowAwayStack(mod, StorageHelper.getItemStackInCursorSlot())) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                return null;
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // 如果光标槽是垃圾，尝试丢弃
            if (garbage.isPresent()) {
                mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                return null;
            }
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            return null;
        }

        if (!shouldAvoidPickingUp(mod)) {
            // 检查物品是否在地面上，如果是则拾取
            if (mod.getEntityTracker().itemDropped(itemTargets)) {

                // 如果正在拾取镐子（我们无法深入地下或挖矿）
                if (PickupDroppedItemTask.isIsGettingPickaxeFirst(mod)) {
                    if (pickupTask.isCollectingPickaxeForThis()) {
                        setDebugState("拾取（优先拾取镐子！）");
                        // 我们的拾取任务是收集镐子，继续执行
                        return pickupTask;
                    }
                    // 只获取离我们近的物品
                    Optional<ItemEntity> closest = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), itemTargets);
                    if (closest.isPresent() && !closest.get().isInRange(mod.getPlayer(), 10)) {
                        return onResourceTick(mod);
                    }
                }

                double range = getPickupRange(mod);
                Optional<ItemEntity> closest = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), itemTargets);
                if (range < 0 || (closest.isPresent() && closest.get().isInRange(mod.getPlayer(), range)) || (pickupTask.isActive() && !pickupTask.isFinished())) {
                    setDebugState("拾取物品");
                    return pickupTask;
                }
            }
        }

        // 检查箱子并从中获取资源
        if (currentContainer == null && allowContainers) {
            List<ContainerCache> containersWithItem = mod.getItemStorage().getContainersWithItem(Arrays.stream(itemTargets).reduce(new Item[0], (items, target) -> ArrayUtils.addAll(items, target.getMatches()), ArrayUtils::addAll));
            if (!containersWithItem.isEmpty()) {
                ContainerCache closest = containersWithItem.stream().min(StlHelper.compareValues(container -> BlockPosVer.getSquaredDistance(container.getBlockPos(),mod.getPlayer().getPos()))).get();
                if (closest.getBlockPos().isWithinDistance(mod.getPlayer().getPos(), mod.getModSettings().getResourceChestLocateRange())) {
                    currentContainer = closest;
                }
            }
        }
        if (currentContainer != null) {
            Optional<ContainerCache> container = mod.getItemStorage().getContainerAtPosition(currentContainer.getBlockPos());
            if (container.isPresent()) {
                if (Arrays.stream(itemTargets).noneMatch(target -> container.get().hasItem(target.getMatches()))) {
                    currentContainer = null;
                } else {
                    // 我们有当前箱子，从其中获取
                    setDebugState("从容器拾取");
                    return new PickupFromContainerTask(currentContainer.getBlockPos(), itemTargets);
                }
            } else {
                currentContainer = null;
            }
        }

        // 如果找到方块，可能会挖掘
        if (mineIfPresent != null) {
            ArrayList<Block> satisfiedReqs = new ArrayList<>(Arrays.asList(mineIfPresent));
            satisfiedReqs.removeIf(block -> !StorageHelper.miningRequirementMet(MiningRequirement.getMinimumRequirementForBlock(block)));
            if (!satisfiedReqs.isEmpty()) {
                if (mod.getBlockScanner().anyFound(satisfiedReqs.toArray(Block[]::new))) {
                    Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock(mineIfPresent);
                    if (closest.isPresent() && closest.get().isWithinDistance(mod.getPlayer().getPos(), mod.getModSettings().getResourceMineRange())) {
                        mineLastClosest = closest.get();
                    }
                    if (mineLastClosest != null) {
                        if (mineLastClosest.isWithinDistance(mod.getPlayer().getPos(), mod.getModSettings().getResourceMineRange() * 1.5 + 20)) {
                            return new MineAndCollectTask(itemTargets, mineIfPresent, MiningRequirement.HAND);
                        }
                    }
                }
            }
        }
        // 确保物品不会卡在玩家制作网格中。如果未来的任务不是资源任务，可能会有问题。
        if (StorageHelper.isPlayerInventoryOpen()) {
            if (!(thisOrChildSatisfies(task -> task instanceof ITaskUsesCraftingGrid)) || ensureFreeCraftingGridTask.isActive()) {
                for (Slot slot : PlayerSlot.CRAFT_INPUT_SLOTS) {
                    if (!StorageHelper.getItemStackInSlot(slot).isEmpty()) {
                        return ensureFreeCraftingGridTask;
                    }
                }
            }
        }
        return onResourceTick(mod);
    }

    /**
     * 获取拾取范围
     * @param mod AltoClef实例
     * @return 返回拾取范围
     */
    protected double getPickupRange(AltoClef mod) {
        return mod.getModSettings().getResourcePickupRange();
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getBehaviour().pop();
        onResourceStop(AltoClef.getInstance(), interruptTask);
    }

    @Override
    protected boolean isEqual(Task other) {
        // 相同的目标物品
        if (other instanceof ResourceTask t) {
            if (!isEqualResource(t)) return false;
            return Arrays.equals(t.itemTargets, itemTargets);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        StringBuilder result = new StringBuilder();
        result.append(toDebugStringName()).append(": [");
        int c = 0;
        if (itemTargets != null) {
            for (ItemTarget target : itemTargets) {
                result.append(target != null ? target.toString() : "(null)");
                if (++c != itemTargets.length) {
                    result.append(", ");
                }
            }
        }
        result.append("]");
        return result.toString();
    }

    /**
     * 检查是否在错误的维度
     * @param mod AltoClef实例
     * @return 如果在错误的维度返回true
     */
    protected boolean isInWrongDimension(AltoClef mod) {
        if (forceDimension) {
            return WorldHelper.getCurrentDimension() != targetDimension;
        }
        return false;
    }

    /**
     * 获取前往正确维度的任务
     * @param mod AltoClef实例
     * @return 返回前往正确维度的任务
     */
    protected Task getToCorrectDimensionTask(AltoClef mod) {
        return new DefaultGoToDimensionTask(targetDimension);
    }

    /**
     * 设置如果存在则挖掘的方块
     * @param toMine 要挖掘的方块数组
     * @return 返回当前资源任务实例
     */
    public ResourceTask mineIfPresent(Block[] toMine) {
        mineIfPresent = toMine;
        return this;
    }

    /**
     * 强制前往指定维度
     * @param dimension 目标维度
     * @return 返回当前资源任务实例
     */
    public ResourceTask forceDimension(Dimension dimension) {
        forceDimension = true;
        targetDimension = dimension;
        return this;
    }

    /**
     * 设置是否允许使用容器
     * @param value 是否允许使用容器
     */
    public void setAllowContainers(boolean value) {
        this.allowContainers = value;
    }

    /**
     * 获取是否允许使用容器
     * @return 如果允许使用容器返回true
     */
    public boolean getAllowContainers() {
        return allowContainers;
    }

    /**
     * 是否应避免拾取物品
     * @param mod AltoClef实例
     * @return 如果应避免拾取返回true
     */
    protected abstract boolean shouldAvoidPickingUp(AltoClef mod);

    /**
     * 资源任务开始时执行
     * @param mod AltoClef实例
     */
    protected abstract void onResourceStart(AltoClef mod);

    /**
     * 资源任务每次tick时执行
     * @param mod AltoClef实例
     * @return 返回应执行的任务
     */
    protected abstract Task onResourceTick(AltoClef mod);

    /**
     * 资源任务停止时执行
     * @param mod AltoClef实例
     * @param interruptTask 中断任务
     */
    protected abstract void onResourceStop(AltoClef mod, Task interruptTask);

    /**
     * 比较是否与另一个资源任务相等
     * @param other 另一个资源任务
     * @return 如果相等返回true
     */
    protected abstract boolean isEqualResource(ResourceTask other);

    /**
     * 获取调试字符串名称
     * @return 返回调试字符串名称
     */
    protected abstract String toDebugStringName();

    /**
     * 获取物品目标数组
     * @return 返回物品目标数组
     */
    public ItemTarget[] getItemTargets() {
        return itemTargets;
    }
}
