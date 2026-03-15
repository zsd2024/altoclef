package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.util.math.BlockPos;

/**
 * 进入方块范围任务 - 前往指定方块并在其指定范围内
 */
public class GetWithinRangeOfBlockTask extends CustomBaritoneGoalTask {

    public final BlockPos blockPos; // 方块位置
    public final int range; // 范围

    public GetWithinRangeOfBlockTask(BlockPos blockPos, int range) {
        this.blockPos = blockPos;
        this.range = range;
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        // 创建范围目标
        return new GoalNear(blockPos, range);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetWithinRangeOfBlockTask task) {
            return task.blockPos.equals(blockPos) && task.range == range;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "进入 " + range + " 个方块内 " + blockPos.toShortString();
    }
}
