package adris.altoclef.util.baritone;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.BaritoneHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.pathing.goals.GoalYLevel;
import net.minecraft.entity.Entity;

import java.util.List;

/**
 * 远离实体目标工具类（抽象基类）
 * 用于让机器人远离指定的一组实体，当与所有实体的距离都大于指定阈值时视为达成目标
 */
public abstract class GoalRunAwayFromEntities implements Goal {

    /**
     * AltoClef主模块实例
     */
    private final AltoClef mod;
    
    /**
     * 安全距离阈值，当与实体的距离大于此值时认为安全
     */
    private final double distance;
    
    /**
     * 是否只考虑XZ平面的距离（忽略Y轴高度）
     */
    private final boolean xzOnly;

    /**
     * 惩罚因子，用于调整路径规划时对靠近实体的惩罚程度
     * 值越高：会更直接地远离每个实体，但可能会拒绝更快的替代路径而选择直线挖掘
     * 值过低：可能会直接冲向实体以穿过它
     */
    private final double penaltyFactor;

    /**
     * 构造函数
     * 
     * @param mod AltoClef主模块实例
     * @param distance 安全距离阈值
     * @param xzOnly 是否只考虑XZ平面的距离
     * @param penaltyFactor 惩罚因子
     */
    public GoalRunAwayFromEntities(AltoClef mod, double distance, boolean xzOnly, double penaltyFactor) {
        this.mod = mod;
        this.distance = distance;
        this.xzOnly = xzOnly;
        this.penaltyFactor = penaltyFactor;
    }

    /**
     * 检查指定坐标是否在目标范围内（即是否远离所有实体）
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @return 如果坐标远离所有实体返回true，否则返回false
     */
    @Override
    public boolean isInGoal(int x, int y, int z) {
        List<Entity> entities = getEntities(mod);
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    if (entity == null || !entity.isAlive()) continue;
                    double sqDistance;
                    if (xzOnly) {
                        sqDistance = entity.getPos().subtract(x, y, z).multiply(1, 0, 1).lengthSquared();
                    } else {
                        sqDistance = entity.squaredDistanceTo(x, y, z);
                    }
                    if (sqDistance < distance * distance) return false;
                }
            }
        }
        return true;
    }

    /**
     * 计算从指定坐标到目标的启发式成本（用于路径规划）
     * 成本越低越好，表示该位置更适合避开实体
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @return 启发式成本值
     */
    @Override
    public double heuristic(int x, int y, int z) {
        // 成本越低越好
        double costSum = 0;
        List<Entity> entities = getEntities(mod);
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            int max = 10; // 如果有100个玩家，这将永远不会计算完。
            int counter = 0;
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    counter++;
                    if (entity == null || !entity.isAlive()) continue;
                    double cost = getCostOfEntity(entity, x, y, z);
                    if (cost != 0) {
                        // 我们希望更近的实体比更远的实体具有更大的权重。
                        costSum += 1 / cost;
                    } else {
                        // 不好的情况 >:(
                        costSum += 1000;
                    }
                    if (counter >= max) break;
                }
            }
            if (counter > 0) {
                costSum /= counter;
            }
            return costSum * penaltyFactor;
        }
    }

    /**
     * 获取需要避开的实体列表（由子类实现）
     * 
     * @param mod AltoClef主模块实例
     * @return 需要避开的实体列表
     */
    protected abstract List<Entity> getEntities(AltoClef mod);

    /**
     * 计算指定实体在给定坐标的成本（虚拟方法，可被子类重写）
     * 
     * @param entity 实体
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @return 成本值
     */
    protected double getCostOfEntity(Entity entity, int x, int y, int z) {
        double heuristic = 0;
        if (!xzOnly) {
            heuristic += GoalYLevel.calculate(entity.getBlockPos().getY(), y);
        }
        heuristic += GoalXZ.calculate(entity.getBlockPos().getX() - x, entity.getBlockPos().getZ() - z);
        return heuristic; //entity.squaredDistanceTo(x, y, z);
    }
}
