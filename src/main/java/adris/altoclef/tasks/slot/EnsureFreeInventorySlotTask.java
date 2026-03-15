package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

/**
 * 确保背包中有空槽的任务
 */
public class EnsureFreeInventorySlotTask extends Task {
    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 获取光标槽中的物品
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        // 获取垃圾槽
        Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
        if (cursorStack.isEmpty()) {
            // 如果光标槽为空且存在垃圾槽，则点击垃圾槽
            if (garbage.isPresent()) {
                mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                return null;
            }
        }
        if (!cursorStack.isEmpty()) {
            // 如果光标槽不为空，则随机改变朝向并丢弃物品
            LookHelper.randomOrientation();
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            return null;
        }
        setDebugState("所有物品都被保护。");
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    protected boolean isEqual(Task obj) {
        // 所有EnsureFreeInventorySlotTask实例都被认为是相等的
        return obj instanceof EnsureFreeInventorySlotTask;
    }

    @Override
    protected String toDebugString() {
        return "确保背包有空槽";
    }
}
