package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.multiversion.versionedfields.Items;
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
import adris.altoclef.util.slots.BlastFurnaceSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BlastFurnaceScreenHandler;
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
 * 高炉烧炼任务 - 在高炉中烧炼物品，必要时放置高炉并收集燃料
 */
public class SmeltInBlastFurnaceTask extends ResourceTask {

    private final SmeltTarget[] _targets; // 烧炼目标数组

    private final DoSmeltInBlastFurnaceTask _doTask; // 执行烧炼的具体任务

    public SmeltInBlastFurnaceTask(SmeltTarget[] targets) {
        super(extractItemTargets(targets));
        _targets = targets;
        // TODO: 按顺序执行多个目标
        _doTask = new DoSmeltInBlastFurnaceTask(targets[0]);
    }

    public SmeltInBlastFurnaceTask(SmeltTarget target) {
        this(new SmeltTarget[]{target});
    }

    private static ItemTarget[] extractItemTargets(SmeltTarget[] recipeTargets) {
        List<ItemTarget> result = new ArrayList<>(recipeTargets.length);
        for (SmeltTarget target : recipeTargets) {
            result.add(target.getItem());
        }
        return result.toArray(ItemTarget[]::new);
    }

    public void ignoreMaterials() {
        _doTask.ignoreMaterials();
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBehaviour().push();
        if (_targets.length != 1) {
            Debug.logWarning("尝试烧炼多个目标，但一次只支持一个目标！");
        }
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        Optional<BlockPos> blastFurnacePos = mod.getBlockScanner().getNearestBlock(Blocks.BLAST_FURNACE);
        blastFurnacePos.ifPresent(blockPos -> mod.getBehaviour().avoidBlockBreaking(blockPos));
        return _doTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
        // Close blast furnace screen
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // Try throwing away cursor slot if it's garbage
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
        if (other instanceof SmeltInBlastFurnaceTask task) {
            return task._doTask.isEqual(_doTask);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return _doTask.toDebugString();
    }

    public SmeltTarget[] getTargets() {
        return _targets;
    }

    @SuppressWarnings("ConditionCoveredByFurtherCondition")
    /**
     * 执行高炉烧炼的具体任务类
     */
    static class DoSmeltInBlastFurnaceTask extends DoStuffInContainerTask {

        private final SmeltTarget _target; // 烧炼目标
        private final BlastFurnaceCache _blastFurnaceCache = new BlastFurnaceCache(); // 高炉缓存
        private final ItemTarget _allMaterials; // 所有材料（包括可选材料）
        private boolean _ignoreMaterials; // 是否忽略材料检查

        public DoSmeltInBlastFurnaceTask(SmeltTarget target) {
            super(Blocks.BLAST_FURNACE, new ItemTarget(Items.BLAST_FURNACE));
            _target = target;
            _allMaterials = new ItemTarget(Stream.concat(Arrays.stream(_target.getMaterial().getMatches()), Arrays.stream(_target.getOptionalMaterials())).toArray(Item[]::new), _target.getMaterial().getTargetCount());
        }

        public void ignoreMaterials() {
            _ignoreMaterials = true;
        }

        @Override
        protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
            if (other instanceof DoSmeltInBlastFurnaceTask task) {
                return task._target.equals(_target) && task._ignoreMaterials == _ignoreMaterials;
            }
            return false;
        }

        @Override
        protected boolean isContainerOpen(AltoClef mod) {
            return (mod.getPlayer().currentScreenHandler instanceof BlastFurnaceScreenHandler);
        }

        @Override
        protected void onStart() {
            super.onStart();
            AltoClef mod = AltoClef.getInstance();

            // 保护必要的物品，防止在烧炼过程中被丢弃
            mod.getBehaviour().addProtectedItems(ItemHelper.PLANKS);
            mod.getBehaviour().addProtectedItems(Items.COAL);
            mod.getBehaviour().addProtectedItems(_allMaterials.getMatches());
            mod.getBehaviour().addProtectedItems(_target.getMaterial().getMatches());
        }

        @Override
        protected Task onTick() {
            AltoClef mod = AltoClef.getInstance();

            tryUpdateOpenBlastFurnace(mod);
            // Include both regular + optional items
            ItemTarget materialTarget = _allMaterials;
            ItemTarget outputTarget = _target.getItem();
            // Materials needed = (mat_target (- 0*mat_in_inventory) - out_in_inventory - mat_in_furnace - out_in_furnace)
            // ^ 0 * mat_in_inventory because we always care aobut the TARGET materials, not how many LEFT there are.
            int materialsNeeded = materialTarget.getTargetCount()
                    /*- mod.getItemStorage().getItemCountInventoryOnly(materialTarget.getMatches())*/ // See comment above
                    - mod.getItemStorage().getItemCountInventoryOnly(outputTarget.getMatches())
                    - (materialTarget.matches(_blastFurnaceCache.materialSlot.getItem()) ? _blastFurnaceCache.materialSlot.getCount() : 0)
                    - (outputTarget.matches(_blastFurnaceCache.outputSlot.getItem()) ? _blastFurnaceCache.outputSlot.getCount() : 0);
            double totalFuelInBlastFurnace = ItemHelper.getFuelAmount(_blastFurnaceCache.fuelSlot) + _blastFurnaceCache.burningFuelCount + _blastFurnaceCache.burnPercentage;
            // Fuel needed = (mat_target - out_in_inventory - out_in_furnace - totalFuelInFurnace)
            double fuelNeeded = _ignoreMaterials
                    ? Math.min(materialTarget.matches(_blastFurnaceCache.materialSlot.getItem()) ? _blastFurnaceCache.materialSlot.getCount() : 0, materialTarget.getTargetCount())
                    : materialTarget.getTargetCount()
                    /* - mod.getItemStorage().getItemCountInventoryOnly(materialTarget.getMatches()) */
                    - mod.getItemStorage().getItemCountInventoryOnly(outputTarget.getMatches())
                    - (outputTarget.matches(_blastFurnaceCache.outputSlot.getItem()) ? _blastFurnaceCache.outputSlot.getCount() : 0)
                    - totalFuelInBlastFurnace;

            // 材料不足...
            if (mod.getItemStorage().getItemCount(materialTarget.getMatches()) < materialsNeeded) {
                setDebugState("获取材料");
                return getMaterialTask(_target.getMaterial());
            }

            // 燃料不足...
            if (_blastFurnaceCache.burningFuelCount <= 0 && StorageHelper.calculateInventoryFuelCount(mod) < fuelNeeded) {
                setDebugState("获取燃料");
                return new CollectFuelTask(fuelNeeded + 1);
            }

            // 确保我们的材料在背包中可访问
            if (StorageHelper.isItemInaccessibleToContainer(mod, _allMaterials)) {
                return new MoveInaccessibleItemToInventoryTask(_allMaterials);
            }

            // 我们有足够的燃料和材料。前往容器并开始烧炼！
            return super.onTick();
        }

        // Override this if our materials must be acquired in a special way.
        // virtual
        protected Task getMaterialTask(ItemTarget target) {
            return TaskCatalogue.getItemTask(target);
        }

        @Override
        protected Task containerSubTask(AltoClef mod) {
            // We have appropriate materials/fuel.
            /*
             * - 如果输出槽有物品，接收它
             * - 计算需要的材料输入，如果没有就放入
             * - 计算需要的燃料输入，如果没有就放入
             * - 等待烧炼完成
             */
            ItemStack output = StorageHelper.getItemStackInSlot(BlastFurnaceSlot.OUTPUT_SLOT);
            ItemStack material = StorageHelper.getItemStackInSlot(BlastFurnaceSlot.INPUT_SLOT_MATERIALS);
            ItemStack fuel = StorageHelper.getItemStackInSlot(BlastFurnaceSlot.INPUT_SLOT_FUEL);

            // Receive from output if present
            double currentlyCachedWhileCooking = StorageHelper.getBlastFurnaceFuel() + StorageHelper.getBlastFurnaceCookPercent();
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
                            // Eh screw it
                            if (ItemHelper.canThrowAwayStack(mod, cursor)) {
                                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                                return null;
                            }
                        }
                    }
                    mod.getSlotHandler().clickSlot(BlastFurnaceSlot.INPUT_SLOT_FUEL, 0, SlotActionType.PICKUP);
                    return null;
                }
            }
            if (!output.isEmpty()) {
                setDebugState("接收输出");
                // 确保光标为空或能接收我们的物品
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
                // 拾取输出物品
                mod.getSlotHandler().clickSlot(BlastFurnaceSlot.OUTPUT_SLOT, 0, SlotActionType.PICKUP);
                return null;
                // return new MoveItemToSlotTask(new ItemTarget(output.getItem(), output.getCount()), toMoveTo.get(), mod -> FurnaceSlot.OUTPUT_SLOT);
            }

            // 如果需要，填充输入槽
            // 槽中所需材料 = (材料目标数量 - 背包中的产出数量 - 炉子中的产出数量)
            ItemTarget materialTarget = _allMaterials;

            int neededMaterialsInSlot = materialTarget.getTargetCount()
                    - mod.getItemStorage().getItemCountInventoryOnly(_target.getItem().getMatches())
                    - (_target.getItem().matches(output.getItem()) ? output.getCount() : 0);
            // 我们没有正确的材料或者需要更多
            if (!_allMaterials.matches(material.getItem()) || neededMaterialsInSlot > material.getCount()) {
                int materialsAlreadyIn = (materialTarget.matches(material.getItem()) ? material.getCount() : 0);
                setDebugState("移动材料");
                return new MoveItemToSlotFromInventoryTask(new ItemTarget(materialTarget, neededMaterialsInSlot - materialsAlreadyIn), BlastFurnaceSlot.INPUT_SLOT_MATERIALS);
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
                double currentlyCached = StorageHelper.getBlastFurnaceFuel() + StorageHelper.getBlastFurnaceCookPercent();
                double needs = material.getCount() - currentlyCached;
                if (needs > 0) {
                    // 获取最佳燃料进行填充
                    double closestDelta = Double.NEGATIVE_INFINITY;
                    ItemStack bestStack = null;
                    for (ItemStack stack : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
                        if (mod.getModSettings().isSupportedFuel(stack.getItem())) {
                            double fuelAmount = ItemHelper.getFuelAmount(stack.getItem()) * stack.getCount();
                            double delta = needs - fuelAmount;
                            if (
                                    (bestStack == null) ||
                                            // 如果当前最佳超出需求，优先选择更低的值
                                            (closestDelta > 0 && delta < closestDelta) ||
                                            // 如果当前最佳低于需求，优先选择更高的值（但仍低于需求）
                                            (closestDelta < 0 && delta < 0 && delta > closestDelta)
                            ) {
                                bestStack = stack;
                                closestDelta = delta;
                            }
                        }
                    }
                    if (bestStack != null) {
                        setDebugState("填充燃料");
                        return new MoveItemToSlotFromInventoryTask(new ItemTarget(bestStack.getItem(), bestStack.getCount()), BlastFurnaceSlot.INPUT_SLOT_FUEL);
                    }
                }
            }

            setDebugState("等待烧炼...");
            return null;
        }

        @Override
        protected double getCostToMakeNew(AltoClef mod) {
            if (_blastFurnaceCache.burnPercentage > 0 || _blastFurnaceCache.burningFuelCount > 0 ||
                    _blastFurnaceCache.fuelSlot != null || _blastFurnaceCache.materialSlot != null ||
                    _blastFurnaceCache.outputSlot != null) {
                return 9999999.0;
            }
            if (mod.getItemStorage().getItemCount(Items.COBBLESTONE) > 11 &&
                    mod.getItemStorage().getItemCount(Items.RAW_IRON) > 5) {
                double cost = 100.0 - 90.0 * (((double) mod.getItemStorage().getItemCount(new Item[]{Items.COBBLESTONE})
                        / 8.0) + ((double) mod.getItemStorage().getItemCount(Items.RAW_IRON) / 5.0));
                return Math.max(cost, 10.0);
            }
            return StorageHelper.miningRequirementMetInventory(MiningRequirement.WOOD) ? 50.0 : 100.0;
        }

        @Override
        protected BlockPos overrideContainerPosition(AltoClef mod) {
            // If we have a valid container position, KEEP it.
            return getTargetContainerPosition();
        }

        private void tryUpdateOpenBlastFurnace(AltoClef mod) {
            if (isContainerOpen(mod)) {
                // Update current furnace cache
                _blastFurnaceCache.burnPercentage = StorageHelper.getBlastFurnaceCookPercent();
                _blastFurnaceCache.burningFuelCount = StorageHelper.getBlastFurnaceFuel();
                _blastFurnaceCache.fuelSlot = StorageHelper.getItemStackInSlot(BlastFurnaceSlot.INPUT_SLOT_FUEL);
                _blastFurnaceCache.materialSlot = StorageHelper.getItemStackInSlot(BlastFurnaceSlot.INPUT_SLOT_MATERIALS);
                _blastFurnaceCache.outputSlot = StorageHelper.getItemStackInSlot(BlastFurnaceSlot.OUTPUT_SLOT);
            }
        }
    }

    /**
     * 高炉缓存类 - 存储高炉当前状态
     */
    static class BlastFurnaceCache {
        public ItemStack materialSlot = ItemStack.EMPTY; // 材料槽
        public ItemStack fuelSlot = ItemStack.EMPTY; // 燃料槽
        public ItemStack outputSlot = ItemStack.EMPTY; // 输出槽
        public double burningFuelCount; // 正在燃烧的燃料数量
        public double burnPercentage; // 烧炼进度百分比
    }
}
