package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.CursorSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Objects;
import java.util.Optional;

/**
 * 将不可访问的物品移动到背包的任务
 * 当物品在容器中但无法从容器中访问时，将它们移回玩家背包
 */
public class MoveInaccessibleItemToInventoryTask extends Task {

    // 目标物品
    private final ItemTarget target;

    public MoveInaccessibleItemToInventoryTask(ItemTarget target) {
        this.target = target;
    }

    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 确保背包界面已关闭
        if (!StorageHelper.isPlayerInventoryOpen()) {
            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
            if (!cursorStack.isEmpty()) {
                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                if (moveTo.isPresent()) {
                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                    return null;
                }
                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                    return null;
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                // 如果光标槽是垃圾，则尝试丢弃
                if (garbage.isPresent()) {
                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                    return null;
                }
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            } else {
                StorageHelper.closeScreen();
            }
            setDebugState("首先关闭界面（希望这不会被重复执行）");
            return null;
        }

        Optional<Slot> slotToMove = StorageHelper.getFilledInventorySlotInaccessibleToContainer(mod, target);
        if (slotToMove.isPresent()) {
            // 如果光标槽中有目标物品，则强制使用光标槽
            if (target.matches(StorageHelper.getItemStackInCursorSlot().getItem())) {
                slotToMove = Optional.of(CursorSlot.SLOT);
            }
            // 问题是在清空坏物品时光标槽已满
            // 解决方案：首先确保光标为空
            if (!StorageHelper.getItemStackInCursorSlot().isEmpty()) {
                return new EnsureFreeCursorSlotTask();
            }

            Slot toMove = slotToMove.get();
            ItemStack stack = StorageHelper.getItemStackInSlot(toMove);
            Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(stack, false);
            if (toMoveTo.isPresent()) {
                setDebugState("将槽位 " + toMove + " 移动到背包");
                // 拾取并移动
                if (Slot.isCursor(toMove)) {
                    mod.getSlotHandler().clickSlot(toMoveTo.get(), 0, SlotActionType.PICKUP);
                } else {
                    mod.getSlotHandler().clickSlot(toMove, 0, SlotActionType.PICKUP);
                }
                return null;
            } else {
                setDebugState("首先释放背包空间。");
                // 先释放空间
                return new EnsureFreeInventorySlotTask();
            }
        }
        setDebugState("未找到");
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof MoveInaccessibleItemToInventoryTask task) {
            // 比较目标物品是否相同
            return Objects.equals(task.target, target);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "使物品可访问: " + target;
    }
}
