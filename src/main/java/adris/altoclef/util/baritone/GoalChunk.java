package adris.altoclef.util.baritone;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.util.math.ChunkPos;

/**
 * 区块目标工具类
 * 用于定义一个目标，该目标位于指定的Minecraft区块内
 */
public class GoalChunk implements Goal {

    /** 目标区块的位置 */
    private final ChunkPos pos;

    /**
     * 构造函数
     * @param pos 目标区块的位置
     */
    public GoalChunk(ChunkPos pos) {
        this.pos = pos;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        // 检查坐标是否在区块范围内
        return pos.getStartX() <= x && x <= pos.getEndX() &&
                pos.getStartZ() <= z && z <= pos.getEndZ();
    }

    @Override
    public double heuristic(int x, int y, int z) {
        // 计算到区块中心的启发式距离
        double cx = (pos.getStartX() + pos.getEndX()) / 2.0, cz = (pos.getStartZ() + pos.getEndZ()) / 2.0;
        return GoalXZ.calculate(cx - x, cz - z);
    }
}
