package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.ITaskUsesCraftingGrid;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.CraftingTableSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * 接收合成输出槽位物品的任务
 */
public class ReceiveCraftingOutputSlotTask extends Task implements ITaskUsesCraftingGrid {

    // 要拿取的数量
    private final int _toTake;
    // 目标槽位
    private final Slot _slot;

    public ReceiveCraftingOutputSlotTask(Slot slot, int toTake) {
        _slot = slot;
        _toTake = toTake;
    }

    /**
     * 构造函数
     * @param slot 目标槽位
     * @param all 是否拿取全部
     */
    public ReceiveCraftingOutputSlotTask(Slot slot, boolean all) {
        this(slot, all ? Integer.MAX_VALUE : 1);
    }

    /**
     * 获取当前合成配方可以合成的次数？
     * @param mod AltoClef主模块实例
     * @return 可以合成的次数
     */
    private static int getCraftMultipleCount(AltoClef mod) {
        int minNonZero = Integer.MAX_VALUE;
        boolean found = false;
        // 根据当前是否打开了大型合成台来选择检查的输入槽位
        for (Slot check : (StorageHelper.isBigCraftingOpen() ? CraftingTableSlot.INPUT_SLOTS : PlayerSlot.CRAFT_INPUT_SLOTS)) {
            ItemStack stack = StorageHelper.getItemStackInSlot(check);
            if (!stack.isEmpty()) {
                minNonZero = Math.min(stack.getCount(), minNonZero);
                found = true;
            }
        }
        if (!found)
            return 0;
        return minNonZero;
    }

    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        ItemStack inOutput = StorageHelper.getItemStackInSlot(_slot);
        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();

        boolean cursorSlotFree = cursor.isEmpty();

        // 如果光标槽不为空且无法与输出物品堆叠，则清空光标槽
        if (!cursorSlotFree && !ItemHelper.canStackTogether(inOutput, cursor)) {
            return new EnsureFreeCursorSlotTask();
        }

        AltoClef mod = AltoClef.getInstance();

        // 计算可以合成的数量
        int craftCount = inOutput.getCount() * getCraftMultipleCount(mod);
        // 计算还需要添加到背包的数量
        int weWantToAddToInventory = _toTake - mod.getItemStorage().getItemCountInventoryOnly(inOutput.getItem());
        boolean takeAll = weWantToAddToInventory >= craftCount;
        // 如果要拿取全部且背包中存在可放入的槽位，则快速移动
        if (takeAll && mod.getItemStorage().getSlotThatCanFitInPlayerInventory(inOutput, true).isPresent()) {
            setDebugState("快速移动输出");
            mod.getSlotHandler().clickSlot(_slot, 0, SlotActionType.QUICK_MOVE);
            return null;
        }
        setDebugState("拾取输出");
        mod.getSlotHandler().clickSlot(_slot, 0, SlotActionType.PICKUP);
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ReceiveCraftingOutputSlotTask task) {
            // 比较槽位和拿取数量是否相同
            return task._slot.equals(_slot) && task._toTake == _toTake;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "接收输出";
    }
}
