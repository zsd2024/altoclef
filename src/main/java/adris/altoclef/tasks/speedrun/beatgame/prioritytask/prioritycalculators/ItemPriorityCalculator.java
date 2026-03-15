package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

/**
 * 物品优先级计算器 - 抽象类，定义了基于物品数量计算优先级的方法
 */
public abstract class ItemPriorityCalculator {

    public final int minCount; // 最小数量
    public final int maxCount; // 最大数量

    protected boolean minCountSatisfied = false; // 是否满足最小数量
    protected boolean maxCountSatisfied = false; // 是否满足最大数量

    public ItemPriorityCalculator(int minCount, int maxCount) {
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    /**
     * 获取优先级
     * @param count 当前数量
     * @return 优先级值
     */
    public final double getPriority(int count) {
        if (count > minCount) {
            minCountSatisfied = true;
        }
        if (count > maxCount) {
            maxCountSatisfied = true;
        }

        if (minCountSatisfied) {
            // 如果已满足最小数量，取最小数量和当前数量的最大值
            count = Math.max(minCount, count);
        }

        if (maxCountSatisfied) return Double.NEGATIVE_INFINITY; // 如果满足最大数量，返回负无穷

        return calculatePriority(count); // 计算并返回优先级
    }

    /**
     * 抽象方法：计算优先级
     * @param count 当前数量
     * @return 优先级值
     */
    abstract double calculatePriority(int count);


}
