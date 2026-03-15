package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.resources.CollectRecipeCataloguedResourcesTask;
import adris.altoclef.tasks.slot.ReceiveCraftingOutputSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;
import java.util.Optional;

/**
 * 在2x2物品栏制作网格中制作物品的任务
 */
public class CraftInInventoryTask extends ResourceTask {

    // 配方目标
    private final RecipeTarget _target;
    // 是否收集材料
    private final boolean _collect;
    // 是否忽略未目录化的槽位
    private final boolean _ignoreUncataloguedSlots;
    // 完整检查是否失败
    private boolean _fullCheckFailed = false;

    /**
     * 构造函数，指定配方目标、是否收集材料和是否忽略未目录化槽位
     * @param target 配方目标
     * @param collect 是否收集材料
     * @param ignoreUncataloguedSlots 是否忽略未目录化的槽位
     */
    public CraftInInventoryTask(RecipeTarget target, boolean collect, boolean ignoreUncataloguedSlots) {
        super(new ItemTarget(target.getOutputItem(), target.getTargetCount()));
        _target = target;
        _collect = collect;
        _ignoreUncataloguedSlots = ignoreUncataloguedSlots;
    }

    /**
     * 构造函数，使用默认参数创建制作任务
     * @param target 配方目标
     */
    public CraftInInventoryTask(RecipeTarget target) {
        this(target, true, false);
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        // 在制作任务中不应避免拾取
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        _fullCheckFailed = false;
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty() && !StorageHelper.isBigCraftingOpen()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // 如果光标槽是垃圾，尝试丢弃
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        } // 为了安全起见
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 首先从输出槽获取
        if (StorageHelper.isPlayerInventoryOpen()) {
            if (StorageHelper.getItemStackInCursorSlot().isEmpty()) {
                Item outputItem = StorageHelper.getItemStackInSlot(PlayerSlot.CRAFT_OUTPUT_SLOT).getItem();
                if (itemTargets != null) {
                    for (ItemTarget target : itemTargets) {
                        if (target.matches(outputItem)) {
                            return new ReceiveCraftingOutputSlotTask(PlayerSlot.CRAFT_OUTPUT_SLOT, target.getTargetCount());
                        }
                    }
                }
            }
        }

        ItemTarget toGet = itemTargets[0];
        Item toGetItem = toGet.getMatches()[0];
        if (_collect && !StorageHelper.hasRecipeMaterialsOrTarget(mod, _target)) {
            // 收集配方材料
            setDebugState("收集材料");
            return collectRecipeSubTask(mod);
        }

        // 无需释放物品栏，输出会被拾取

        setDebugState("在物品栏中制作... 为了 " + toGet);
        return mod.getModSettings().shouldUseCraftingBookToCraft()
                ? new CraftGenericWithRecipeBooksTask(_target)
                : new CraftGenericManuallyTask(_target);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            List<Slot> moveTo = mod.getItemStorage().getSlotsThatCanFitInPlayerInventory(cursorStack, false);
            if (!moveTo.isEmpty()) {
                for (Slot MoveTo : moveTo) {
                    mod.getSlotHandler().clickSlot(MoveTo, 0, SlotActionType.PICKUP);
                }
            } else {
                Optional<Slot> garbageSlot = StorageHelper.getGarbageSlot(mod);
                if (garbageSlot.isPresent()) {
                    mod.getSlotHandler().clickSlot(garbageSlot.get(), 0, SlotActionType.PICKUP);
                } else {
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                }
            }
        }
    }

    // TODO 检查这是否破坏了某些功能... 但通常这不应该拾取物品
    @Override
    protected double getPickupRange(AltoClef mod) {
        // 制作任务不需要拾取范围
        return 0;
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CraftInInventoryTask task) {
            if (!task._target.equals(_target)) return false;
            return isCraftingEqual(task);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return toCraftingDebugStringName() + " " + _target;
    }

    /**
     * 虚拟方法。默认假设子任务是目录化的（在TaskCatalogue.java中）
     * @param mod AltoClef实例
     * @return 返回收集配方子任务
     */
    protected Task collectRecipeSubTask(AltoClef mod) {
        return new CollectRecipeCataloguedResourcesTask(_ignoreUncataloguedSlots, _target);
    }

    /**
     * 返回制作调试字符串名称
     * @return 制作任务的调试名称
     */
    protected String toCraftingDebugStringName() {
        return "制作 2x2 任务";
    }

    /**
     * 检查制作是否相等
     * @param other 另一个制作任务
     * @return 如果制作任务相等返回true
     */
    protected boolean isCraftingEqual(CraftInInventoryTask other) {
        return true;
    }

    /**
     * 获取配方目标
     * @return 返回配方目标
     */
    public RecipeTarget getRecipeTarget() {
        return _target;
    }
}
