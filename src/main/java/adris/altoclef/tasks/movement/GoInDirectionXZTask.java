package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalDirectionXZ;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.Vec3d;

/**
 * 按XZ方向前进任务 - 沿指定方向前进
 */
public class GoInDirectionXZTask extends CustomBaritoneGoalTask {

    private final Vec3d _origin; // 起始点
    private final Vec3d _delta; // 方向向量
    private final double _sidePenalty; // 侧向惩罚值

    public GoInDirectionXZTask(Vec3d origin, Vec3d delta, double sidePenalty) {
        _origin = origin;
        _delta = delta;
        _sidePenalty = sidePenalty;
    }

    /**
     * 检查两个向量是否足够接近
     * @param a 向量a
     * @param b 向量b
     * @return 是否足够接近
     */
    private static boolean closeEnough(Vec3d a, Vec3d b) {
        return a.squaredDistanceTo(b) < 0.001;
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        try {
            // 创建XZ方向目标
            return new GoalDirectionXZ(_origin, _delta, _sidePenalty);
        } catch (Exception e) {
            Debug.logMessage("无效的XZ方向目标（可能是零距离）");
            return null;
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GoInDirectionXZTask) {
            GoInDirectionXZTask task = (GoInDirectionXZTask) other;
            return (closeEnough(task._origin, _origin) && closeEnough(task._delta, _delta));
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "按方向前进: <" + _origin.x + "," + _origin.z + "> 方向: <" + _delta.x + "," + _delta.z + ">";
    }
}
