package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import baritone.api.pathing.goals.Goal;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.function.Supplier;

/**
 * 远离实体任务 - 远离指定实体的抽象任务类
 */
public abstract class RunAwayFromEntitiesTask extends CustomBaritoneGoalTask {

    private final Supplier<List<Entity>> _runAwaySupplier; // 提供需要远离的实体列表的供应器

    private final double _distanceToRun; // 需要跑开的距离
    private final boolean _xz; // 是否仅在XZ平面移动
    // 参见GoalrunAwayFromEntities惩罚值
    private final double _penalty; // 惩罚值

    public RunAwayFromEntitiesTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun, boolean xz, double penalty) {
        _runAwaySupplier = toRunAwayFrom;
        _distanceToRun = distanceToRun;
        _xz = xz;
        _penalty = penalty;
    }

    public RunAwayFromEntitiesTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun, double penalty) {
        this(toRunAwayFrom, distanceToRun, false, penalty);
    }


    @Override
    protected Goal newGoal(AltoClef mod) {
        return new GoalRunAwayStuff(mod, _distanceToRun, _xz);
    }


    /**
     * 远离实体目标类
     */
    private class GoalRunAwayStuff extends GoalRunAwayFromEntities {

        public GoalRunAwayStuff(AltoClef mod, double distance, boolean xz) {
            super(mod, distance, xz, _penalty);
        }

        @Override
        protected List<net.minecraft.entity.Entity> getEntities(AltoClef mod) {
            // 获取需要远离的实体列表
            return _runAwaySupplier.get();
        }
    }
}
