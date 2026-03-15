package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.util.math.BlockPos;

/**
 * 前往XZ坐标任务 - 前往指定的XZ坐标位置
 */
public class GetToXZTask extends CustomBaritoneGoalTask {

    private final int x, z; // 目标X和Z坐标
    private final Dimension dimension; // 目标维度

    public GetToXZTask(int x, int z) {
        this(x, z, null);
    }

    public GetToXZTask(int x, int z, Dimension dimension) {
        this.x = x;
        this.z = z;
        this.dimension = dimension;
    }

    @Override
    protected Task onTick() {
        // 如果指定了维度且当前不在该维度，则前往指定维度
        if (dimension != null && WorldHelper.getCurrentDimension() != dimension) {
            return new DefaultGoToDimensionTask(dimension);
        }
        return super.onTick();
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        // 创建XZ坐标目标
        return new GoalXZ(x, z);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToXZTask task) {
            return task.x == x && task.z == z && task.dimension == dimension;
        }
        return false;
    }

    @Override
    public boolean isFinished() {
        BlockPos cur = AltoClef.getInstance().getPlayer().getBlockPos();
        // 当玩家到达目标XZ坐标且在正确维度时完成
        return (cur.getX() == x && cur.getZ() == z && (dimension == null || dimension == WorldHelper.getCurrentDimension()));
    }

    @Override
    protected String toDebugString() {
        return "前往 (" + x + "," + z + ")" + (dimension != null ? " 在维度 " + dimension : "");
    }
}
