package adris.altoclef.util.helpers;

import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.Vec3d;

/**
 * Baritone帮助器
 * 提供与Baritone路径规划相关的辅助方法和常量
 */
public class BaritoneHelper {

    /**
     * 在Baritone中访问Minecraft数据（来自ClientWorld或ClientPlayerEntity）时使用的锁对象
     */
    public static final Object MINECRAFT_LOCK = new Object();

    /**
     * 计算从起始位置到目标位置的通用启发式距离
     * 
     * @param start 起始位置
     * @param target 目标位置
     * @return 启发式距离值
     */
    public static double calculateGenericHeuristic(Vec3d start, Vec3d target) {
        return calculateGenericHeuristic(start.x, start.y, start.z, target.x, target.y, target.z);
    }

    /**
     * 返回从起始位置到目标位置的Baritone近似启发式距离
     * 
     * @param xStart 起始X坐标
     * @param yStart 起始Y坐标
     * @param zStart 起始Z坐标
     * @param xTarget 目标X坐标
     * @param yTarget 目标Y坐标
     * @param zTarget 目标Z坐标
     * @return 启发式距离值
     */
    public static double calculateGenericHeuristic(double xStart, double yStart, double zStart, double xTarget, double yTarget, double zTarget) {
        double xDiff = xTarget - xStart;
        int yDiff = (int) yTarget - (int) yStart;
        double zDiff = zTarget - zStart;
        return GoalBlock.calculate(xDiff, yDiff < 0 ? yDiff - 1 : yDiff, zDiff);
    }
}
