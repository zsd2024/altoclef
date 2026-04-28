package adris.altoclef.util.baritone;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * 跟随实体目标工具类
 * 用于让机器人跟随指定的实体，当到达实体附近指定距离时视为达成目标
 */
public class GoalFollowEntity implements Goal {

    /**
     * 需要跟随的实体
     */
    private final Entity entity;
    
    /**
     * 判定为"足够接近"的距离阈值
     */
    private final double closeEnoughDistance;

    /**
     * 构造函数
     * 
     * @param entity 需要跟随的实体
     * @param closeEnoughDistance 判定为"足够接近"的距离阈值
     */
    public GoalFollowEntity(Entity entity, double closeEnoughDistance) {
        this.entity = entity;
        this.closeEnoughDistance = closeEnoughDistance;
    }

    /**
     * 检查指定坐标是否在目标范围内
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @return 如果坐标在目标范围内返回true，否则返回false
     */
    @Override
    public boolean isInGoal(int x, int y, int z) {
        BlockPos p = new BlockPos(x, y, z);
        return entity.getBlockPos().equals(p) || p.isWithinDistance(entity.getPos(), closeEnoughDistance);
    }

    /**
     * 计算从指定坐标到目标的启发式距离（用于路径规划）
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @return 启发式距离值
     */
    @Override
    public double heuristic(int x, int y, int z) {
        //synchronized (BaritoneHelper.MINECRAFT_LOCK) {
        double xDiff = x - entity.getPos().getX();
        int yDiff = y - entity.getBlockPos().getY();
        double zDiff = z - entity.getPos().getZ();
        return GoalBlock.calculate(xDiff, yDiff, zDiff);
        //}
    }
}
