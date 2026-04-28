package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.FurnaceSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * 从容器拾取物品任务 - 从指定容器中拾取特定物品到玩家背包
 */
public class PickupFromContainerTask extends AbstractDoToStorageContainerTask {

    private final BlockPos _targetContainer; // 目标容器位置
    private final ItemTarget[] _targets; // 目标物品数组

    private final EnsureFreeInventorySlotTask _freeInventoryTask = new EnsureFreeInventorySlotTask(); // 确保背包有空位的任务

    public PickupFromContainerTask(BlockPos targetContainer, ItemTarget... targets) {
        _targets = targets;
        _targetContainer = targetContainer;
    }

    /**
     * 获取最佳转移槽位 - 选择最符合需求的槽位来转移物品
     * @param mod AltoClef实例
     * @param itemToMove 要移动的物品目标
     * @param currentItemQuantity 当前已有的物品数量
     * @param grabPotentials 可能的槽位列表
     * @param canStackFit 检查物品堆叠是否能放入背包的函数
     * @return 最佳槽位的Optional
     */
    public static Optional<Slot> getBestSlotToTransfer(AltoClef mod, ItemTarget itemToMove, int currentItemQuantity, List<Slot> grabPotentials, Function<ItemStack, Boolean> canStackFit) {
        Slot bestPotential = null;
        int leftNeeded = itemToMove.getTargetCount() - currentItemQuantity;
        for (Slot slot : grabPotentials) {
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            if (itemToMove.matches(stack.getItem())) {
                if (bestPotential == null) {
                    bestPotential = slot;
                    continue;
                }
                ItemStack currBest = StorageHelper.getItemStackInSlot(bestPotential);
                int overshoot = stack.getCount() - leftNeeded;
                int currBestOverhoot = currBest.getCount() - leftNeeded;
                boolean canFit = canStackFit.apply(stack);
                boolean currBestCanFit = canStackFit.apply(currBest);
                // 优先选择能放入背包的物品堆叠
                if (canFit || !currBestCanFit) {
                    if (overshoot < 0) {
                        // 优先选择数量最接近但不超过需求的，然后是最少超出的
                        if (currBestOverhoot > 0 || overshoot > currBestOverhoot)
                            bestPotential = slot;
                    } else if (overshoot > 0) {
                        // 优先选择超出最少的
                        if (overshoot < currBestOverhoot)
                            bestPotential = slot;
                    } else if (currBestOverhoot != 0) {
                        // 我们有完美的匹配
                        bestPotential = slot;
                    }
                }
            }
        }
        return Optional.ofNullable(bestPotential);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof PickupFromContainerTask task) {
            return Objects.equals(_targetContainer, task._targetContainer) && Arrays.equals(_targets, task._targets);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "从容器 (" + _targetContainer.toShortString() + ") 拾取: " + Arrays.toString(_targets);
    }

    @Override
    protected Optional<BlockPos> getContainerTarget() {
        return Optional.of(_targetContainer);
    }

    @Override
    protected Task onTick() {
        // 在执行过程中释放背包空间
        if (_freeInventoryTask.isActive() && !_freeInventoryTask.isFinished() && !AltoClef.getInstance().getItemStorage().hasEmptyInventorySlot()) {
            setDebugState("释放背包空间");
            return _freeInventoryTask;
        }
        return super.onTick();
    }

    @Override
    public boolean isFinished() {
        // 当所有目标物品都已收集完成时，任务完成
        return Arrays.stream(_targets).allMatch(target -> AltoClef.getInstance().getItemStorage().getItemCountInventoryOnly(target.getMatches()) >= target.getTargetCount());
    }

    @Override
    protected Task onContainerOpenSubtask(AltoClef mod, ContainerCache containerCache) {
        for (ItemTarget target : _targets) {
            // Go through each item
            int count = mod.getItemStorage().getItemCountInventoryOnly(target.getMatches());
            if (target.matches(StorageHelper.getItemStackInCursorSlot().getItem()))
                count -= StorageHelper.getItemStackInCursorSlot().getCount();
            if (count < target.getTargetCount()) {
                setDebugState("收集 " + target);
                // 从当前容器中获取最符合我们需求的物品
                List<Slot> potentials = mod.getItemStorage().getSlotsWithItemContainer(target.getMatches());

                // 选择最佳槽位进行拾取
                Optional<Slot> bestPotential = getBestSlotToTransfer(mod, target, count, potentials, stack -> mod.getItemStorage().getSlotThatCanFitInPlayerInventory(stack, false).isPresent());
                ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
                if (!cursorStack.isEmpty()) {
                    Optional<Slot> toPlace = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false).or(() -> StorageHelper.getGarbageSlot(mod));
                    if (toPlace.isPresent() && target.matches(cursorStack.getItem())) {
                        mod.getSlotHandler().clickSlot(toPlace.get(), 0, SlotActionType.PICKUP);
                        return null;
                    }
                    if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                        mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                        return null;
                    }
                    if (toPlace.isPresent()) {
                        mod.getSlotHandler().clickSlot(toPlace.get(), 0, SlotActionType.PICKUP);
                        return null;
                    }
                }
                if (bestPotential.isPresent()) {
                    // Just pick it up, it's now ours.
                    mod.getSlotHandler().clickSlot(bestPotential.get(), 0, SlotActionType.PICKUP);
                    return null;
                }
                setDebugState("不应发生！未检测到有效物品。");
            }
        }

        // 我们完成了
        setDebugState("完成");
        if (mod.getPlayer().currentScreenHandler instanceof SmokerScreenHandler || mod.getPlayer().currentScreenHandler
                instanceof FurnaceScreenHandler) {
            mod.getSlotHandler().clickSlot(FurnaceSlot.INPUT_SLOT_MATERIALS, 0, SlotActionType.PICKUP);
            return null;
        }
        return null;
    }
}
