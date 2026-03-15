package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 收集农作物任务
 * 用于收集成熟的农作物（如小麦、胡萝卜、马铃薯等），并可选择性地重新种植
 */
public class CollectCropTask extends ResourceTask {

    private final ItemTarget _cropToCollect; // 要收集的作物目标
    private final Item[] _cropSeed; // 作物种子
    private final Predicate<BlockPos> _canBreak; // 判断是否可以破坏的谓词
    private final Block[] _cropBlock; // 作物方块数组

    private final Set<BlockPos> _emptyCropland = new HashSet<>(); // 空农田位置集合

    private final Task _collectSeedTask; // 收集种子任务

    // 为防止无限区块卸载-加载循环错误
    private final HashSet<BlockPos> _wasFullyGrown = new HashSet<>(); // 记录曾经成熟过的作物位置

    public CollectCropTask(ItemTarget cropToCollect, Block[] cropBlock, Item[] cropSeed, Predicate<BlockPos> canBreak) {
        super(cropToCollect);
        _cropToCollect = cropToCollect;
        _cropSeed = cropSeed;
        _canBreak = canBreak;
        _cropBlock = cropBlock;
        _collectSeedTask = new PickupDroppedItemTask(new ItemTarget(cropSeed, 1), true);
    }

    public CollectCropTask(ItemTarget cropToCollect, Block[] cropBlock, Item... cropSeed) {
        this(cropToCollect, cropBlock, cropSeed, canBreak -> true);
    }

    public CollectCropTask(ItemTarget cropToCollect, Block cropBlock, Item... cropSeed) {
        this(cropToCollect, new Block[]{cropBlock}, cropSeed);
    }

    public CollectCropTask(Item cropItem, int count, Block cropBlock, Item... cropSeed) {
        this(new ItemTarget(cropItem, count), cropBlock, cropSeed);
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        /*
         *   过滤空作物列表以移除非空区域
         *   如果 _currentCropBreaking != null && _currentCropBreaking 是空的:
         *      将 _currentCropBreaking 添加到空农田
         * - 如果空农田列表不为空 && mod.getSettings().shouldReplaceCrops() && 我们库存中有作物种子:
         *      执行到最近方块任务：与种子交互
         *   执行到最近方块任务：
         *      设置 _currentCropBreaking = 方块
         *      破坏方块
         */

        // 如果需要，收集种子
        if (hasEmptyCrops(mod) && mod.getModSettings().shouldReplantCrops() && !mod.getItemStorage().hasItem(_cropSeed)) {
            if (_collectSeedTask.isActive() && !_collectSeedTask.isFinished()) {
                setDebugState("拾取掉落的种子");
                return _collectSeedTask;
            }
            if (mod.getEntityTracker().itemDropped(_cropSeed)) {
                Optional<ItemEntity> closest = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), _cropSeed);
                if (closest.isPresent() && closest.get().isInRange(mod.getPlayer(), 7)) {
                    // 触发种子的收集
                    return _collectSeedTask;
                }
            }
        }

        // 如果需要，重新种植！
        if (shouldReplantNow(mod)) {
            setDebugState("重新种植...");
            // 我们保证空农田列表包含有效的空方块。我们可以在此阶段清除
            _emptyCropland.removeIf(blockPos -> !isEmptyCrop(mod, blockPos));
            assert !_emptyCropland.isEmpty();
            return new DoToClosestBlockTask(
                    blockPos -> new InteractWithBlockTask(new ItemTarget(_cropSeed, 1), Direction.UP, blockPos.down(), true),
                    pos -> _emptyCropland.stream().min(StlHelper.compareValues(block -> BlockPosVer.getSquaredDistance(block,pos))),
                    _emptyCropland::contains,
                    Blocks.FARMLAND); // Blocks.FARMLAND 放在这里没有用
        }

        Predicate<BlockPos> validCrop = blockPos -> {
            if (!_canBreak.test(blockPos)) return false;
            // 破坏未成熟的作物只会产生一个输出！这是个糟糕的举动
            if (mod.getModSettings().shouldReplantCrops() && !isMature(mod, blockPos)) return false;
            // 小麦必须始终成熟
            if (mod.getWorld().getBlockState(blockPos).getBlock() == Blocks.WHEAT)
                return isMature(mod, blockPos);
            return true;
        };

        // 维度
        if (isInWrongDimension(mod) && !mod.getBlockScanner().anyFound(validCrop, _cropBlock)) {
            return getToCorrectDimensionTask(mod);
        }

        // 破坏作物方块
        setDebugState("破坏作物");
        return new DoToClosestBlockTask(
                blockPos -> {
                    _emptyCropland.add(blockPos);
                    return new DestroyBlockTask(blockPos);
                },
                validCrop,
                _cropBlock
        );
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    public boolean isFinished() {
        // 在重新种植作物时不要停止
        if (shouldReplantNow(AltoClef.getInstance())) {
            return false;
        }
        return super.isFinished();
    }

    private boolean shouldReplantNow(AltoClef mod) {
        return mod.getModSettings().shouldReplantCrops() && hasEmptyCrops(mod) && mod.getItemStorage().hasItem(_cropSeed);
    }

    private boolean hasEmptyCrops(AltoClef mod) {
        for (BlockPos pos : _emptyCropland) {
            if (isEmptyCrop(mod, pos)) return true;
        }
        return false;
    }

    private boolean isEmptyCrop(AltoClef mod, BlockPos pos) {
        return WorldHelper.isAir(pos);
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CollectCropTask task) {
            return Arrays.equals(task._cropSeed, _cropSeed) && Arrays.equals(task._cropBlock, _cropBlock) && task._cropToCollect.equals(_cropToCollect);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "收集作物: " + _cropToCollect;
    }


    /**
     * 检查作物是否成熟
     * @param mod AltoClef实例
     * @param blockPos 作物位置
     * @return 作物是否成熟
     */
    private boolean isMature(AltoClef mod, BlockPos blockPos) {
        // 检查小麦成熟度需要加载区块
        if (!mod.getChunkTracker().isChunkLoaded(blockPos) || !WorldHelper.canReach(blockPos)) {
            return _wasFullyGrown.contains(blockPos);
        }
        // 如果不是成熟/完全生长的小麦，则修剪
        BlockState s = mod.getWorld().getBlockState(blockPos);
        if (s.getBlock() instanceof CropBlock crop) {
            boolean mature = crop.isMature(s);
            if (_wasFullyGrown.contains(blockPos)) {
                if (!mature) _wasFullyGrown.remove(blockPos);
            } else {
                if (mature) _wasFullyGrown.add(blockPos);
            }
            return mature;
        }
        // 不是作物方块
        return false;
    }
}
