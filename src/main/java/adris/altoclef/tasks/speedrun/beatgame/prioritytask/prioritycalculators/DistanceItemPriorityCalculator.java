package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

/**
 * 距离物品优先级计算器 - 基于距离计算物品收集优先级
 */
public class DistanceItemPriorityCalculator extends DistancePriorityCalculator {

    private final double multiplier; // 乘数
    private final double unneededMultiplier; // 无需求乘数
    private final double unneededDistanceThreshold; // 无需求距离阈值


    public DistanceItemPriorityCalculator(double multiplier, double unneededMultiplier, double unneededDistanceThreshold, int minCount, int maxCount) {
        super(minCount, maxCount);
        this.multiplier = multiplier;
        this.unneededMultiplier = unneededMultiplier;
        this.unneededDistanceThreshold = unneededDistanceThreshold;
    }

    @Override
    protected double calculatePriority(double distance) {
        // 计算基础优先级（距离的倒数）
        double priority = 1 / distance;

        if (super.minCountSatisfied) {
            // 如果已满足最小数量需求
            if (distance < unneededDistanceThreshold) {
                return priority * unneededMultiplier; // 距离阈值内的优先级
            }
            return Double.NEGATIVE_INFINITY; // 负无穷表示不需要
        }

        return priority * multiplier;
    }

}
