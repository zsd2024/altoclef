package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalYLevel;

/**
 * 前往Y坐标任务 - 前往指定的Y坐标位置
 */
public class GetToYTask extends CustomBaritoneGoalTask {

    private final int _yLevel; // 目标Y坐标
    private final Dimension _dimension; // 目标维度

    public GetToYTask(int ylevel, Dimension dimension) {
        _yLevel = ylevel;
        _dimension = dimension;
    }

    public GetToYTask(int ylevel) {
        this(ylevel, null);
    }

    @Override
    protected Task onTick() {
        // 如果指定了维度且当前不在该维度，则前往指定维度
        if (_dimension != null && WorldHelper.getCurrentDimension() != _dimension) {
            return new DefaultGoToDimensionTask(_dimension);
        }
        return super.onTick();
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        // 创建Y轴层级目标
        return new GoalYLevel(_yLevel);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToYTask task) {
            return task._yLevel == _yLevel;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "前往 y=" + _yLevel + (_dimension != null ? ("在维度" + _dimension) : "");
    }
}
