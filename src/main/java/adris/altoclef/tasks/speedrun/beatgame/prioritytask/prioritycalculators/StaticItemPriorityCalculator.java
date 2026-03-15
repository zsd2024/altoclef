package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

/**
 * 静态物品优先级计算器 - 为物品提供固定优先级值的计算器
 */
public class StaticItemPriorityCalculator extends ItemPriorityCalculator{

    /**
     * 创建静态优先级计算器
     * @param priority 优先级值
     * @return 静态物品优先级计算器实例
     */
    public static StaticItemPriorityCalculator of(int priority) {
        return new StaticItemPriorityCalculator(priority,1,1);
    }

    private final int priority; // 优先级值

    public StaticItemPriorityCalculator(int priority, int minCount, int maxCount) {
        super(minCount, maxCount);
        this.priority = priority;
    }

    @Override
    double calculatePriority(int count) {
        // 返回固定优先级值
        return priority;
    }
}
