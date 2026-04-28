package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.slot.MoveItemToSlotFromInventoryTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 将物品从玩家背包移动到指定存储容器的任务
 */
public class StoreInContainerTask extends AbstractDoToStorageContainerTask {

    private final BlockPos targetContainer; // 目标容器的位置
    private final boolean getIfNotPresent; // 如果物品不存在是否获取
    private final ItemTarget[] toStore; // 要存储的物品目标数组

    private ContainerStoredTracker storedItems; // 已存储物品的跟踪器

    /**
     * 构造函数
     * @param targetContainer 目标容器的位置
     * @param getIfNotPresent 如果物品不存在是否获取
     * @param toStore 要存储的物品目标
     */
    public StoreInContainerTask(BlockPos targetContainer, boolean getIfNotPresent, ItemTarget... toStore) {
        this.targetContainer = targetContainer;
        this.getIfNotPresent = getIfNotPresent;
        this.toStore = toStore;
    }

    /**
     * 获取目标容器位置
     * @return 目标容器的位置
     */
    @Override
    protected Optional<BlockPos> getContainerTarget() {
        return Optional.of(targetContainer);
    }

    /**
     * 任务开始时的初始化
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (storedItems == null) {
            // 只考虑向我们指定的容器传输物品
            storedItems = new ContainerStoredTracker(slot -> {
                Optional<BlockPos> openContainer = AltoClef.getInstance().getItemStorage().getLastBlockPosInteraction();
                return openContainer.isPresent() && openContainer.get().equals(targetContainer);
            });
        }
        storedItems.startTracking();
    }

    /**
     * 每帧执行的任务逻辑
     * @return 下一个要执行的子任务
     */
    @Override
    protected Task onTick() {
        // 如果物品不足且"不存在时获取"为true，则获取更多物品
        if (getIfNotPresent) {
            for (ItemTarget target : toStore) {
                int inventoryNeed = target.getTargetCount() - storedItems.getStoredCount(target.getMatches());
                if (inventoryNeed > AltoClef.getInstance().getItemStorage().getItemCount(target)) {
                    return TaskCatalogue.getItemTask(new ItemTarget(target, inventoryNeed));
                }
            }
        }
        return super.onTick();
    }

    /**
     * 任务停止时的清理工作
     * @param interruptTask 中断任务
     */
    @Override
    protected void onStop(Task interruptTask) {
        super.onStop(interruptTask);
        storedItems.stopTracking();
    }

    /**
     * 容器打开时的子任务逻辑
     * @param mod AltoClef实例
     * @param containerCache 容器缓存
     * @return 下一个要执行的子任务
     */
    @Override
    protected Task onContainerOpenSubtask(AltoClef mod, ContainerCache containerCache) {
        // 移动所有不在容器中的物品
        for (ItemTarget target : storedItems.getUnstoredItemTargetsYouCanStore(mod, toStore)) {
            setDebugState("正在存储 " + target);
            // 从当前箱子中获取最符合我们需求的物品
            List<Slot> potentials = mod.getItemStorage().getSlotsWithItemPlayerInventory(false, target.getMatches());

            // 选择最佳的槽位进行抓取
            Optional<Slot> bestPotential = PickupFromContainerTask.getBestSlotToTransfer(
                    mod,
                    target,
                    mod.getItemStorage().getItemCountContainer(target.getMatches()),
                    potentials,
                    stack -> mod.getItemStorage().getSlotThatCanFitInOpenContainer(stack, false).isPresent());
            if (bestPotential.isPresent()) {
                ItemStack stackIn = StorageHelper.getItemStackInSlot(bestPotential.get());
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInOpenContainer(stackIn, false);
                if (toMoveTo.isEmpty()) {
                    setDebugState("容器已满！");
                    return null;
                }
                setDebugState("正在移动到槽位...");
                return new MoveItemToSlotFromInventoryTask(target, toMoveTo.get());
            }
            setDebugState("不应该发生！未检测到有效物品。");
        }
        setDebugState("不应该发生！所有物品已存储但我们仍在尝试。");
        return null;
    }

    /**
     * 检查任务是否完成
     * @return 如果所有物品都已存储则返回true
     */
    @Override
    public boolean isFinished() {
        // 我们已经存储了所有物品
        return storedItems != null && storedItems.getUnstoredItemTargetsYouCanStore(AltoClef.getInstance(), toStore).length == 0;
    }

    /**
     * 检查任务是否相等
     * @param other 要比较的其他任务
     * @return 如果任务相等则返回true
     */
    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof StoreInContainerTask task) {
            return task.targetContainer.equals(targetContainer) && task.getIfNotPresent == getIfNotPresent && Arrays.equals(task.toStore, toStore);
        }
        return false;
    }

    /**
     * 获取调试字符串
     * @return 调试信息字符串
     */
    @Override
    protected String toDebugString() {
        return "在容器[" + targetContainer.toShortString() + "]中存储 " + Arrays.toString(toStore);
    }
}
