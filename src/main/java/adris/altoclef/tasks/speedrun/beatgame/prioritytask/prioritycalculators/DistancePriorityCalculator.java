package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

/**
 * 距离优先级计算器 - 基于距离计算优先级的抽象类
 */
public abstract class DistancePriorityCalculator {

    public final int minCount; // 最小数量
    public final int maxCount; // 最大数量

    protected boolean minCountSatisfied = false; // 是否满足最小数量
    protected boolean maxCountSatisfied = false; // 是否满足最大数量

    public DistancePriorityCalculator(int minCount, int maxCount) {
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    /**
     * 更新当前数量状态
     * @param count 当前数量
     */
    public void update(int count) {
        if (count >= minCount) {
            minCountSatisfied = true;
        }
        if (count >= maxCount) {
            maxCountSatisfied = true;
        }
    }


    /**
     * 获取优先级
     * @param distance 距离
     * @return 优先级值
     */
    public double getPriority(double distance) {
        if (Double.isInfinite(distance) || distance == Integer.MAX_VALUE || maxCountSatisfied) return Double.NEGATIVE_INFINITY;

        return calculatePriority(distance);
    }

    /**
     * 计算优先级
     * @param distance 距离
     * @return 优先级值
     */
    abstract double calculatePriority(double distance);

}
