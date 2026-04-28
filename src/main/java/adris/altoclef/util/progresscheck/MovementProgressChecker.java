package adris.altoclef.util.progresscheck;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * 移动进度检查器
 * 综合检查玩家移动和方块破坏进度的检查器。
 * 包含距离检查器（检查玩家位置变化）和挖掘检查器（检查方块破坏进度）。
 */
public class MovementProgressChecker {

    /** 距离检查器，用于检查玩家位置是否发生变化 */
    private final IProgressChecker<Vec3d> distanceChecker;
    /** 挖掘进度检查器，用于检查方块破坏进度 */
    private final IProgressChecker<Double> mineChecker;

    /** 上次正在破坏的方块位置 */
    private BlockPos lastBreakingBlock = null;

    /**
     * 构造移动进度检查器
     * @param distanceTimeout 距离检查超时时间（秒）
     * @param minDistance 最小移动距离阈值
     * @param mineTimeout 挖掘检查超时时间（秒）
     * @param minMineProgress 最小挖掘进度阈值
     * @param attempts 允许的重试次数
     */
    public MovementProgressChecker(double distanceTimeout, double minDistance, double mineTimeout, double minMineProgress, int attempts) {
        distanceChecker = new ProgressCheckerRetry<>(new DistanceProgressChecker(distanceTimeout, minDistance), attempts);
        mineChecker = new LinearProgressChecker(mineTimeout, minMineProgress);
    }

    /**
     * 构造移动进度检查器（默认重试次数为1）
     */
    public MovementProgressChecker(double distanceTimeout, double minDistance, double mineTimeout, double minMineProgress) {
        this(distanceTimeout, minDistance, mineTimeout, minMineProgress, 1);
    }

    /**
     * 构造移动进度检查器（使用默认参数）
     * @param attempts 允许的重试次数
     */
    public MovementProgressChecker(int attempts) {
        this(6, 0.1, 0.5, 0.001, attempts);
    }

    /**
     * 默认构造函数（允许重试1次）
     */
    public MovementProgressChecker() {
        this(1);
    }

    /**
     * 检查当前进度是否正常
     * @param mod AltoClef主模块实例
     * @return 如果进度正常返回true，否则返回false
     */
    public boolean check(AltoClef mod) {

        // 允许在进食时暂停进度检查
        if (mod.getFoodChain().needsToEat()) {
            distanceChecker.reset();
            mineChecker.reset();
        }

        if (mod.getControllerExtras().isBreakingBlock()) {
            BlockPos breakBlock = mod.getControllerExtras().getBreakingBlockPos();
            // 如果我们破坏了一个方块，说明取得了进展。
            // 我们必须延迟重置距离检查器，直到我们真正破坏一个方块。
            // 因为否则如果我们不断重试挖掘但没有成功，可能会导致无法正确检测失败。
            if (lastBreakingBlock != null && WorldHelper.isAir(lastBreakingBlock)) {
                distanceChecker.reset();
                mineChecker.reset();
            }
            lastBreakingBlock = breakBlock;
            mineChecker.setProgress(mod.getControllerExtras().getBreakingBlockProgress());
            return !mineChecker.failed();
        } else {
            mineChecker.reset();
            distanceChecker.setProgress(mod.getPlayer().getPos());
            return !distanceChecker.failed();
        }
    }

    /**
     * 重置所有内部检查器的状态
     */
    public void reset() {
        distanceChecker.reset();
        mineChecker.reset();
    }

}
