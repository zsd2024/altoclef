package adris.altoclef.util.baritone;

import baritone.api.pathing.goals.Goal;

import java.util.Arrays;

/**
 * 目标与组合工具类
 * 实现Baritone的Goal接口，表示多个目标的逻辑与组合
 * 只有当所有子目标都满足时，整个组合目标才算满足
 */
public class GoalAnd implements Goal {
    /** 子目标数组 */
    private final Goal[] goals;

    /**
     * 构造目标与组合对象
     * 
     * @param goals 子目标数组
     */
    public GoalAnd(Goal... goals) {
        this.goals = goals;
    }

    /**
     * 检查指定坐标是否满足所有子目标
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @return 如果所有子目标都满足，则返回true；否则返回false
     */
    public boolean isInGoal(int x, int y, int z) {
        Goal[] var4 = this.goals;
        int var5 = var4.length;

        for (Goal goal : var4) {
            if (!goal.isInGoal(x, y, z)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 计算到指定坐标的启发式距离（所有子目标距离之和）
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @return 启发式距离总和
     */
    public double heuristic(int x, int y, int z) {
        double sum = 0;
        if (this.goals != null) {
            for (Goal goal : this.goals) {
                sum += goal.heuristic(x, y, z);
            }
        }
        return sum;
        /*double min = 1.7976931348623157E308D;
        Goal[] var6 = this.goals;
        int var7 = var6.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            Goal g = var6[var8];
            min = Math.min(min, g.heuristic(x, y, z));
        }

        return min;
         */
    }

    @Override
    public String toString() {
        return "GoalAnd" + Arrays.toString(this.goals);
    }

    /**
     * 获取子目标数组
     * 
     * @return 子目标数组
     */
    public Goal[] goals() {
        return this.goals;
    }
}
