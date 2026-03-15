package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import adris.altoclef.util.helpers.BaritoneHelper;
import baritone.api.pathing.goals.Goal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 远离敌对生物任务 - 远离敌对生物指定距离
 */
public class RunAwayFromHostilesTask extends CustomBaritoneGoalTask {

    private final double distanceToRun; // 需要跑开的距离
    private final boolean includeSkeletons; // 是否包括骷髅

    public RunAwayFromHostilesTask(double distance, boolean includeSkeletons) {
        distanceToRun = distance;
        this.includeSkeletons = includeSkeletons;
    }

    public RunAwayFromHostilesTask(double distance) {
        this(distance, false);
    }


    @Override
    protected Goal newGoal(AltoClef mod) {
        // 我们现在就要逃跑
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        return new GoalRunAwayFromHostiles(mod, distanceToRun);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof RunAwayFromHostilesTask task) {
            return Math.abs(task.distanceToRun - distanceToRun) < 1;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "远离敌对生物! 距离="+ distanceToRun +", 包括骷髅="+ includeSkeletons;
    }

    /**
     * 远离敌对生物目标类
     */
    private class GoalRunAwayFromHostiles extends GoalRunAwayFromEntities {

        public GoalRunAwayFromHostiles(AltoClef mod, double distance) {
            super(mod, distance, false, 0.8);
        }

        @Override
        protected List<Entity> getEntities(AltoClef mod) {
            Stream<LivingEntity> stream = mod.getEntityTracker().getHostiles().stream();
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                if (!includeSkeletons) {
                    // 过滤掉骷髅实体
                    stream = stream.filter(hostile -> !(hostile instanceof SkeletonEntity));
                }
                return stream.collect(Collectors.toList());
            }
        }
    }
}
