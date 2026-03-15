package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;
import adris.altoclef.tasks.slot.EnsureFreePlayerCraftingGridTask;
import adris.altoclef.tasks.slot.ReceiveCraftingOutputSlotTask;
import adris.altoclef.tasksystem.ITaskUsesCraftingGrid;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.JankCraftingRecipeMapping;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.CraftingTableSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

/**
 * 使用配方书合成指定物品的任务
 */
public class CraftGenericWithRecipeBooksTask extends Task implements ITaskUsesCraftingGrid {

    // 合成目标
    private final RecipeTarget target;

    public CraftGenericWithRecipeBooksTask(RecipeTarget target) {
        this.target = target;
    }

    /**
     * 模组启动时调用此方法
     */
    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    /**
     * 处理onTick事件的逻辑。
     * 检查各种条件并相应地执行操作。
     *
     * @return 要执行的下一个任务
     */
    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 检查大型合成UI或玩家背包UI是否打开
        boolean isBigCraftingOpen = StorageHelper.isBigCraftingOpen();
        boolean isPlayerInventoryOpen = StorageHelper.isPlayerInventoryOpen();

        // 获取光标槽中的物品堆叠
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();

        // 声明移动到的槽位和垃圾槽的变量
        Optional<Slot> moveTo;
        Optional<Slot> garbage;

        // 检查大型合成UI和玩家背包UI是否都没有打开
        if (!isBigCraftingOpen && !isPlayerInventoryOpen) {
            // 检查光标堆叠是否不为空
            if (!cursorStack.isEmpty()) {
                // 在玩家背包中找到一个可以移动物品的槽位
                moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                if (moveTo.isPresent()) {
                    // 点击槽位将物品移动到玩家背包中
                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                    return null;
                }
                // 检查物品是否可以丢弃
                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                    // 点击未定义槽位丢弃物品
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                    return null;
                }
                // 找到垃圾槽并点击它将物品移动到那里
                garbage = StorageHelper.getGarbageSlot(mod);
                if (garbage.isPresent()) {
                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                }
                // 点击未定义槽位清除光标堆叠
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            } else {
                // 关闭界面
                StorageHelper.closeScreen();
            }
        }

        // 根据大型合成UI是否打开确定输出槽位
        Slot outputSlot = isBigCraftingOpen ? CraftingTableSlot.OUTPUT_SLOT : PlayerSlot.CRAFT_OUTPUT_SLOT;
        // 获取输出槽中的物品堆叠
        ItemStack output = StorageHelper.getItemStackInSlot(outputSlot);

        // 检查输出物品是否匹配目标物品且未达到目标数量
        if (target.getOutputItem() == output.getItem() && mod.getItemStorage().getItemCount(target.getOutputItem()) < target.getTargetCount()) {
            // 返回接收合成输出槽的任务
            return new ReceiveCraftingOutputSlotTask(outputSlot, target.getTargetCount());
        }

        // 检查光标堆叠是否不为空
        if (!cursorStack.isEmpty()) {
            // 在玩家背包中找到一个可以移动物品的槽位
            moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            if (moveTo.isPresent()) {
                // 点击槽位将物品移动到玩家背包中
                mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                return null;
            }
            // 检查物品是否可以丢弃
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                // 点击未定义槽位丢弃物品
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                return null;
            }
            // 找到垃圾槽并点击它将物品移动到那里
            garbage = StorageHelper.getGarbageSlot(mod);
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            // 点击未定义槽位清除光标堆叠
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            return null;
        }

        // 检查大型合成UI和玩家背包UI是否都没有打开
        if (!isBigCraftingOpen) {
            PlayerSlot[] playerInputSlots = PlayerSlot.CRAFT_INPUT_SLOTS;
            for (PlayerSlot playerInputSlot : playerInputSlots) {
                ItemStack playerInput = StorageHelper.getItemStackInSlot(playerInputSlot);
                if (!playerInput.isEmpty()) {
                    // 返回确保玩家合成网格空闲的任务
                    return new EnsureFreePlayerCraftingGridTask();
                }
            }
        }

        // 获取要发送的配方
        Optional<WrappedRecipeEntry> recipeToSend = JankCraftingRecipeMapping.getMinecraftMappedRecipe(target.getRecipe(), target.getOutputItem());
        if (recipeToSend.isPresent()) {
            if (mod.getSlotHandler().canDoSlotAction()) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                assert player != null;
                // 点击发送配方
                mod.getController().clickRecipe(player.currentScreenHandler.syncId, recipeToSend.get().asRecipe(), true);
                mod.getSlotHandler().registerSlotAction();
            }
        }

        return null;
    }

    /**
     * 任务被中断时调用此方法。
     *
     * @param interruptTask 中断当前任务的任务。
     */
    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    /**
     * 检查给定的Task对象是否等于此CraftGenericWithRecipeBooksTask对象。
     *
     * @param other 要比较的Task对象。
     * @return 如果给定的Task等于此CraftGenericWithRecipeBooksTask，则返回true，否则返回false。
     */
    @Override
    protected boolean isEqual(Task other) {
        // 检查另一个Task是否是CraftGenericWithRecipeBooksTask的实例
        if (other instanceof CraftGenericWithRecipeBooksTask) {
            CraftGenericWithRecipeBooksTask task = (CraftGenericWithRecipeBooksTask) other;

            // 检查另一个任务的目标是否等于此任务的目标
            boolean isEqual = task.target.equals(target);

            // 如果目标不相等则记录消息
            if (!isEqual) {
                Debug.logInternal("任务目标不相等");
            }

            // 返回相等性检查的结果
            return isEqual;
        }

        // 如果另一个Task不是CraftGenericWithRecipeBooksTask的实例则记录消息
        Debug.logInternal("任务不是CraftGenericWithRecipeBooksTask的实例");

        // 如果另一个Task不是CraftGenericWithRecipeBooksTask的实例则返回false
        return false;
    }

    /**
     * 返回对象的调试字符串表示。
     *
     * @return 调试字符串表示。
     */
    @Override
    protected String toDebugString() {
        // 返回调试字符串。
        return getClass().getSimpleName() + " (使用配方): " + target;
    }
}
