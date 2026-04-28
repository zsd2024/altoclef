package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.BotBehaviour;
import adris.altoclef.tasks.movement.CustomBaritoneGoalTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

/**
 * 龙息跟踪器
 * 用于检测和跟踪末影龙喷吐的龙息效果云，以便机器人避开危险区域
 */
public class DragonBreathTracker {
    /** 存储龙息效果云覆盖的方块位置集合 */
    private final HashSet<BlockPos> breathBlocks = new HashSet<>();

    /**
     * 更新龙息效果云的位置信息
     * @param mod AltoClef主实例
     */
    public void updateBreath(AltoClef mod) {
        breathBlocks.clear();
        for (AreaEffectCloudEntity cloud : mod.getEntityTracker().getTrackedEntities(AreaEffectCloudEntity.class)) {
            for (BlockPos bad : WorldHelper.getBlocksTouchingBox(cloud.getBoundingBox())) {
                breathBlocks.add(bad);
            }
        }
    }

    /**
     * 检查指定位置是否接触到龙息效果云
     * @param pos 要检查的方块位置
     * @return 如果位置接触到龙息则返回true，否则返回false
     */
    public boolean isTouchingDragonBreath(BlockPos pos) {
        return breathBlocks.contains(pos);
    }

    /**
     * 获取逃离龙息的任务
     * @return 逃离龙息的任务实例
     */
    public Task getRunAwayTask() {
        return new RunAwayFromDragonsBreathTask();
    }

    /**
     * 逃离龙息效果云的内部任务类
     */
    private class RunAwayFromDragonsBreathTask extends CustomBaritoneGoalTask {

        @Override
        protected void onStart() {
            super.onStart();
            BotBehaviour botBehaviour = AltoClef.getInstance().getBehaviour();

            botBehaviour.push();
            botBehaviour.setBlockPlacePenalty(Double.POSITIVE_INFINITY);
            // 绝对不要随意游荡
            checker = new MovementProgressChecker((int) Float.POSITIVE_INFINITY);
        }

        @Override
        protected void onStop(Task interruptTask) {
            super.onStop(interruptTask);
            AltoClef.getInstance().getBehaviour().pop();
        }

        @Override
        protected Goal newGoal(AltoClef mod) {
            return new GoalRunAway(10, breathBlocks.toArray(BlockPos[]::new));
        }

        @Override
        protected boolean isEqual(Task other) {
            return other instanceof RunAwayFromDragonsBreathTask;
        }

        @Override
        protected String toDebugString() {
            return "逃离龙息";
        }
    }
}
