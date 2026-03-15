package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Optional;

/**
 * 当你想前往可能变化的目标位置时使用此任务。
 * <p>
 * https://www.notion.so/Closest-threshold-ing-system-utility-c3816b880402494ba9209c9f9b62b8bf
 */
public abstract class AbstractDoToClosestObjectTask<T> extends Task {

    // 启发式缓存映射
    private final HashMap<T, CachedHeuristic> heuristicMap = new HashMap<>();
    // 当前追踪的对象
    private T currentlyPursuing = null;
    // 是否曾经徘徊
    private boolean wasWandering;
    // 目标任务
    private Task goalTask = null;

    /**
     * 获取对象的位置
     * @param mod AltoClef实例
     * @param obj 对象
     * @return 返回对象的位置
     */
    protected abstract Vec3d getPos(AltoClef mod, T obj);

    /**
     * 获取距离指定位置最近的对象
     * @param mod AltoClef实例
     * @param pos 位置
     * @return 返回最近的对象
     */
    protected abstract Optional<T> getClosestTo(AltoClef mod, Vec3d pos);

    /**
     * 获取起始位置
     * @param mod AltoClef实例
     * @return 返回起始位置
     */
    protected abstract Vec3d getOriginPos(AltoClef mod);

    /**
     * 获取目标任务
     * @param obj 对象
     * @return 返回目标任务
     */
    protected abstract Task getGoalTask(T obj);

    /**
     * 检查对象是否有效
     * @param mod AltoClef实例
     * @param obj 对象
     * @return 如果对象有效返回true
     */
    protected abstract boolean isValid(AltoClef mod, T obj);

    // 虚方法
    /**
     * 获取徘徊任务
     * @param mod AltoClef实例
     * @return 返回徘徊任务
     */
    protected Task getWanderTask(AltoClef mod) {
        return new TimeoutWanderTask(true);
    }

    /**
     * 重置搜索
     */
    public void resetSearch() {
        currentlyPursuing = null;
        heuristicMap.clear();
        goalTask = null;
    }

    /**
     * 检查是否曾经徘徊
     * @return 如果曾经徘徊返回true
     */
    public boolean wasWandering() {
        return wasWandering;
    }

    /**
     * 获取当前计算的启发值
     * @param mod AltoClef实例
     * @return 返回当前计算的启发值
     */
    private double getCurrentCalculatedHeuristic(AltoClef mod) {
        Optional<Double> ticksRemainingOp = mod.getClientBaritone().getPathingBehavior().ticksRemainingInSegment();
        return ticksRemainingOp.orElse(Double.POSITIVE_INFINITY);
    }

    @Override
    protected Task onTick() {
        wasWandering = false;
        AltoClef mod = AltoClef.getInstance();

        // 如果当前追踪的对象不再可追踪，重置追踪。
        if (currentlyPursuing != null && !isValid(mod, currentlyPursuing)) {
            // 这可能是个好主意，对吧？
            heuristicMap.remove(currentlyPursuing);
            currentlyPursuing = null;
        }

        // 获取最近的对象
        Optional<T> checkNewClosest = getClosestTo(mod, getOriginPos(mod));

        // 接收最近的对象和位置
        if (checkNewClosest.isPresent() && !checkNewClosest.get().equals(currentlyPursuing)) {
            T newClosest = checkNewClosest.get();
            // 不同的最近对象
            if (currentlyPursuing == null) {
                // 我们没有最近的对象
                currentlyPursuing = newClosest;
            } else {
                if (goalTask != null /*isMovingToClosestPos(mod)*/) {
                    setDebugState("向最近对象移动...");
                    double currentHeuristic = getCurrentCalculatedHeuristic(mod);
                    double closestDistanceSqr = getPos(mod, currentlyPursuing).squaredDistanceTo(mod.getPlayer().getPos());
                    int lastTick = WorldHelper.getTicks();

                    if (!heuristicMap.containsKey(currentlyPursuing)) {
                        heuristicMap.put(currentlyPursuing, new CachedHeuristic());
                    }
                    CachedHeuristic h = heuristicMap.get(currentlyPursuing);
                    h.updateHeuristic(currentHeuristic);
                    h.updateDistance(closestDistanceSqr);
                    h.setTickAttempted(lastTick);
                    if (heuristicMap.containsKey(newClosest)) {
                        // 我们的新对象有过去计算的启发值，如果更好则尝试它。
                        CachedHeuristic maybeReAttempt = heuristicMap.get(newClosest);
                        double maybeClosestDistance = getPos(mod, newClosest).squaredDistanceTo(mod.getPlayer().getPos());
                        // 显著地更接近（距离除以2）
                        if (maybeReAttempt.getHeuristicValue() < h.getHeuristicValue() || maybeClosestDistance < maybeReAttempt.getClosestDistanceSqr() / 4) {
                            setDebugState("重试旧启发式！");
                            // 当前最近的先前计算启发式更好，向它移动！
                            currentlyPursuing = newClosest;
                            // 理论上，下一行不需要运行，
                            // 但由于某些原因这对使其工作至关重要
                            maybeReAttempt.updateDistance(maybeClosestDistance);
                        }
                    } else {
                        setDebugState("尝试新追踪");
                        // 我们的新对象没有启发式，尝试一下！
                        currentlyPursuing = newClosest;
                    }
                } else {
                    setDebugState("等待移动任务启动...");
                    // 我们应该继续向对象移动，直到获得新信息。
                }
            }
        }

        if (currentlyPursuing != null) {
            goalTask = getGoalTask(currentlyPursuing);
            return goalTask;
        } else {
            goalTask = null;
        }


        if (checkNewClosest.isEmpty()) {
            setDebugState("等待计算我想（徘徊）");
            wasWandering = true;
            return getWanderTask(mod);
        }

        setDebugState("等待计算我想（不徘徊）");
        return null;
    }

    /**
     * 缓存启发式类，用于存储和管理启发式计算结果
     */
    private static class CachedHeuristic {

        // 最近距离的平方值
        private double _closestDistanceSqr;
        // 尝试的tick数
        private int _tickAttempted;
        // 启发式值
        private double _heuristicValue;

        /**
         * 构造函数，使用默认值初始化
         */
        public CachedHeuristic() {
            _closestDistanceSqr = Double.POSITIVE_INFINITY;
            _heuristicValue = Double.POSITIVE_INFINITY;
        }

        /**
         * 构造函数，使用指定值初始化
         * @param closestDistanceSqr 最近距离的平方值
         * @param tickAttempted 尝试的tick数
         * @param heuristicValue 启发式值
         */
        public CachedHeuristic(double closestDistanceSqr, int tickAttempted, double heuristicValue) {
            _closestDistanceSqr = closestDistanceSqr;
            _tickAttempted = tickAttempted;
            _heuristicValue = heuristicValue;
        }

        /**
         * 获取启发式值
         * @return 返回启发式值
         */
        public double getHeuristicValue() {
            return _heuristicValue;
        }

        /**
         * 更新启发式值
         * @param heuristicValue 新的启发式值
         */
        public void updateHeuristic(double heuristicValue) {
            _heuristicValue = Math.min(_heuristicValue, heuristicValue);
        }

        /**
         * 获取最近距离的平方值
         * @return 返回最近距离的平方值
         */
        public double getClosestDistanceSqr() {
            return _closestDistanceSqr;
        }

        /**
         * 更新距离值
         * @param closestDistanceSqr 新的距离平方值
         */
        public void updateDistance(double closestDistanceSqr) {
            _closestDistanceSqr = Math.min(_closestDistanceSqr, closestDistanceSqr);
        }

        /**
         * 获取尝试的tick数
         * @return 返回尝试的tick数
         */
        public int getTickAttempted() {
            return _tickAttempted;
        }

        /**
         * 设置尝试的tick数
         * @param tickAttempted 尝试的tick数
         */
        public void setTickAttempted(int tickAttempted) {
            _tickAttempted = tickAttempted;
        }
    }
}
