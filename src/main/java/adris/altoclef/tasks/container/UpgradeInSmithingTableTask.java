package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasks.slot.MoveItemToSlotFromInventoryTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.slots.SmithingTableSlot;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

/**
 * 在锻造台中升级物品的任务（例如将钻石工具升级为下界合金工具）
 */
public class UpgradeInSmithingTableTask extends ResourceTask {

    private final ItemTarget _tool; // 要升级的工具（如钻石镐）
    private final ItemTarget _template; // 升级模板（如下界合金升级模板）
    private final ItemTarget _material; // 升级材料（如下界合金锭）
    private final ItemTarget _output; // 升级后的输出物品（如下界合金镐）

    private final Task _innerTask; // 内部执行任务

    /**
     * 构造函数
     * @param tool 要升级的工具
     * @param material 升级材料
     * @param output 升级后的输出物品
     */
    public UpgradeInSmithingTableTask(ItemTarget tool, ItemTarget material, ItemTarget output) {
        super(output);
        _tool = new ItemTarget(tool, output.getTargetCount());
        _material = new ItemTarget(material, output.getTargetCount());
        _template = new ItemTarget("netherite_upgrade_smithing_template", output.getTargetCount());
        _output = output;
        _innerTask = new UpgradeInSmithingTableInternalTask();
    }

    /**
     * 是否应避免拾取物品
     * @param mod AltoClef实例
     * @return 是否避免拾取
     */
    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    /**
     * 资源任务开始时的回调
     * @param mod AltoClef实例
     */
    @Override
    protected void onResourceStart(AltoClef mod) {
    }

    /**
     * 获取槽位中匹配物品的数量
     * @param slot 槽位
     * @param match 物品匹配目标
     * @return 匹配物品的数量
     */
    private int getItemsInSlot(Slot slot, ItemTarget match) {
        ItemStack stack = StorageHelper.getItemStackInSlot(slot);
        if (!stack.isEmpty() && match.matches(stack.getItem())) {
            return stack.getCount();
        }
        return 0;
    }

    /**
     * 资源任务每帧执行的逻辑
     * @param mod AltoClef实例
     * @return 下一个要执行的子任务
     */
    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 如果我们没有工具和材料，就去获取它们

        boolean inSmithingTable = (mod.getPlayer().currentScreenHandler instanceof SmithingScreenHandler);

        int templatesInSlot = inSmithingTable ? getItemsInSlot(SmithingTableSlot.INPUT_SLOT_TEMPLATE, _template) : 0;
        int materialsInSlot = inSmithingTable ? getItemsInSlot(SmithingTableSlot.INPUT_SLOT_MATERIALS, _material) : 0;
        int toolsInSlot = inSmithingTable ? getItemsInSlot(SmithingTableSlot.INPUT_SLOT_TOOL, _tool) : 0;
        int ouputInSlot = inSmithingTable ? getItemsInSlot(SmithingTableSlot.OUTPUT_SLOT, _output) : 0;

        int desiredOutput = _output.getTargetCount() - ouputInSlot;

        if (mod.getItemStorage().getItemCount(_tool) + toolsInSlot < desiredOutput ||
                mod.getItemStorage().getItemCount(_material) + materialsInSlot < desiredOutput ||
                mod.getItemStorage().getItemCount(_template) + templatesInSlot < desiredOutput) {
            setDebugState("正在获取材料和工具");
            return TaskCatalogue.getSquashedItemTask(_tool, _material, _template);
        }

        // 边界情况：我们正穿着要升级的盔甲。如果是这样，先脱掉它。
        if (StorageHelper.isArmorEquipped(_tool.getMatches())) {
            // 退出任何界面以便我们可以移动盔甲
            if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
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
                    // 如果光标槽位是垃圾物品，尝试丢弃它
                    if (garbage.isPresent()) {
                        mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                        return null;
                    }
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                } else {
                    StorageHelper.closeScreen();
                }
                setDebugState("快速移除已装备的盔甲");
                return null;
            }
            // 脱下我们的盔甲
            if (mod.getItemStorage().hasEmptyInventorySlot()) {
                return new EnsureFreeInventorySlotTask();
            }
            for (Slot armorSlot : PlayerSlot.ARMOR_SLOTS) {
                if (_tool.matches(StorageHelper.getItemStackInSlot(armorSlot).getItem())) {
                    setDebugState("快速移除已装备的盔甲");
                    mod.getSlotHandler().clickSlot(armorSlot, 0, SlotActionType.QUICK_MOVE);
                    return null;
                }
            }
        }

        setDebugState("正在锻造...");
        return _innerTask;
    }

    /**
     * 资源任务停止时的回调
     * @param mod AltoClef实例
     * @param interruptTask 中断任务
     */
    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
    }

    /**
     * 检查资源任务是否相等
     * @param other 要比较的其他资源任务
     * @return 如果资源任务相等则返回true
     */
    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof UpgradeInSmithingTableTask task) {
            return task._tool.equals(_tool) && task._output.equals(_output) && task._material.equals(_material);
        }
        return false;
    }

    /**
     * 获取调试字符串名称
     * @return 调试信息字符串
     */
    @Override
    protected String toDebugStringName() {
        return "升级 " + _tool.toString() + " + " + _material.toString() + " -> " + _output.toString();
    }

    /**
     * 获取要升级的工具
     * @return 工具物品目标
     */
    public ItemTarget getTools() {
        return _tool;
    }

    /**
     * 获取升级材料
     * @return 材料物品目标
     */
    public ItemTarget getMaterials() {
        return _material;
    }

    /**
     * 锻造台内部执行任务类
     */
    private class UpgradeInSmithingTableInternalTask extends DoStuffInContainerTask {

        private final TimerGame _invTimer; // 物品移动计时器

        public UpgradeInSmithingTableInternalTask() {
            super(Blocks.SMITHING_TABLE, new ItemTarget("smithing_table"));
            _invTimer = new TimerGame(0);
        }

        /**
         * 检查子任务是否相等
         * @param other 要比较的其他容器任务
         * @return 如果子任务相等则返回true（内部部分，不关心）
         */
        @Override
        protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
            // 内部部分，不关心
            return true;
        }

        /**
         * 检查容器是否已打开
         * @param mod AltoClef实例
         * @return 如果锻造台界面已打开则返回true
         */
        @Override
        protected boolean isContainerOpen(AltoClef mod) {
            return (mod.getPlayer().currentScreenHandler instanceof SmithingScreenHandler);
        }

        /**
         * 容器子任务逻辑
         * @param mod AltoClef实例
         * @return 下一个要执行的子任务
         */
        @Override
        protected Task containerSubTask(AltoClef mod) {
            setDebugState("正在锻造...");
            // 我们已经有了工具和材料。现在，执行升级操作。
            _invTimer.setInterval(mod.getModSettings().getContainerItemMoveDelay());

            // 每隔一段时间执行一次
            if (!_invTimer.elapsed()) {
                return null;
            }
            _invTimer.reset();

            Slot templateSlot = SmithingTableSlot.INPUT_SLOT_TEMPLATE;
            Slot materialSlot = SmithingTableSlot.INPUT_SLOT_MATERIALS;
            Slot toolSlot = SmithingTableSlot.INPUT_SLOT_TOOL;
            Slot outputSlot = SmithingTableSlot.OUTPUT_SLOT;

            ItemStack currentTemplates = StorageHelper.getItemStackInSlot(templateSlot);
            ItemStack currentMaterials = StorageHelper.getItemStackInSlot(materialSlot);
            ItemStack currentTools = StorageHelper.getItemStackInSlot(toolSlot);
            ItemStack currentOutput = StorageHelper.getItemStackInSlot(outputSlot);
            // 从输出槽位抓取物品
            if (!currentOutput.isEmpty()) {
                mod.getSlotHandler().clickSlot(outputSlot, 0, SlotActionType.QUICK_MOVE);
                return null;
            }
            // 将材料放入槽位
            if (currentMaterials.isEmpty() || !_material.matches(currentMaterials.getItem())) {
                return new MoveItemToSlotFromInventoryTask(new ItemTarget(_material, 1), materialSlot);
            }
            // 将工具放入槽位
            if (currentTools.isEmpty() || !_tool.matches(currentTools.getItem())) {
                return new MoveItemToSlotFromInventoryTask(new ItemTarget(_tool, 1), toolSlot);
            }

            if (currentTemplates.isEmpty() || !_template.matches(currentTemplates.getItem())) {
                return new MoveItemToSlotFromInventoryTask(new ItemTarget(_template, 1), templateSlot);
            }

            setDebugState("问题：无事可做！");
            return null;
        }

        /**
         * 获取创建新任务的成本
         * @param mod AltoClef实例
         * @return 创建成本
         */
        @Override
        protected double getCostToMakeNew(AltoClef mod) {
            int price = 400;
            if (mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 4) {
                price -= 125;
            }
            if (mod.getItemStorage().getItemCount(Items.FLINT) >= 2) {
                price -= 125;
            }
            return price;
        }
    }

}
