package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalDodgeProjectiles;
import baritone.api.pathing.goals.Goal;

/**
 * 躲避投射物任务
 * 此任务使玩家自动躲避飞行中的投射物（如箭矢）
 */
public class DodgeProjectilesTask extends CustomBaritoneGoalTask {

    // 水平躲避距离
    private final double _distanceHorizontal;
    // 垂直躲避距离
    private final double _distanceVertical;

    /**
     * 构造函数
     * @param distanceHorizontal 水平躲避距离
     * @param distanceVertical 垂直躲避距离
     */
    public DodgeProjectilesTask(double distanceHorizontal, double distanceVertical) {
        _distanceHorizontal = distanceHorizontal;
        _distanceVertical = distanceVertical;
    }

    @Override
    protected Task onTick() {
        if (cachedGoal != null) {
            // EntityTracker会自动运行ensureUpdated，它会调用updateState并锁定互斥锁，
            // 因此这里不要锁定。
            // 多线程在这方面似乎会带来麻烦。
            GoalDodgeProjectiles goal = (GoalDodgeProjectiles) cachedGoal;
        }
        return super.onTick();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof DodgeProjectilesTask task) {
            //if (task._mob.getPos().squaredDistanceTo(_mob.getPos()) > 0.5) return false;
            if (Math.abs(task._distanceHorizontal - _distanceHorizontal) > 1) return false;
            if (Math.abs(task._distanceVertical - _distanceVertical) > 1) return false;
            return true;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "在 " + _distanceHorizontal + " 格距离处躲避箭矢";
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        return new GoalDodgeProjectiles(mod, _distanceHorizontal, _distanceVertical);
    }
}
