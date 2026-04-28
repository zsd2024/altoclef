package adris.altoclef.util.baritone;

import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * 方块侧面目标工具类
 * 用于定义一个目标，该目标位于指定方块的特定侧面方向上
 */
public class GoalBlockSide implements Goal {

    /** 目标方块的位置 */
    private final BlockPos block;
    /** 目标所在的侧面方向 */
    private final Direction direction;
    /** 缓冲距离，用于确定目标区域的大小 */
    private final double buffer;

    /**
     * 构造函数
     * @param block 目标方块的位置
     * @param direction 目标所在的侧面方向
     * @param bufferDistance 缓冲距离
     */
    public GoalBlockSide(BlockPos block, Direction direction, double bufferDistance) {
        this.block = block;
        this.direction = direction;
        this.buffer = bufferDistance;
    }

    /**
     * 构造函数（使用默认缓冲距离为1）
     * @param block 目标方块的位置
     * @param direction 目标所在的侧面方向
     */
    public GoalBlockSide(BlockPos block, Direction direction) {
        this(block, direction, 1);
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        // 检查是否在正确的一侧
        return getDistanceInRightDirection(x, y, z) > 0;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        // 计算启发式距离（距离目标有多远）
        return Math.min(getDistanceInRightDirection(x, y, z), 0);
    }

    /**
     * 计算在正确方向上的距离
     * @param x 当前X坐标
     * @param y 当前Y坐标
     * @param z 当前Z坐标
     * @return 在指定方向上的距离减去缓冲距离
     */
    private double getDistanceInRightDirection(int x, int y, int z) {
        Vec3d delta = new Vec3d(x, y, z).subtract(block.getX(), block.getY(), block.getZ());
        Vec3i dir = direction.getVector();
        double dot = new Vec3d(dir.getX(), dir.getY(), dir.getZ()).dotProduct(delta);
        // 我们假设dir是已归一化的
        double distCorrect = dot;
        return distCorrect - this.buffer;
    }
}
