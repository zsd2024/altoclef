package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;
import adris.altoclef.tasks.CraftGenericManuallyTask;
import adris.altoclef.tasks.CraftGenericWithRecipeBooksTask;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.CollectRecipeCataloguedResourcesTask;
import adris.altoclef.tasks.slot.MoveInaccessibleItemToInventoryTask;
import adris.altoclef.tasks.slot.ReceiveCraftingOutputSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.JankCraftingRecipeMapping;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * 工作台合成任务
 * 在工作台中合成物品，如果没有找到工作台则会获取并放置一个
 */
public class CraftInTableTask extends ResourceTask {

    private final RecipeTarget[] targets;

    private final DoCraftInTableTask craftTask;

    public CraftInTableTask(RecipeTarget[] targets) {
        super(extractItemTargets(targets));
        this.targets = targets;
        craftTask = new DoCraftInTableTask(this.targets);
    }

    public CraftInTableTask(RecipeTarget target, boolean collect, boolean ignoreUncataloguedSlots) {
        super(new ItemTarget(target.getOutputItem(), target.getTargetCount()));
        targets = new RecipeTarget[]{target};
        craftTask = new DoCraftInTableTask(targets, collect, ignoreUncataloguedSlots);
    }

    public CraftInTableTask(RecipeTarget target) {
        this(target, true, true);
    }

    /**
     * 从配方目标中提取物品目标
     *
     * @param recipeTargets 配方目标数组
     * @return 物品目标数组
     */
    private static ItemTarget[] extractItemTargets(RecipeTarget[] recipeTargets) {
        // 使用Java流将每个配方目标映射为新的物品目标
        return Arrays.stream(recipeTargets)
                .map(t -> new ItemTarget(t.getOutputItem(), t.getTargetCount()))
                .toArray(ItemTarget[]::new);
    }

    /**
     * 确定玩家是否应避免拾取物品
     *
     * @param mod AltoClef模组实例
     * @return 如果玩家应避免拾取物品则返回true，否则返回false
     */
    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    /**
     * 资源任务开始时调用
     *
     * @param mod AltoClef模组实例
     */
    @Override
    protected void onResourceStart(AltoClef mod) {

    }

    /**
     * 资源管理器每次tick时调用此方法
     * 返回每次tick应执行的任务
     *
     * @param mod AltoClef模组实例
     * @return 每次tick要执行的任务
     */
    @Override
    protected Task onResourceTick(AltoClef mod) {
        return craftTask;
    }

    /**
     * 资源任务停止时重写的方法
     *
     * @param mod           AltoClef模组
     * @param interruptTask 中断任务
     */
    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 获取光标槽中的物品堆栈
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();

        // 如果光标槽不为空，则处理它
        if (!cursorStack.isEmpty()) {
            // 查找玩家背包中可以容纳光标槽物品的槽位
            Optional<Slot> moveToSlot = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);

            // 如果找到槽位，则从光标槽拾取物品并移动到找到的槽位
            moveToSlot.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));

            // 如果物品可以丢弃，则从光标槽拾取物品并丢弃
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }

            // 查找垃圾槽并将光标槽中的物品移动到垃圾槽
            Optional<Slot> garbageSlot = StorageHelper.getGarbageSlot(mod);
            garbageSlot.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
        } else {
            // 如果光标槽为空，则关闭界面
            StorageHelper.closeScreen();
        }

        // 拾取未定义的槽位
        mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
    }

    /**
     * 检查给定的ResourceTask是否等于此CraftInTableTask
     *
     * @param other 要比较的ResourceTask
     * @return 如果ResourceTask是CraftInTableTask且其craftTask等于此任务的craftTask，则返回true，否则返回false
     */
    @Override
    protected boolean isEqualResource(ResourceTask other) {
        // 检查其他任务是否为CraftInTableTask的实例
        if (other instanceof CraftInTableTask task) {
            // 比较两个任务的craftTask
            return craftTask.isEqual(task.craftTask);
        }
        // 其他任务不是CraftInTableTask，返回false
        return false;
    }

    /**
     * 返回craft任务的调试字符串名称
     * 如果craft任务不为null，则调用craft任务的toDebugString()方法并返回结果
     * 否则返回null
     *
     * @return craft任务的调试字符串名称，如果craft任务为null则返回null
     */
    @Override
    protected String toDebugStringName() {
        return (craftTask != null) ? craftTask.toDebugString() : null;
    }

    /**
     * 返回配方目标的副本
     *
     * @return 配方目标
     */
    public RecipeTarget[] getRecipeTargets() {
        return Arrays.copyOf(targets, targets.length);
    }
}


class DoCraftInTableTask extends DoStuffInContainerTask {

    private final float CRAFT_RESET_TIMER_BONUS_SECONDS = 10;

    private final RecipeTarget[] _targets;

    private final boolean _collect;

    private final CollectRecipeCataloguedResourcesTask _collectTask;
    private final TimerGame _craftResetTimer = new TimerGame(CRAFT_RESET_TIMER_BONUS_SECONDS);
    private int _craftCount;

    public DoCraftInTableTask(RecipeTarget[] targets, boolean collect, boolean ignoreUncataloguedSlots) {
        super(Blocks.CRAFTING_TABLE, new ItemTarget("工作台"));
        _collectTask = new CollectRecipeCataloguedResourcesTask(false, targets);
        _targets = targets;
        _collect = collect;
    }

    public DoCraftInTableTask(RecipeTarget[] targets) {
        this(targets, true, false);
    }

    /**
     * 模组启动时重写的方法
     * 重构以处理物品管理和界面关闭
     * 重置收集任务
     */
    @Override
    protected void onStart() {
        super.onStart();
        AltoClef mod = AltoClef.getInstance();
        // 保存当前行为和合成计数
        mod.getBehaviour().push();
        _craftCount = 0;

        // 检查光标槽中是否有物品
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();

        if (!cursorStack.isEmpty()) {
            // 将物品移动到玩家背包中可以容纳它的槽位
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));

            // 检查物品是否可以丢弃
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                // 丢弃物品
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }

            // 将物品移动到垃圾槽
            StorageHelper.getGarbageSlot(mod).ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));

            // 清空光标槽
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            // 如果光标槽中没有物品，则关闭界面
            StorageHelper.closeScreen();
        }

        // 重置收集任务
        _collectTask.reset();

        // 将保护物品添加到行为中
        mod.getBehaviour().addProtectedItems(getMaterialsArray());
    }

    /**
     * 任务被中断或停止时调用此方法
     * 执行处理任务中断或停止所需的必要操作
     *
     * @param interruptTask 导致中断的任务，如果手动停止任务则为null
     */
    @Override
    protected void onStop(Task interruptTask) {
        // 获取光标槽中的物品堆栈
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        AltoClef mod = AltoClef.getInstance();

        // 如果光标槽为空，则关闭界面
        if (cursorStack.isEmpty()) {
            StorageHelper.closeScreen();
        } else {
            // 获取可以容纳光标槽物品的槽位
            Optional<Slot> moveToSlot = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);

            // 如果找到槽位，则将光标槽物品移动到该槽位
            moveToSlot.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));

            // 如果光标槽物品可以丢弃，则丢弃它
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }

            // 获取垃圾槽
            Optional<Slot> garbageSlot = StorageHelper.getGarbageSlot(mod);

            // 如果找到垃圾槽，则将光标槽物品移动到该槽位
            garbageSlot.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));

            // 将光标槽物品移动到未定义槽位
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        }

        // 调用父类的onStop方法
        super.onStop(interruptTask);

        // 从堆栈中弹出行为
        mod.getBehaviour().pop();
    }

    /**
     * 定期调用此方法执行与合成相关的任务
     *
     * @return 要执行的下一个任务
     */
    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 避免破坏工作台
        List<BlockPos> craftingTablePositions = mod.getBlockScanner().getKnownLocations(Blocks.CRAFTING_TABLE);
        for (BlockPos craftingTablePos : craftingTablePositions) {
            mod.getBehaviour().avoidBlockBreaking(craftingTablePos);
        }

        // 检查玩家背包是否打开且光标槽为空
        if (StorageHelper.isPlayerInventoryOpen() && StorageHelper.getItemStackInCursorSlot().isEmpty()) {
            // 获取合成输出槽中的物品
            Item outputItem = StorageHelper.getItemStackInSlot(PlayerSlot.CRAFT_OUTPUT_SLOT).getItem();
            // 检查输出物品是否匹配任何目标且目标数量未达到
            for (RecipeTarget target : _targets) {
                if (target.getOutputItem() == outputItem && mod.getItemStorage().getItemCount(target.getOutputItem()) < target.getTargetCount()) {
                    return new ReceiveCraftingOutputSlotTask(PlayerSlot.CRAFT_OUTPUT_SLOT, target.getTargetCount());
                }
            }
        }

        // 检查是否需要收集物品且收集任务未完成
        if (_collect && !_collectTask.isFinished() && !StorageHelper.hasRecipeMaterialsOrTarget(mod, _targets)) {
            return _collectTask;
        }

        // 如果容器未打开，则重置合成重置计时器
        if (!isContainerOpen(mod)) {
            _craftResetTimer.reset();
        }

        // 检查配方中是否有任何无法访问的物品并将其移动到背包
        if (!thisOrChildSatisfies(task -> task instanceof CraftInInventoryTask)) {
            for (RecipeTarget target : _targets) {
                for (int slot = 0; slot < target.getRecipe().getSlotCount(); ++slot) {
                    ItemTarget toCheck = target.getRecipe().getSlot(slot);
                    if (StorageHelper.isItemInaccessibleToContainer(mod, toCheck)) {
                        return new MoveInaccessibleItemToInventoryTask(toCheck);
                    }
                }
            }
        }

        // 调用父类方法
        return super.onTick();
    }

    /**
     * 检查给定的DoStuffInContainerTask是否等于此任务
     *
     * @param other 要比较的其他DoStuffInContainerTask
     * @return 如果任务相等则返回True，否则返回False
     */
    @Override
    protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
        // 检查其他任务是否为DoCraftInTableTask的实例
        if (other instanceof DoCraftInTableTask task) {
            // 比较两个任务的目标数组
            return Arrays.equals(task._targets, _targets);
        }
        // 其他任务不是DoCraftInTableTask的实例，因此它们不相等
        return false;
    }

    /**
     * 检查容器是否已打开
     *
     * @param mod AltoClef模组实例
     * @return 如果容器已打开则返回True，否则返回false
     */
    @Override
    protected boolean isContainerOpen(AltoClef mod) {
        return mod.getPlayer().currentScreenHandler instanceof CraftingScreenHandler;
    }

    /**
     * 执行容器子任务
     *
     * @param mod AltoClef模组实例
     * @return 要执行的子任务
     */
    @Override
    protected Task containerSubTask(AltoClef mod) {
        // 根据容器物品移动延迟和额外持续时间计算间隔
        float interval = mod.getModSettings().getContainerItemMoveDelay() * 10 + CRAFT_RESET_TIMER_BONUS_SECONDS;
        _craftResetTimer.setInterval(interval);

        // 如果合成重置计时器已超时，则返回TimeoutWanderTask
        if (_craftResetTimer.elapsed()) {
            return new TimeoutWanderTask(5);
        }

        // 遍历每个目标配方
        for (RecipeTarget target : _targets) {
            // 检查输出物品数量是否达到目标数量
            if (mod.getItemStorage().getItemCount(target.getOutputItem()) >= target.getTargetCount()) {
                continue;
            }

            // 根据目标配方和输出物品获取要发送的配方
            Optional<WrappedRecipeEntry> recipeToSend = JankCraftingRecipeMapping.getMinecraftMappedRecipe(target.getRecipe(), target.getOutputItem());

            // 获取客户端玩家实体
            ClientPlayerEntity player = MinecraftClient.getInstance().player;

            // 如果启用了合成书，存在要发送的配方，且玩家的合成书中包含该配方，则返回CraftGenericWithRecipeBooksTask
            if (mod.getModSettings().shouldUseCraftingBookToCraft() && recipeToSend.isPresent()) {
                assert player != null;
                if (player.getRecipeBook().contains(recipeToSend.get().id())) {
                    return new CraftGenericWithRecipeBooksTask(target);
                }
            }

            // 默认返回CraftGenericManuallyTask
            return new CraftGenericManuallyTask(target);
        }

        return null;
    }

    /**
     * 检查指定的模组是否已完成
     *
     * @return 如果模组已完成则返回True，否则返回false
     */
    @Override
    public boolean isFinished() {
        // 检查合成计数是否大于或等于目标数量
        return _craftCount >= _targets.length;
    }

    /**
     * 返回制作新AltoClef模组的成本
     *
     * @param mod AltoClef模组实例
     * @return 制作新AltoClef模组的成本
     */
    @Override
    protected double getCostToMakeNew(AltoClef mod) {
        // 获取最近的工作台
        Optional<BlockPos> closestCraftingTable = mod.getBlockScanner().getNearestBlock(Blocks.CRAFTING_TABLE);

        // 如果工作台在玩家40格范围内，则返回正无穷大
        if (closestCraftingTable.isPresent() && closestCraftingTable.get().isWithinDistance(mod.getPlayer().getPos(), 40)) {
            return Double.POSITIVE_INFINITY;
        }

        // 如果模组有原木或足够的木板，则返回成本10
        if (mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 4) {
            return 10;
        }

        // 否则返回成本100
        return 100;
    }

    /**
     * 返回材料数组
     *
     * @return 材料数组
     */
    private Item[] getMaterialsArray() {
        List<Item> result = new ArrayList<>();

        // 遍历每个目标
        for (RecipeTarget target : _targets) {
            // 遍历配方中的每个槽位
            for (int i = 0; i < target.getRecipe().getSlotCount(); ++i) {
                ItemTarget materialTarget = target.getRecipe().getSlot(i);
                // 检查材料目标是否不为null且有匹配项
                if (materialTarget != null && materialTarget.getMatches() != null) {
                    // 将所有匹配项添加到结果列表中
                    Collections.addAll(result, materialTarget.getMatches());
                }
            }
        }

        // 将结果列表转换为数组并返回
        return result.toArray(new Item[0]);
    }

}
