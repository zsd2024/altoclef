package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 查找最近的可到达方块并在该方块上执行任务。
 */
public class DoToClosestBlockTask extends AbstractDoToClosestObjectTask<BlockPos> {

    // 目标方块数组
    private final Block[] targetBlocks;

    // 获取起始位置的函数
    private final Supplier<Vec3d> getOriginPos;
    // 获取最近方块的函数
    private final Function<Vec3d, Optional<BlockPos>> getClosest;
    
    // 获取目标任务的函数
    private final Function<BlockPos, Task> getTargetTask;

    // 验证方块是否有效的谓词
    private final Predicate<BlockPos> isValid;

    /**
     * 构造函数，指定起始位置获取器、目标任务生成器、最近方块获取器、有效性验证器和目标方块
     * @param getOriginSupplier 起始位置获取器
     * @param getTargetTask 目标任务生成器
     * @param getClosestBlock 最近方块获取器
     * @param isValid 有效性验证器
     * @param blocks 目标方块
     */
    public DoToClosestBlockTask(Supplier<Vec3d> getOriginSupplier, Function<BlockPos, Task> getTargetTask, Function<Vec3d, Optional<BlockPos>> getClosestBlock, Predicate<BlockPos> isValid, Block... blocks) {
        getOriginPos = getOriginSupplier;
        this.getTargetTask = getTargetTask;
        getClosest = getClosestBlock;
        this.isValid = isValid;
        targetBlocks = blocks;
    }

    /**
     * 构造函数，指定目标任务生成器、最近方块获取器、有效性验证器和目标方块
     * @param getTargetTask 目标任务生成器
     * @param getClosestBlock 最近方块获取器
     * @param isValid 有效性验证器
     * @param blocks 目标方块
     */
    public DoToClosestBlockTask(Function<BlockPos, Task> getTargetTask, Function<Vec3d, Optional<BlockPos>> getClosestBlock, Predicate<BlockPos> isValid, Block... blocks) {
        this(null, getTargetTask, getClosestBlock, isValid, blocks);
    }

    /**
     * 构造函数，指定目标任务生成器、有效性验证器和目标方块
     * @param getTargetTask 目标任务生成器
     * @param isValid 有效性验证器
     * @param blocks 目标方块
     */
    public DoToClosestBlockTask(Function<BlockPos, Task> getTargetTask, Predicate<BlockPos> isValid, Block... blocks) {
        this(null, getTargetTask, null, isValid, blocks);
    }

    /**
     * 构造函数，指定目标任务生成器和目标方块
     * @param getTargetTask 目标任务生成器
     * @param blocks 目标方块
     */
    public DoToClosestBlockTask(Function<BlockPos, Task> getTargetTask, Block... blocks) {
        this(getTargetTask, null, blockPos -> true, blocks);
    }

    @Override
    protected Vec3d getPos(AltoClef mod, BlockPos obj) {
        // 将方块位置转换为向量
        return WorldHelper.toVec3d(obj);
    }

    @Override
    protected Optional<BlockPos> getClosestTo(AltoClef mod, Vec3d pos) {
        if (getClosest != null) {
            // 使用自定义的获取最近方块函数
            return getClosest.apply(pos);
        }
        // 使用默认方法获取最近的方块
        return mod.getBlockScanner().getNearestBlock(pos, isValid, targetBlocks);
    }

    @Override
    protected Vec3d getOriginPos(AltoClef mod) {
        if (getOriginPos != null) {
            // 使用自定义的起始位置获取器
            return getOriginPos.get();
        }
        // 使用玩家当前位置作为起始位置
        return mod.getPlayer().getPos();
    }

    @Override
    protected Task getGoalTask(BlockPos obj) {
        // 根据方块位置生成目标任务
        return getTargetTask.apply(obj);
    }

    @Override
    protected boolean isValid(AltoClef mod, BlockPos obj) {
        // 假设我们是有效的，因为我们处于同一区块
        if (!mod.getChunkTracker().isChunkLoaded(obj)) return true;
        // 检查有效性谓词
        if (isValid != null && !isValid.test(obj)) return false;
        // 检查是否为正确的目标方块
        return mod.getBlockScanner().isBlockAtPosition(obj, targetBlocks);
    }

    @Override
    protected void onStart() {
        // 任务开始时不需要特殊处理
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特殊处理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof DoToClosestBlockTask task) {
            // 比较目标方块数组是否相同
            return Arrays.equals(task.targetBlocks, targetBlocks);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "对最近的方块执行操作...";
    }
}