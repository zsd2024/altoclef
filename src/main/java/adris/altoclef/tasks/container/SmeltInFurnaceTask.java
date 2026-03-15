package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.BotBehaviour;
import adris.altoclef.Debug;
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
import adris.altoclef.util.slots.FurnaceSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.FurnaceScreenHandler;
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
 * 在熔炉中烧炼物品，根据需要放置熔炉并收集燃料。
 */
public class SmeltInFurnaceTask extends ResourceTask {
    // 烧炼目标数组
    private final SmeltTarget[] _targets;

    // 执行熔炼任务
    private final DoSmeltInFurnaceTask _doTask;

    /**
     * 构造函数，指定烧炼目标数组
     * @param targets 烧炼目标数组
     */
    public SmeltInFurnaceTask(SmeltTarget[] targets) {
        super(extractItemTargets(targets));
        _targets = targets;
        // TODO: 按顺序执行
        _doTask = new DoSmeltInFurnaceTask(targets[0]);
    }

    /**
     * 构造函数，指定单一烧炼目标
     * @param target 烧炼目标
     */
    public SmeltInFurnaceTask(SmeltTarget target) {
        this(new SmeltTarget[]{target});
    }

    /**
     * 从烧炼目标数组中提取物品目标
     * @param recipeTargets 烧炼目标数组
     * @return 返回物品目标数组
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
        _doTask.ignoreMaterials();
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        // 在烧炼任务中不应避免拾取
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBehaviour().push();
        if (_targets.length != 1) {
            Debug.logWarning("尝试烧炼多个目标，一次只支持一个目标！");
        }
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        Optional<BlockPos> furnacePos = mod.getBlockScanner().getNearestBlock(Blocks.FURNACE);
        furnacePos.ifPresent(blockPos -> mod.getBehaviour().avoidBlockBreaking(blockPos));
        return _doTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
        // 关闭熔炉界面
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
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
        }
    }

    @Override
    public boolean isFinished() {
        return super.isFinished() || _doTask.isFinished();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof SmeltInFurnaceTask task) {
            return task._doTask.isEqual(_doTask);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return _doTask.toDebugString();
    }

    /**
     * 获取烧炼目标数组
     * @return 返回烧炼目标数组
     */
    public SmeltTarget[] getTargets() {
        return _targets;
    }


    /**
     * 在熔炉中执行烧炼的内部任务类
     * 负责管理熔炉操作的详细流程
     */
    static class DoSmeltInFurnaceTask extends DoStuffInContainerTask {

        // 烧炼目标
        private final SmeltTarget target;
        // 熔炉缓存
        private final FurnaceCache furnaceCache = new FurnaceCache();
        // 所有材料
        private final ItemTarget allMaterials;
        // 是否忽略材料检查
        private boolean ignoreMaterials;

        /**
         * 构造函数
         * @param target 烧炼目标
         */
        public DoSmeltInFurnaceTask(SmeltTarget target) {
            super(Blocks.FURNACE, new ItemTarget(Items.FURNACE));
            this.target = target;
            // 合并普通材料和可选材料
            allMaterials = new ItemTarget(Stream.concat(Arrays.stream(this.target.getMaterial().getMatches()), Arrays.stream(this.target.getOptionalMaterials())).toArray(Item[]::new), this.target.getMaterial().getTargetCount());
        }

        /**
         * 忽略材料检查
         */
        public void ignoreMaterials() {
            ignoreMaterials = true;
        }

        @Override
        protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
            if (other instanceof DoSmeltInFurnaceTask task) {
                return task.target.equals(target) && task.ignoreMaterials == ignoreMaterials;
            }
            return false;
        }

        @Override
        protected boolean isContainerOpen(AltoClef mod) {
            // 检查是否打开了熔炉界面
            return (mod.getPlayer().currentScreenHandler instanceof FurnaceScreenHandler);
        }

        @Override
        protected void onStart() {
            super.onStart();
            BotBehaviour botBehaviour = AltoClef.getInstance().getBehaviour();

            // 添加需要保护的物品
            botBehaviour.addProtectedItems(ItemHelper.PLANKS);
            botBehaviour.addProtectedItems(Items.COAL);
            botBehaviour.addProtectedItems(allMaterials.getMatches());
            botBehaviour.addProtectedItems(target.getMaterial().getMatches());
        }

        @Override
        protected Task onTick() {
            AltoClef mod = AltoClef.getInstance();

            tryUpdateOpenFurnace(mod);
            // 包含普通和可选物品
            ItemTarget materialTarget = allMaterials;
            ItemTarget outputTarget = target.getItem();
            // 需要的材料数量 = (材料目标数量 - 产出在物品栏中的数量 - 熔炉中的材料数量 - 熔炉中的产出数量)
            // ^ 0 * mat_in_inventory 因为我们总是关心TARGET材料，而不是剩余数量。
            int materialsNeeded = materialTarget.getTargetCount()
                    /*- mod.getItemStorage().getItemCountInventoryOnly(materialTarget.getMatches())*/ // 参见上面的注释
                    - mod.getItemStorage().getItemCountInventoryOnly(outputTarget.getMatches())
                    - (materialTarget.matches(furnaceCache.materialSlot.getItem()) ? furnaceCache.materialSlot.getCount() : 0)
                    - (outputTarget.matches(furnaceCache.outputSlot.getItem()) ? furnaceCache.outputSlot.getCount() : 0);
            double totalFuelInFurnace = ItemHelper.getFuelAmount(furnaceCache.fuelSlot) + furnaceCache.burningFuelCount + furnaceCache.burnPercentage;
            // 需要的燃料 = (材料目标数量 - 产出在物品栏中的数量 - 熔炉中的产出数量 - 熔炉中的总燃料)
            double fuelNeeded = ignoreMaterials
                    ? Math.min(materialTarget.matches(furnaceCache.materialSlot.getItem()) ? furnaceCache.materialSlot.getCount() : 0, materialTarget.getTargetCount())
                    : materialTarget.getTargetCount()
                    /* - mod.getItemStorage().getItemCountInventoryOnly(materialTarget.getMatches()) */
                    - mod.getItemStorage().getItemCountInventoryOnly(outputTarget.getMatches())
                    - (outputTarget.matches(furnaceCache.outputSlot.getItem()) ? furnaceCache.outputSlot.getCount() : 0)
                    - totalFuelInFurnace;

            // 我们没有足够的材料...
            if (mod.getItemStorage().getItemCount(materialTarget.getMatches()) < materialsNeeded) {
                setDebugState("获取材料");
                return getMaterialTask(target.getMaterial());
            }

            // 我们没有足够的燃料...
            if (furnaceCache.burningFuelCount <= 0 && StorageHelper.calculateInventoryFuelCount(mod) < fuelNeeded) {
                setDebugState("获取燃料");
                return new CollectFuelTask(fuelNeeded + 1);
            }

            // 确保我们的材料在物品栏中是可访问的
            if (StorageHelper.isItemInaccessibleToContainer(mod, allMaterials)) {
                return new MoveInaccessibleItemToInventoryTask(allMaterials);
            }

            // 我们有燃料和材料。前往容器并烧炼！
            return super.onTick();
        }

        // 如果我们的材料必须以特殊方式获取，请重写此方法。
        // 虚方法
        /**
         * 获取材料任务
         * @param target 材料目标
         * @return 返回获取材料的任务
         */
        protected Task getMaterialTask(ItemTarget target) {
            return TaskCatalogue.getItemTask(target);
        }

        @Override
        protected Task containerSubTask(AltoClef mod) {
            // 我们有合适的材料/燃料。
            /*
             * - 如果输出槽有物品，接收它。
             * - 计算需要的材料输入。如果没有，放入它。
             * - 计算需要的燃料输入。如果没有，放入它。
             * - 等待
             */
            ItemStack output = StorageHelper.getItemStackInSlot(FurnaceSlot.OUTPUT_SLOT);
            ItemStack material = StorageHelper.getItemStackInSlot(FurnaceSlot.INPUT_SLOT_MATERIALS);
            ItemStack fuel = StorageHelper.getItemStackInSlot(FurnaceSlot.INPUT_SLOT_FUEL);

            // 如果存在输出，接收它
            double currentlyCachedWhileCooking = StorageHelper.getFurnaceFuel() + StorageHelper.getFurnaceCookPercent();
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
                            // 算了
                            if (ItemHelper.canThrowAwayStack(mod, cursor)) {
                                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                                return null;
                            }
                        }
                    }
                    mod.getSlotHandler().clickSlot(FurnaceSlot.INPUT_SLOT_FUEL, 0, SlotActionType.PICKUP);
                    return null;
                }
            }
            if (!output.isEmpty()) {
                setDebugState("接收产出");
                // 确保我们的光标为空/可以接收我们的物品
                ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
                if (!ItemHelper.canStackTogether(output, cursor)) {
                    Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false);
                    if (toFit.isPresent()) {
                        mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
                        return null;
                    } else {
                        // 算了
                        if (ItemHelper.canThrowAwayStack(mod, cursor)) {
                            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                            return null;
                        }
                    }
                }
                // 拾取
                mod.getSlotHandler().clickSlot(FurnaceSlot.OUTPUT_SLOT, 0, SlotActionType.PICKUP);
                return null;
                // return new MoveItemToSlotTask(new ItemTarget(output.getItem(), output.getCount()), toMoveTo.get(), mod -> FurnaceSlot.OUTPUT_SLOT);
            }

            // 填充输入（如果需要）
            // 槽中需要的材料数量 = (材料目标数量 - 物品栏中的产出数量 - 熔炉中的产出数量)
            ItemTarget materialTarget = allMaterials;

            int neededMaterialsInSlot = materialTarget.getTargetCount()
                    - mod.getItemStorage().getItemCountInventoryOnly(target.getItem().getMatches())
                    - (target.getItem().matches(output.getItem()) ? output.getCount() : 0);
            // 我们没有正确的材料或需要更多
            if (!allMaterials.matches(material.getItem()) || neededMaterialsInSlot > material.getCount()) {
                int materialsAlreadyIn = (materialTarget.matches(material.getItem()) ? material.getCount() : 0);
                setDebugState("移动材料");
                return new MoveItemToSlotFromInventoryTask(new ItemTarget(materialTarget, neededMaterialsInSlot - materialsAlreadyIn), FurnaceSlot.INPUT_SLOT_MATERIALS);
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
            // 填充燃料（如果需要）
            if (fuel.isEmpty() || ItemHelper.isFuel(fuel.getItem())) {
                double currentlyCached = StorageHelper.getFurnaceFuel() + StorageHelper.getFurnaceCookPercent();
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
                                            // 如果我们的最佳值过高，优先选择较低的值
                                            (closestDelta > 0 && delta < closestDelta) ||
                                            // 如果我们的最佳值过低，优先选择更高的较低值
                                            (delta < 0 && delta > closestDelta)
                            ) {
                                bestStack = stack;
                                closestDelta = delta;
                            }
                        }
                    }
                    if (bestStack != null) {
                        setDebugState("填充燃料");
                        return new MoveItemToSlotFromInventoryTask(new ItemTarget(bestStack.getItem(), bestStack.getCount()), FurnaceSlot.INPUT_SLOT_FUEL);
                    }
                }
            }

            setDebugState("等待中...");
            return null;
        }

        @Override
        protected double getCostToMakeNew(AltoClef mod) {
            // 如果熔炉正在工作或有物品，成本很高
            if (furnaceCache.burnPercentage > 0 || furnaceCache.burningFuelCount > 0 ||
                    !furnaceCache.fuelSlot.isEmpty() || !furnaceCache.materialSlot.isEmpty() ||
                    !furnaceCache.outputSlot.isEmpty()) {
                return 9999999.0;
            }
            // 如果有足够圆石，成本降低
            if (mod.getItemStorage().getItemCount(Items.COBBLESTONE) > 8) {
                double cost = 100.0 - 90.0 * (double) mod.getItemStorage().getItemCount(new Item[]{Items.COBBLESTONE}) / 8.0;
                return Math.max(cost, 10.0);
            }
            // 如果拥有木头工具要求，成本较低
            return StorageHelper.miningRequirementMetInventory(MiningRequirement.WOOD) ? 50.0 : 100.0;
        }

        @Override
        protected BlockPos overrideContainerPosition(AltoClef mod) {
            // 如果我们有一个有效的容器位置，保持它。
            return getTargetContainerPosition();
        }

        /**
         * 尝试更新打开的熔炉状态
         * @param mod AltoClef实例
         */
        private void tryUpdateOpenFurnace(AltoClef mod) {
            if (isContainerOpen(mod)) {
                // 更新当前熔炉缓存
                furnaceCache.burnPercentage = StorageHelper.getFurnaceCookPercent();
                furnaceCache.burningFuelCount = StorageHelper.getFurnaceFuel();
                furnaceCache.fuelSlot = StorageHelper.getItemStackInSlot(FurnaceSlot.INPUT_SLOT_FUEL);
                furnaceCache.materialSlot = StorageHelper.getItemStackInSlot(FurnaceSlot.INPUT_SLOT_MATERIALS);
                furnaceCache.outputSlot = StorageHelper.getItemStackInSlot(FurnaceSlot.OUTPUT_SLOT);
            }
        }
    }

    /**
     * 熔炉缓存类，用于存储熔炉各槽位状态
     */
    static class FurnaceCache {
        // 材料槽
        public ItemStack materialSlot = ItemStack.EMPTY;
        // 燃料槽
        public ItemStack fuelSlot = ItemStack.EMPTY;
        // 输出槽
        public ItemStack outputSlot = ItemStack.EMPTY;
        // 燃烧中的燃料数量
        public double burningFuelCount = 0;
        // 燃烧百分比
        public double burnPercentage = 0;
    }
}
