package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceObsidianBucketTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 收集黑曜石任务
 * 用于收集黑曜石，通过挖掘现有黑曜石或通过岩浆与水交互生成
 */
public class CollectObsidianTask extends ResourceTask {

    private final TimerGame _placeWaterTimeout = new TimerGame(6); // 放置水的超时计时器
    private final MovementProgressChecker _lavaTimeout = new MovementProgressChecker(); // 岩浆超时检查器
    private final Set<BlockPos> _lavaBlacklist = new HashSet<>(); // 岩浆黑名单
    private final int _count; // 目标黑曜石数量
    private Task _forceCompleteTask = null; // 强制完成任务
    private BlockPos _lavaWaitCurrentPos; // 当前等待的岩浆位置

    private PlaceObsidianBucketTask _placeObsidianTask; // 放置黑曜石任务

    public CollectObsidianTask(int count) {
        super(Items.OBSIDIAN, count);
        _count = count;
    }

    /**
     * 获取岩浆结构位置
     * @param lavaPos 岩浆位置
     * @return 岩浆结构位置
     */
    private static BlockPos getLavaStructurePos(BlockPos lavaPos) {
        return lavaPos.add(1,1,0);
    }

    /**
     * 获取岩浆水位置
     * @param lavaPos 岩浆位置
     * @return 岩浆上方水的位置
     */
    private static BlockPos getLavaWaterPos(BlockPos lavaPos) {
        return lavaPos.up();
    }

    /**
     * 获取合适的黑曜石位置
     * @param mod AltoClef实例
     * @return 合适的位置或null
     */
    private static BlockPos getGoodObsidianPosition(AltoClef mod) {
        BlockPos start = mod.getPlayer().getBlockPos().add(-3,-3,-3);
        BlockPos end = mod.getPlayer().getBlockPos().add(3,3,3);
        for (BlockPos pos : WorldHelper.scanRegion(start, end)) {
            if (!WorldHelper.canBreak(pos) || !WorldHelper.canPlace(pos)) {
                return null;
            }
        }
        return mod.getPlayer().getBlockPos();
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBehaviour().push();

        mod.getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);

        // 避免在我们试图挖掘的岩浆块上放置方块
        mod.getBehaviour().avoidBlockPlacing(pos -> {
            if (_lavaWaitCurrentPos != null) {
                return pos.equals(_lavaWaitCurrentPos) || pos.equals(getLavaWaterPos(_lavaWaitCurrentPos));
            }
            return false;
        });
        mod.getBehaviour().avoidBlockBreaking(pos -> {
            if (_lavaWaitCurrentPos != null) {
                return pos.equals(getLavaStructurePos(_lavaWaitCurrentPos));
            }
            return false;
        });
    }

    @Override
    protected adris.altoclef.tasksystem.Task onResourceTick(AltoClef mod) {

        // 如果不再为岩浆，则清除当前等待的岩浆位置
        if (_lavaWaitCurrentPos != null && mod.getChunkTracker().isChunkLoaded(_lavaWaitCurrentPos) && mod.getWorld().getBlockState(_lavaWaitCurrentPos).getBlock() != Blocks.LAVA) {
            _lavaWaitCurrentPos = null;
        }

        // 首先获取钻石镐
        if (!StorageHelper.miningRequirementMet(MiningRequirement.DIAMOND)) {
            setDebugState("首先获取钻石镐");
            return new SatisfyMiningRequirementTask(MiningRequirement.DIAMOND);
        }

        if (_forceCompleteTask != null && _forceCompleteTask.isActive() && !_forceCompleteTask.isFinished()) {
            return _forceCompleteTask;
        }

        Predicate<BlockPos> goodObsidian = (blockPos ->
                blockPos.isWithinDistance(mod.getPlayer().getPos(), 800)
                        && WorldHelper.canBreak(blockPos)
        );

        /*
        // 检查附近是否有黑曜石
        // 为什么我们这样做？
        //      - 因为我们的'portal'任务保护我们的黑曜石
        boolean obsidianNearby = false;
        BlockPos start = mod.getPlayer().getBlockPos().add(-3, -3, -3);
        BlockPos end = mod.getPlayer().getBlockPos().add(3, 3, 3);
        for (BlockPos pos : WorldUtil.scanRegion(mod, start, end)) {
            if (mod.getBlockTracker().blockIsValid(pos, Blocks.OBSIDIAN) && !badObsidian.test(pos)) {
                obsidianNearby = true;
                break;
            }
        }
         */
        if (/*obsidianNearby || */mod.getBlockScanner().anyFound(goodObsidian, Blocks.OBSIDIAN) || mod.getEntityTracker().itemDropped(Items.OBSIDIAN)) {
            /*
            // 清除附近水
            BlockPos nearestObby = mod.getBlockTracker().getNearestTracking(mod.getPlayer().getPos(), Blocks.OBSIDIAN);
            if (nearestObby != null) {
                BlockPos nearestWater = mod.getBlockTracker().getNearestTracking(WorldWorldHelper.toVec3d(nearestObby), blockPos -> !WorldUtil.isSourceBlock(mod, blockPos, true), Blocks.WATER);

                if (nearestWater != null && nearestWater.getSquaredDistance(nearestObby) < 5 * 5) {
                    _forceCompleteTask = new ClearLiquidTask(nearestWater);
                    setDebugState("Clearing water nearby obsidian");
                    return _forceCompleteTask;
                }
            }
             */

            setDebugState("挖掘/收集黑曜石");
            _placeObsidianTask = null;
            return new MineAndCollectTask(new ItemTarget(Items.OBSIDIAN, _count), new Block[]{Blocks.OBSIDIAN}, MiningRequirement.DIAMOND);
        }

        if (WorldHelper.getCurrentDimension() == Dimension.NETHER) {
            final double AVERAGE_GOLD_PER_OBSIDIAN = 11.475;
            int gold_buffer = (int) (AVERAGE_GOLD_PER_OBSIDIAN * _count);
            setDebugState("我们无法放置水，所以通过交易获取黑曜石");
            return new TradeWithPiglinsTask(gold_buffer, Items.OBSIDIAN, _count);
        }

        if (_placeObsidianTask == null) {
            BlockPos goodPos = getGoodObsidianPosition(mod);
            if (goodPos != null) {
                _placeObsidianTask = new PlaceObsidianBucketTask(goodPos);
            } else {
                setDebugState("行走直到找到一个放置黑曜石的位置");
                return new TimeoutWanderTask();
            }
        }
        // 尝试看看是否可以将黑曜石放置器移到更靠近岩浆的位置
        //noinspection ConstantConditions
        if (_placeObsidianTask != null && !mod.getItemStorage().hasItem(Items.LAVA_BUCKET)) {
            // 我们移动得离放置点有点远，当获取岩浆桶时这个任务会停止运行
            // (这正是我们希望运行且仅运行到这里的时候！)
            if (!_placeObsidianTask.getPos().isWithinDistance(mod.getPlayer().getPos(), 4)) {
                BlockPos goodPos = getGoodObsidianPosition(mod);
                if (goodPos != null) {
                    Debug.logMessage("(将黑曜石目标点移近)");
                    _placeObsidianTask = new PlaceObsidianBucketTask(goodPos);
                }
            }
        }

        // lmfao
        setDebugState("放置黑曜石");
        return _placeObsidianTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, adris.altoclef.tasksystem.Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CollectObsidianTask task) {
            return task._count == _count;
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个黑曜石方块";
    }
}
