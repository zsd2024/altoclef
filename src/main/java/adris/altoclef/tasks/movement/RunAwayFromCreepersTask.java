package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.MobDefenseChain;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import baritone.api.pathing.goals.Goal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * 远离苦力怕任务 - 远离苦力怕指定距离
 */
public class RunAwayFromCreepersTask extends CustomBaritoneGoalTask {

    private final double _distanceToRun; // 需要跑开的距离

    public RunAwayFromCreepersTask(double distance) {
        _distanceToRun = distance;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof RunAwayFromCreepersTask task) {
            //if (task._mob.getPos().squaredDistanceTo(_mob.getPos()) > 0.5) return false;
            if (Math.abs(task._distanceToRun - _distanceToRun) > 1) return false;
            return true;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "远离苦力怕 " + _distanceToRun + " 个方块";
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        // 我们现在就要逃跑
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        return new GoalRunAwayFromCreepers(mod, _distanceToRun);
    }

    /**
     * 远离苦力怕目标类
     */
    private static class GoalRunAwayFromCreepers extends GoalRunAwayFromEntities {

        public GoalRunAwayFromCreepers(AltoClef mod, double distance) {
            super(mod, distance, false, 10);
        }

        @Override
        protected List<Entity> getEntities(AltoClef mod) {
            // 获取所有被追踪的苦力怕实体列表
            return new ArrayList<>(mod.getEntityTracker().getTrackedEntities(CreeperEntity.class));
        }

        @Override
        protected double getCostOfEntity(Entity entity, int x, int y, int z) {
            if (entity instanceof CreeperEntity) {
                // 获取苦力怕安全系数
                return MobDefenseChain.getCreeperSafety(new Vec3d(x + 0.5, y + 0.5, z + 0.5), (CreeperEntity) entity);
            }
            return super.getCostOfEntity(entity, x, y, z);
        }
    }
}
