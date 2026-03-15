package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 移动物品到指定槽位的任务基类
 */
public class MoveItemToSlotTask extends Task {

    // 要移动的物品目标
    private final ItemTarget toMove;
    // 目标槽位
    private final Slot destination;
    // 获取可移动物品槽位的函数
    private final Function<AltoClef, List<Slot>> getMovableSlots;

    public MoveItemToSlotTask(ItemTarget toMove, Slot destination, Function<AltoClef, List<Slot>> getMovableSlots) {
        this.toMove = toMove;
        this.destination = destination;
        this.getMovableSlots = getMovableSlots;
    }

    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        if (mod.getSlotHandler().canDoSlotAction()) {
            // 大致计划
            // - 如果槽位为空或物品不匹配
            //      找到最佳匹配物品（略多于目标数量的最小堆叠，或没有多余时的最大堆叠）
            //      点击它（一个回合）
            // - 如果手持物品数量少于目标数量
            //      左键点击目标槽位（一个回合）
            // - 如果手持物品数量多于目标数量
            //      右键点击目标槽位（一个回合）
            ItemStack currentHeld = StorageHelper.getItemStackInCursorSlot();
            ItemStack atTarget = StorageHelper.getItemStackInSlot(destination);

            // 可以移动到该槽位的物品
            Item[] validItems = toMove.getMatches();//Arrays.stream(_toMove.getMatches()).filter(item -> mod.getItemStorage().getItemCount(item) >= _toMove.getTargetCount()).toArray(Item[]::new);

            // 我们需要处理光标中的物品或放置一个物品（来移动）。
            boolean wrongItemHeld = !Arrays.asList(validItems).contains(currentHeld.getItem());
            if (currentHeld.isEmpty() || wrongItemHeld) {
                Optional<Slot> toPlace;
                if (currentHeld.isEmpty()) {
                    // 直接拾取
                    toPlace = getBestSlotToPickUp(mod, validItems);
                } else {
                    // 首先尝试放置当前持有的物品。
                    toPlace = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(currentHeld, true);
                    if (toPlace.isEmpty()) {
                        // 如果其他方法都失败了，直接交换。
                        toPlace = getBestSlotToPickUp(mod, validItems);
                    }
                }
                if (toPlace.isEmpty()) {
                    Debug.logWarning("调用MoveItemToSlotTask时物品/物品数量不足！有效物品: " + StlHelper.toString(validItems, Item::getTranslationKey));
                    this.stop();
                    return null;
                }
                mod.getSlotHandler().clickSlot(toPlace.get(), 0, SlotActionType.PICKUP);
                return null;
            }

            int currentlyPlaced = Arrays.asList(validItems).contains(atTarget.getItem()) ? atTarget.getCount() : 0;
            if (currentHeld.getCount() + currentlyPlaced <= toMove.getTargetCount()) {
                // 直接放置全部
                mod.getSlotHandler().clickSlot(destination, 0, SlotActionType.PICKUP);
            } else {
                // 逐个放置。
                mod.getSlotHandler().clickSlot(destination, 1, SlotActionType.PICKUP);
            }
            return null;
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    public boolean isFinished() {
        ItemStack atDestination = StorageHelper.getItemStackInSlot(destination);
        // 检查目标槽位是否包含目标物品且数量达到目标值
        return (toMove.matches(atDestination.getItem()) && atDestination.getCount() >= toMove.getTargetCount());
    }

    @Override
    protected boolean isEqual(Task obj) {
        if (obj instanceof MoveItemToSlotTask task) {
            // 比较物品目标和目标槽位是否相同
            return task.toMove.equals(toMove) && task.destination.equals(destination);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "将 " + toMove + " 移动到 " + destination;
    }

    /**
     * 获取最佳的拾取槽位
     * @param mod AltoClef主模块实例
     * @param validItems 有效的物品数组
     * @return 最佳拾取槽位的可选值
     */
    private Optional<Slot> getBestSlotToPickUp(AltoClef mod, Item[] validItems) {
        Slot bestMatch = null;
        if (!getMovableSlots.apply(mod).isEmpty()) {
            for (Slot slot : getMovableSlots.apply(mod)) {
                if (Slot.isCursor(slot))
                    continue;
                if (!toMove.matches(StorageHelper.getItemStackInSlot(slot).getItem()))
                    continue;
                if (bestMatch == null) {
                    bestMatch = slot;
                    continue;
                }
                int countBest = StorageHelper.getItemStackInSlot(bestMatch).getCount();
                int countCheck = StorageHelper.getItemStackInSlot(slot).getCount();
                if ((countBest < toMove.getTargetCount() && countCheck > countBest)
                        || (countBest >= toMove.getTargetCount() && countCheck >= toMove.getTargetCount() && countCheck > countBest)) {
                    // 如果数量不足，选择最大的
                    // 如果数量过多，选择超过限制中最小的。
                    bestMatch = slot;
                }
            }
        }
        return Optional.ofNullable(bestMatch);
    }
}
