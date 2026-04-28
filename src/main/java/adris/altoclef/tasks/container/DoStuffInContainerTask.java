package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Optional;


/**
 * 在容器内执行操作的抽象任务基类。
 * 如果附近没有找到合适的容器，会自动获取并放置一个容器。
 */
public abstract class DoStuffInContainerTask extends Task {

    private final ItemTarget containerTarget; // 容器物品目标
    private final Block[] containerBlocks; // 容器方块类型数组

    private final PlaceBlockNearbyTask placeTask; // 放置容器方块的任务
    // 如果决定放置容器，则强制放置至少1秒（原为10秒）
    private final TimerGame placeForceTimer = new TimerGame(1);

    // 如果刚刚放置了容器，停止放置并尝试前往最近的容器
    private final TimerGame justPlacedTimer = new TimerGame(3);
    private BlockPos cachedContainerPosition = null; // 缓存的容器位置
    private Task openTableTask; // 打开容器的任务

    public DoStuffInContainerTask(Block[] containerBlocks, ItemTarget containerTarget) {
        this.containerBlocks = containerBlocks;
        this.containerTarget = containerTarget;

        placeTask = new PlaceBlockNearbyTask(this.containerBlocks);
    }

    public DoStuffInContainerTask(Block containerBlock, ItemTarget containerTarget) {
        this(new Block[]{containerBlock}, containerTarget);
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();
        mod.getBehaviour().push();
        if (openTableTask == null) {
            openTableTask = new DoToClosestBlockTask(InteractWithBlockTask::new, containerBlocks);
        }

        // 保护容器物品，因为我们可能会放置它
        mod.getBehaviour().addProtectedItems(ItemHelper.blocksToItems(containerBlocks));
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();
        // 如果正在放置容器，继续放置
        if (mod.getItemStorage().hasItem(ItemHelper.blocksToItems(containerBlocks)) && placeTask.isActive() && !placeTask.isFinished()) {
            setDebugState("正在放置容器");
            return placeTask;
        }

        if (isContainerOpen(mod)) {
            return containerSubTask(mod);
        }

        // infinity if such a container does not exist.
        double costToWalk = Double.POSITIVE_INFINITY;

        Optional<BlockPos> nearest;

        Vec3d currentPos = mod.getPlayer().getPos();
        BlockPos override = overrideContainerPosition(mod);

        if (override != null && mod.getBlockScanner().isBlockAtPosition(override, containerBlocks)) {
            // 我们有覆盖位置，直接前往那里
            nearest = Optional.of(override);
        } else {
            // 跟踪最近的容器
            nearest = mod.getBlockScanner().getNearestBlock(currentPos, blockPos -> WorldHelper.canReach(blockPos), containerBlocks);
        }
        if (nearest.isEmpty()) {
            // 如果其他方法都失败了，尝试使用我们放置的任务
            nearest = Optional.ofNullable(placeTask.getPlaced());
            if (nearest.isPresent() && !mod.getBlockScanner().isBlockAtPosition(nearest.get(), containerBlocks)) {
                nearest = Optional.empty();
            }
        }
        if (nearest.isPresent()) {
            costToWalk = BaritoneHelper.calculateGenericHeuristic(currentPos, WorldHelper.toVec3d(nearest.get()));
        }

        // 如果前往现有容器的成本很高，则制作一个新的容器
        // 如果我们卡在某些情况下，也继续制作容器
        if (costToWalk > getCostToMakeNew(mod)) {
            placeForceTimer.reset();
        }
        if (nearest.isEmpty() || (!placeForceTimer.elapsed() && justPlacedTimer.elapsed())) {
            // 制作一个新的容器更便宜，或者这是我们唯一的选择

            // 我们不再前往之前的容器
            cachedContainerPosition = null;

            // 如果我们没有容器物品...
            if (!mod.getItemStorage().hasItem(containerTarget)) {
                setDebugState("获取容器物品");
                return TaskCatalogue.getItemTask(containerTarget);
            }

            setDebugState("正在放置容器...");

            justPlacedTimer.reset();
            // 现在放置！
            return placeTask;
        }

        // 这段代码非常复杂（原注释：insanely cursed）
        // TODO: 完全使用Optional，这段代码很丑陋
        cachedContainerPosition = nearest.get();

        // 走向容器并打开它

        // 等待进食
        if (mod.getFoodChain().needsToEat()) {
            setDebugState("等待进食...");
            return null;
        }
        setDebugState("走向容器... " + nearest.get().toShortString());

        if (!StorageHelper.getItemStackInCursorSlot().isEmpty()) {
            Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(StorageHelper.getItemStackInCursorSlot(), false);
            if (toMoveTo.isEmpty()) {
                return new EnsureFreeInventorySlotTask();
            }
            if (ItemHelper.canThrowAwayStack(mod, StorageHelper.getItemStackInCursorSlot())) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                return null;
            }
            mod.getSlotHandler().clickSlot(toMoveTo.get(), 0, SlotActionType.PICKUP);
            return null;
        }
        return openTableTask;
        //return new GetToBlockTask(nearest, true);
    }

    public ItemTarget getContainerTarget() {
        return containerTarget;
    }

    // Virtual
    protected BlockPos overrideContainerPosition(AltoClef mod) {
        return null;
    }

    protected BlockPos getTargetContainerPosition() {
        return cachedContainerPosition;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof DoStuffInContainerTask task) {
            if (!Arrays.equals(task.containerBlocks, containerBlocks)) return false;
            if (!task.containerTarget.equals(containerTarget)) return false;
            return isSubTaskEqual(task);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "在 " + containerTarget + " 容器中执行操作";
    }

    protected abstract boolean isSubTaskEqual(DoStuffInContainerTask other);

    protected abstract boolean isContainerOpen(AltoClef mod);

    protected abstract Task containerSubTask(AltoClef mod);

    protected abstract double getCostToMakeNew(AltoClef mod);
}
