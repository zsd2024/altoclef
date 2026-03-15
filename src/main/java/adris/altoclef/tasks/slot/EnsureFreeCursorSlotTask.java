package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

/**
 * 确保光标槽位为空的任务
 */
public class EnsureFreeCursorSlotTask extends Task {

    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();

        if (!cursor.isEmpty()) {
            // 尝试将光标中的物品移至背包中合适的槽位
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false);
            if (moveTo.isPresent()) {
                setDebugState("将光标中的物品移回");
                mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                return null;
            }
            // 如果光标中的物品可以丢弃，则直接丢弃
            if (ItemHelper.canThrowAwayStack(mod, cursor)) {
                setDebugState("光标中的物品不兼容，正在丢弃");
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            } else {
                // 否则尝试找到垃圾槽位
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                if (garbage.isPresent()) {
                    // 拾取垃圾以便在下一帧丢弃
                    setDebugState("拾取垃圾");
                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                } else {
                    // 如果找不到垃圾槽，则直接丢弃
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                }
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
    protected boolean isEqual(Task other) {
        // 所有EnsureFreeCursorSlotTask实例都被认为是相等的
        return other instanceof EnsureFreeCursorSlotTask;
    }


    // 填写此方法将使其在任务树中显示良好
    @Override
    protected String toDebugString() {
        return "清空光标槽位";
    }
}
