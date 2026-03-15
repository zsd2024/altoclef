package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * 确保玩家2x2合成网格为空的任务
 */
public class EnsureFreePlayerCraftingGridTask extends Task {
    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        setDebugState("清空2x2合成网格");
        // 遍历合成网格中的所有槽位
        for (Slot slot : PlayerSlot.CRAFT_INPUT_SLOTS) {
            ItemStack items = StorageHelper.getItemStackInSlot(slot);
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
            // 如果光标槽不为空，则先清空光标槽
            if (!cursor.isEmpty()) {
                return new EnsureFreeCursorSlotTask();
            }
            // 如果当前槽位不为空，则点击该槽位来清空它
            if (!items.isEmpty()) {
                AltoClef.getInstance().getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    protected boolean isEqual(Task other) {
        // 所有EnsureFreePlayerCraftingGridTask实例都被认为是相等的
        return other instanceof EnsureFreePlayerCraftingGridTask;
    }

    @Override
    protected String toDebugString() {
        return "清空合成网格";
    }
}
