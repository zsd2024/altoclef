package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalChunk;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.ChunkPos;

/**
 * 前往区块任务 - 前往指定的区块位置
 */
public class GetToChunkTask extends CustomBaritoneGoalTask {

    private final ChunkPos _pos; // 目标区块位置

    public GetToChunkTask(ChunkPos pos) {
        // 重写检查器以更加宽松，因为我们要在这里遍历整个区块。
        checker = new MovementProgressChecker();
        _pos = pos;
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        return new GoalChunk(_pos); // 创建区块目标
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToChunkTask task) {
            return task._pos.equals(_pos);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "前往区块: " + _pos.toString();
    }
}
