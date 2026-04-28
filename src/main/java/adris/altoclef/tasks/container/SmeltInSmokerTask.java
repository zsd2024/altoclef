package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.BotBehaviour;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.resources.CollectFuelTask;
import adris.altoclef.tasks.slot.MoveInaccessibleItemToInventoryTask;
import adris.altoclef.tasks.slot.MoveItemToSlotFromInventoryTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.SmeltTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.slots.SmokerSlot;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


// 参考
// https://minecraft.gamepedia.com/Smelting

/**
 * 在烟熏炉中进行烧炼任务，必要时会放置烟熏炉并收集燃料。
 */
public class SmeltInSmokerTask extends ResourceTask {

    private final SmeltTarget target; // 目标烧炼配方

    private final DoSmeltInSmokerTask doTask; // 实际执行烧炼的子任务

    public SmeltInSmokerTask(SmeltTarget target) {
        super(extractItemTargets(new SmeltTarget[]{target}));
        this.target = target;
        // TODO: 按顺序执行。
        boolean ignoreMaterials = false;
        doTask = new DoSmeltInSmokerTask(target, ignoreMaterials);
    }



    /**
     * 从烧炼目标数组中提取物品目标数组
     * @param recipeTargets 烧炼目标数组
     * @return 物品目标数组
     */
    private static ItemTarget[] extractItemTargets(SmeltTarget[] recipeTargets) {
        List<ItemTarget> result = new ArrayList<>(recipeTargets.length);
        for (SmeltTarget target : recipeTargets) {
            result.add(target.getItem());
        }
        return result.toArray(ItemTarget[]::new);
    }

    /**
     * 忽略材料检查
     */
    public void ignoreMaterials() {
        doTask.ignoreMaterials();
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 保存当前行为设置
        mod.getBehaviour().push();
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 获取最近的烟熏炉位置
        Optional<BlockPos> smokerPos = mod.getBlockScanner().getNearestBlock(Blocks.SMOKER);
        // 避免破坏烟熏炉
        smokerPos.ifPresent(blockPos -> mod.getBehaviour().avoidBlockBreaking(blockPos));
        return doTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 恢复之前的行为设置
        mod.getBehaviour().pop();
        // 关闭烟熏炉界面
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            // 尝试将光标中的物品放入玩家背包
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            // 如果可以丢弃光标中的物品，则丢弃
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            // 尝试将垃圾物品放入垃圾桶槽位
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // 如果光标槽位是垃圾，尝试丢弃
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            // 直接关闭界面
            StorageHelper.closeScreen();
        }
    }

    @Override
    public boolean isFinished() {
        // 检查资源任务或子任务是否已完成
        return super.isFinished() || doTask.isFinished();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof SmeltInSmokerTask task) {
            return task.doTask.isEqual(doTask);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return doTask.toDebugString();
    }

    /**
     * 获取烧炼目标数组
     * @return 烧炼目标数组
     */
    public SmeltTarget[] getTargets() {
        return new SmeltTarget[]{target};
    }

    @SuppressWarnings("ConditionCoveredByFurtherCondition")
    /**
     * 执行烟熏炉烧炼的实际任务类
     */
    static class DoSmeltInSmokerTask extends DoStuffInContainerTask {

        private final SmeltTarget _target; // 烧炼目标
        private final SmokerCache _smokerCache = new SmokerCache(); // 烟熏炉缓存
        private final ItemTarget _allMaterials; // 所有需要的材料（包括可选材料）
        private boolean _ignoreMaterials; // 是否忽略材料检查

        public DoSmeltInSmokerTask(SmeltTarget target, boolean ignoreMaterials) {
            super(Blocks.SMOKER, new ItemTarget(Items.SMOKER));
            _target = target;
            _ignoreMaterials = ignoreMaterials;
            // 合并主要材料和可选材料
            _allMaterials = new ItemTarget(Stream.concat(Arrays.stream(_target.getMaterial().getMatches()), Arrays.stream(_target.getOptionalMaterials())).toArray(Item[]::new), _target.getMaterial().getTargetCount());
        }

        /**
         * 设置忽略材料检查
         */
        public void ignoreMaterials() {
            _ignoreMaterials = true;
        }

        @Override
        protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
            if (other instanceof DoSmeltInSmokerTask task) {
                return task._target.equals(_target) && task._ignoreMaterials == _ignoreMaterials;
            }
            return false;
        }

        @Override
        protected boolean isContainerOpen(AltoClef mod) {
            // 检查是否打开了烟熏炉界面
            return (mod.getPlayer().currentScreenHandler instanceof SmokerScreenHandler);
        }

        @Override
        protected void onStart() {
            super.onStart();
            BotBehaviour botBehaviour = AltoClef.getInstance().getBehaviour();

            // 保护木板、煤炭、所有材料和目标材料不被自动丢弃
            botBehaviour.addProtectedItems(ItemHelper.PLANKS);
            botBehaviour.addProtectedItems(Items.COAL);
            botBehaviour.addProtectedItems(_allMaterials.getMatches());
            botBehaviour.addProtectedItems(_target.getMaterial().getMatches());
        }

        @Override
        protected Task onTick() {
            AltoClef mod = AltoClef.getInstance();

            // 更新打开的烟熏炉状态
            tryUpdateOpenSmoker(mod);
            // 包含常规材料和可选材料
            ItemTarget materialTarget = _allMaterials;
            ItemTarget outputTarget = _target.getItem();
            // 所需材料 = (目标材料数量 - 背包中的成品数量 - 烟熏炉中的材料数量 - 烟熏炉中的成品数量)
            // ^ 不考虑背包中的材料数量，因为我们只关心目标材料数量，而不是剩余数量。
            int materialsNeeded = materialTarget.getTargetCount()
                    /*- mod.getItemStorage().getItemCountInventoryOnly(materialTarget.getMatches())*/ // 参见上面的注释
                    - mod.getItemStorage().getItemCountInventoryOnly(outputTarget.getMatches())
                    - (materialTarget.matches(_smokerCache.materialSlot.getItem()) ? _smokerCache.materialSlot.getCount() : 0)
                    - (outputTarget.matches(_smokerCache.outputSlot.getItem()) ? _smokerCache.outputSlot.getCount() : 0);
            double totalFuelInSmoker = ItemHelper.getFuelAmount(_smokerCache.fuelSlot) + _smokerCache.burningFuelCount + _smokerCache.burnPercentage;
            // 所需燃料 = (目标材料数量 - 背包中的成品数量 - 烟熏炉中的成品数量 - 烟熏炉中的总燃料量)
            double fuelNeeded = _ignoreMaterials
                    ? Math.min(materialTarget.matches(_smokerCache.materialSlot.getItem()) ? _smokerCache.materialSlot.getCount() : 0, materialTarget.getTargetCount())
                    : materialTarget.getTargetCount()
                    /* - mod.getItemStorage().getItemCountInventoryOnly(materialTarget.getMatches()) */
                    - mod.getItemStorage().getItemCountInventoryOnly(outputTarget.getMatches())
                    - (outputTarget.matches(_smokerCache.outputSlot.getItem()) ? _smokerCache.outputSlot.getCount() : 0)
                    - totalFuelInSmoker;

            // 材料不足...
            if (mod.getItemStorage().getItemCount(materialTarget.getMatches()) < materialsNeeded) {
                setDebugState("获取材料");
                return getMaterialTask(_target.getMaterial());
            }

            // 燃料不足...
            if (_smokerCache.burningFuelCount <= 0 && StorageHelper.calculateInventoryFuelCount(mod) < fuelNeeded) {
                setDebugState("获取燃料");
                return new CollectFuelTask(fuelNeeded + 1);
            }

            // 确保我们的材料在背包中可访问
            if (StorageHelper.isItemInaccessibleToContainer(mod, _allMaterials)) {
                return new MoveInaccessibleItemToInventoryTask(_allMaterials);
            }

            // 我们有足够的燃料和材料。前往容器并进行烧炼！
            return super.onTick();
        }

        // 如果我们的材料需要以特殊方式获取，请重写此方法。
        // 虚方法
        protected Task getMaterialTask(ItemTarget target) {
            return TaskCatalogue.getItemTask(target);
        }

        @Override
        protected Task containerSubTask(AltoClef mod) {
            // 我们有合适的材料/燃料。
            /*
             * - 如果输出槽有物品，接收它。
             * - 计算所需的材料输入。如果没有，放入。
             * - 计算所需的燃料输入。如果没有，放入。
             * - 等待
             */
            ItemStack output = StorageHelper.getItemStackInSlot(SmokerSlot.OUTPUT_SLOT);
            ItemStack material = StorageHelper.getItemStackInSlot(SmokerSlot.INPUT_SLOT_MATERIALS);
            ItemStack fuel = StorageHelper.getItemStackInSlot(SmokerSlot.INPUT_SLOT_FUEL);

            // 如果正在烧炼且不需要更多燃料，则移除现有燃料
            double currentlyCachedWhileCooking = StorageHelper.getSmokerFuel() + StorageHelper.getSmokerCookPercent();
            double needsWhileCooking = material.getCount() - currentlyCachedWhileCooking;
            if (needsWhileCooking <= 0) {
                if (!fuel.isEmpty()) {
                    ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
                    if (!ItemHelper.canStackTogether(fuel, cursor)) {
                        Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false);
                        if (toFit.isPresent()) {
                            mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
                            return null;
                        } else {
                            // 唉，算了
                            if (ItemHelper.canThrowAwayStack(mod, cursor)) {
                                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                                return null;
                            }
                        }
                    }
                    mod.getSlotHandler().clickSlot(SmokerSlot.INPUT_SLOT_FUEL, 0, SlotActionType.PICKUP);
                    return null;
                }
            }
            // 如果输出槽不为空，接收输出物品
            if (!output.isEmpty()) {
                setDebugState("接收输出");
                // 确保光标为空或可以接收我们的物品
                ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
                if (!ItemHelper.canStackTogether(output, cursor)) {
                    Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false);
                    if (toFit.isPresent()) {
                        mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
                        return null;
                    } else {
                        // 唉，算了
                        if (ItemHelper.canThrowAwayStack(mod, cursor)) {
                            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                            return null;
                        }
                    }
                }
                // 拾取物品
                mod.getSlotHandler().clickSlot(SmokerSlot.OUTPUT_SLOT, 0, SlotActionType.PICKUP);
                return null;
                // return new MoveItemToSlotTask(new ItemTarget(output.getItem(), output.getCount()), toMoveTo.get(), mod -> FurnaceSlot.OUTPUT_SLOT);
            }

            // 如果需要，填充输入材料
            // 槽中所需材料 = (目标材料数量 - 背包中的成品数量 - 烟熏炉中的成品数量)
            ItemTarget materialTarget = _allMaterials;

            int neededMaterialsInSlot = materialTarget.getTargetCount()
                    - mod.getItemStorage().getItemCountInventoryOnly(_target.getItem().getMatches())
                    - (_target.getItem().matches(output.getItem()) ? output.getCount() : 0);
            // 我们没有正确的材料或者需要更多
            if (!_allMaterials.matches(material.getItem()) || neededMaterialsInSlot > material.getCount()) {
                int materialsAlreadyIn = (materialTarget.matches(material.getItem()) ? material.getCount() : 0);
                setDebugState("移动材料");
                return new MoveItemToSlotFromInventoryTask(new ItemTarget(materialTarget, neededMaterialsInSlot - materialsAlreadyIn), SmokerSlot.INPUT_SLOT_MATERIALS);
            }

            /*
            double currentFuel = _ignoreMaterials
                    ? (Math.min(materialTarget.matches(_furnaceCache.materialSlot.getItem()) ? _furnaceCache.materialSlot.getCount() : 0, materialTarget.getTargetCount())
                    : materialTarget.getTargetCount()
                    - mod.getItemStorage().getItemCountInventoryOnly(materialTarget.getMatches())
                    - mod.getItemStorage().getItemCountInventoryOnly(outputTarget.getMatches())
                    - (outputTarget.matches(_furnaceCache.outputSlot.getItem()) ? _furnaceCache.outputSlot.getCount() : 0)
                    - totalFuelInFurnace;
             */
            // 如果需要，填充燃料
            if (fuel.isEmpty() || ItemHelper.isFuel(fuel.getItem())) {
                double currentlyCached = StorageHelper.getSmokerFuel() + StorageHelper.getSmokerCookPercent();
                double needs = material.getCount() - currentlyCached;
                if (needs > 0) {
                    // 获取最佳燃料填充
                    double closestDelta = Double.NEGATIVE_INFINITY;
                    ItemStack bestStack = null;
                    for (ItemStack stack : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
                        if (mod.getModSettings().isSupportedFuel(stack.getItem())) {
                            double fuelAmount = ItemHelper.getFuelAmount(stack.getItem()) * stack.getCount();
                            double delta = needs - fuelAmount;
                            if (
                                    (bestStack == null) ||
                                            // 如果我们的最佳选择高于需求，优先选择较低的值
                                            (closestDelta > 0 && delta < closestDelta) ||
                                            // 如果我们的最佳选择低于需求，优先选择较高的低于值
                                            (closestDelta < 0 && delta < 0 && delta > closestDelta)
                            ) {
                                bestStack = stack;
                                closestDelta = delta;
                            }
                        }
                    }
                    if (bestStack != null) {
                        setDebugState("填充燃料");
                        return new MoveItemToSlotFromInventoryTask(new ItemTarget(bestStack.getItem(), bestStack.getCount()), SmokerSlot.INPUT_SLOT_FUEL);
                    }
                }
            }

            setDebugState("等待...");
            return null;
        }

        @Override
        protected double getCostToMakeNew(AltoClef mod) {
            // 如果烟熏炉正在使用中，返回高成本以避免创建新的
            if (_smokerCache.burnPercentage > 0 || _smokerCache.burningFuelCount > 0 ||
                    _smokerCache.fuelSlot != null || _smokerCache.materialSlot != null ||
                    _smokerCache.outputSlot != null) {
                return 9999999.0;
            }
            // 如果有足够的圆石和原木，计算制作烟熏炉的成本
            if (mod.getItemStorage().getItemCount(Items.COBBLESTONE) > 8 &&
                    mod.getItemStorage().getItemCount(ItemHelper.LOG) > 4) {
                double cost = 100.0 - 90.0 * (((double) mod.getItemStorage().getItemCount(new Item[]{Items.COBBLESTONE})
                        / 8.0) + ((double) mod.getItemStorage().getItemCount(ItemHelper.LOG) / 4.0));
                return Math.max(cost, 10.0);
            }
            // 检查是否满足木材挖掘要求
            return StorageHelper.miningRequirementMetInventory(MiningRequirement.WOOD) ? 50.0 : 100.0;
        }

        @Override
        protected BlockPos overrideContainerPosition(AltoClef mod) {
            // 如果我们有有效的容器位置，保持它。
            return getTargetContainerPosition();
        }

        /**
         * 尝试更新打开的烟熏炉状态
         */
        private void tryUpdateOpenSmoker(AltoClef mod) {
            if (isContainerOpen(mod)) {
                // 更新当前烟熏炉缓存
                _smokerCache.burnPercentage = StorageHelper.getSmokerCookPercent();
                _smokerCache.burningFuelCount = StorageHelper.getSmokerFuel();
                _smokerCache.fuelSlot = StorageHelper.getItemStackInSlot(SmokerSlot.INPUT_SLOT_FUEL);
                _smokerCache.materialSlot = StorageHelper.getItemStackInSlot(SmokerSlot.INPUT_SLOT_MATERIALS);
                _smokerCache.outputSlot = StorageHelper.getItemStackInSlot(SmokerSlot.OUTPUT_SLOT);
            }
        }
    }

    /**
     * 烟熏炉缓存类，用于存储烟熏炉各槽位的状态
     */
    static class SmokerCache {
        public ItemStack materialSlot = ItemStack.EMPTY; // 材料槽
        public ItemStack fuelSlot = ItemStack.EMPTY; // 燃料槽
        public ItemStack outputSlot = ItemStack.EMPTY; // 输出槽
        public double burningFuelCount; // 正在燃烧的燃料数量
        public double burnPercentage; // 烧炼进度百分比
    }
}
