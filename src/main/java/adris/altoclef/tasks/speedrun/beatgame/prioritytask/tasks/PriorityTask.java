package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;

import java.util.function.Function;

/**
 * 优先级任务抽象类 - 定义了优先级任务的基本结构
 */
public abstract class PriorityTask {

    private final Function<AltoClef, Boolean> canCall; // 是否可以调用
    private final boolean shouldForce; // 是否应该强制执行
    private final boolean canCache; // 是否可以缓存

    // 如果我们确定要在被调用后3秒内结束此任务，可以使用此参数
    public final boolean bypassForceCooldown;

    public PriorityTask(Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        this.canCall = canCall;
        this.shouldForce = shouldForce;
        this.canCache = canCache;
        this.bypassForceCooldown = bypassForceCooldown;
    }

    /**
     * 计算优先级
     * @param mod AltoClef实例
     * @return 优先级值
     */
    public final double calculatePriority(AltoClef mod) {
        if (!canCall.apply(mod)) return Double.NEGATIVE_INFINITY;

        return getPriority(mod);
    }

    @Override
    public String toString() {
        return getDebugString();
    }

    /**
     * 获取任务
     * @param mod AltoClef实例
     * @return 任务实例
     */
    public abstract Task getTask(AltoClef mod);

    /**
     * 获取调试字符串
     * @return 调试字符串
     */
    public abstract String getDebugString();

    // 也许还需要传入距离？
    protected abstract double getPriority(AltoClef mod);

    /**
     * 是否在开始时需要制作
     * @param mod AltoClef实例
     * @return 是否需要
     */
    public boolean needCraftingOnStart(AltoClef mod) {
        return false;
    }

    /**
     * 是否应该强制执行
     * @return 是否应该强制
     */
    public boolean shouldForce() {
        return shouldForce;
    }

    /**
     * 是否可以缓存
     * @return 是否可以缓存
     */
    public boolean canCache() {
        return canCache;
    }
}
