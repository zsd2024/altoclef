package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.slot.MoveItemToSlotFromInventoryTask;
import adris.altoclef.tasks.slot.ReceiveCraftingOutputSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.CraftingTableSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

/**
 * 假设一个合成界面已打开，手动合成指定的配方。
 * <p>
 * 对自定义任务没有用。
 */
public class CraftGenericManuallyTask extends Task {

    // 合成目标
    private final RecipeTarget target;

    public CraftGenericManuallyTask(RecipeTarget target) {
        this.target = target;
    }

    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        boolean bigCrafting = StorageHelper.isBigCraftingOpen();

        if (!bigCrafting && !StorageHelper.isPlayerInventoryOpen()) {
            // 在合成之前确保不在其他界面中，
            // 否则合成将不起作用
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
            // 为了安全起见
        }

        // 确定输出槽位，根据是否在合成台合成决定使用哪个槽位
        Slot outputSlot = bigCrafting ? CraftingTableSlot.OUTPUT_SLOT : PlayerSlot.CRAFT_OUTPUT_SLOT;

        // 示例：
        // 我们需要9根木棍
        // 木板配方生成4根木棍
        // 这意味着每个合成槽需要3个木板
        int requiredPerSlot = (int) Math.ceil((double) target.getTargetCount() / target.getRecipe().outputCount());

        // 遍历合成表中的每个槽位
        for (int craftSlot = 0; craftSlot < target.getRecipe().getSlotCount(); ++craftSlot) {
            ItemTarget toFill = target.getRecipe().getSlot(craftSlot);
            Slot currentCraftSlot;
            if (bigCrafting) {
                // 在合成台中合成
                currentCraftSlot = CraftingTableSlot.getInputSlot(craftSlot, target.getRecipe().isBig());
            } else {
                // 在窗口中合成
                currentCraftSlot = PlayerSlot.getCraftInputSlot(craftSlot);
            }
            ItemStack present = StorageHelper.getItemStackInSlot(currentCraftSlot);
            if (toFill == null || toFill.isEmpty()) {
                if (present.getItem() != Items.AIR) {
                    // 如果槽位应该是空的，将此物品移出
                    setDebugState("找到无效槽位");
                    mod.getSlotHandler().clickSlot(currentCraftSlot, 0, SlotActionType.PICKUP);
                }
            } else {
                // 检查物品是否正确且数量充足
                boolean correctItem = toFill.matches(present.getItem());
                boolean isSatisfied = correctItem && present.getCount() >= requiredPerSlot;
                if (!isSatisfied) {
                    // 我们有满足条件的物品，但无法填入当前槽位！
                    // 在这种情况下，直接从输出槽获取。
                    if (!mod.getItemStorage().hasItemInventoryOnly(present.getItem())) {
                        if (!StorageHelper.getItemStackInSlot(outputSlot).isEmpty()) {
                            setDebugState("没有更多空间可放：从输出槽获取。");
                            return new ReceiveCraftingOutputSlotTask(outputSlot, target.getTargetCount());
                        } else {
                            // 移动到下一个槽位，我们无法再填充此槽位。
                            continue;
                        }
                    }

                    setDebugState("将物品移动到槽位...");
                    return new MoveItemToSlotFromInventoryTask(new ItemTarget(toFill, requiredPerSlot), currentCraftSlot);
                }
                // 我们可能过度满足了
                boolean oversatisfies = present.getCount() > requiredPerSlot;
                if (oversatisfies) {
                    setDebugState("过度满足槽位！右键点击槽位以提取一半并分散。");
                    mod.getSlotHandler().clickSlot(currentCraftSlot, 0, SlotActionType.PICKUP);
                }
            }
        }

        // 确保光标是空的或可以接收我们的物品
        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
        if (!ItemHelper.canStackTogether(StorageHelper.getItemStackInSlot(outputSlot), cursor)) {
            Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
            if (toFit.isPresent()) {
                mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
            } else {
                // 无所谓了
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
        }

        if (!StorageHelper.getItemStackInSlot(outputSlot).isEmpty()) {
            return new ReceiveCraftingOutputSlotTask(outputSlot, target.getTargetCount());
        } else {
            // 等待
            return null;
        }
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof CraftGenericManuallyTask task) {
            // 比较合成目标是否相同
            return task.target.equals(target);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "合成: " + target;
    }
}
