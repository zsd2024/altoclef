package adris.altoclef.util.baritone;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.Vec3d;

/**
 * XZ方向目标工具类
 * 用于定义一个沿特定XZ平面方向移动的目标
 */
public class GoalDirectionXZ implements Goal {
    /** 起始点的X坐标 */
    private final double originx;
    /** 起始点的Z坐标 */
    private final double originz;
    /** 方向向量的X分量 */
    private final double dirx;
    /** 方向向量的Z分量 */
    private final double dirz;

    /** 侧向惩罚系数，用于路径规划 */
    private final double sidePenalty;

    /**
     * 构造函数
     * @param origin 起始位置
     * @param offset 方向偏移量
     * @param sidePenalty 侧向惩罚系数
     */
    public GoalDirectionXZ(Vec3d origin, Vec3d offset, double sidePenalty) {
        this.originx = origin.getX();
        //this.y = origin.getY();
        this.originz = origin.getZ();
        offset = offset.multiply(1, 0, 1);
        offset = offset.normalize();
        this.dirx = offset.x;
        this.dirz = offset.z;
        if (this.dirx == 0 && this.dirz == 0) {
            throw new IllegalArgumentException(offset + "");
        }
        this.sidePenalty = sidePenalty;
    }

    /**
     * 根据设置决定是否屏蔽坐标值
     * @param value 要处理的坐标值
     * @return 屏蔽后的字符串或原始值的字符串表示
     */
    private static String maybeCensor(double value) {
        return Baritone.settings().censorCoordinates.value ? "<censored>" : Double.toString(value);
    }

    public boolean isInGoal(int x, int y, int z) {
        // 方向目标永远不被视为到达目标
        return false;
    }

    public double heuristic(int x, int y, int z) {
        double dx = (x - this.originx),
                dz = (z - this.originz);
        double correctDistance = dx * this.dirx + dz * this.dirz;
        double px = dirx * correctDistance,
                pz = dirz * correctDistance;
        double perpendicularDistance = ((dx - px) * (dx - px)) + ((dz - pz) * (dz - pz));

        return -correctDistance * BaritoneAPI.getSettings().costHeuristic.value
                + perpendicularDistance * sidePenalty;
    }

    public String toString() {
        return String.format("GoalDirection{x=%s, z=%s, dx=%s, dz=%s}", maybeCensor(this.originx), maybeCensor(this.originz), maybeCensor(this.dirx), maybeCensor(this.dirz));
    }
}
