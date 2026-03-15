package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.BlockPos;

/**
 * 到达指定方块的任务类
 * 此任务用于移动到指定的方块位置
 */
public class GetToBlockTask extends CustomBaritoneGoalTask implements ITaskRequiresGrounded {

    // 目标位置
    private final BlockPos _position;
    // 是否优先使用楼梯
    private final boolean _preferStairs;
    // 目标维度
    private final Dimension _dimension;
    // 完成的tick数
    private int finishedTicks = 0;
    // 漫游计时器
    private final TimerGame wanderTimer = new TimerGame(2);

    /**
     * 构造函数
     * @param position 目标位置
     * @param preferStairs 是否优先使用楼梯
     */
    public GetToBlockTask(BlockPos position, boolean preferStairs) {
        this(position, preferStairs, null);
    }

    /**
     * 构造函数
     * @param position 目标位置
     * @param dimension 目标维度
     */
    public GetToBlockTask(BlockPos position, Dimension dimension) {
        this(position, false, dimension);
    }

    /**
     * 构造函数
     * @param position 目标位置
     * @param preferStairs 是否优先使用楼梯
     * @param dimension 目标维度
     */
    public GetToBlockTask(BlockPos position, boolean preferStairs, Dimension dimension) {
        _dimension = dimension;
        _position = position;
        _preferStairs = preferStairs;
    }

    /**
     * 构造函数
     * @param position 目标位置
     */
    public GetToBlockTask(BlockPos position) {
        this(position, false);
    }

    @Override
    protected Task onTick() {
        if (_dimension != null && WorldHelper.getCurrentDimension() != _dimension) {
            return new DefaultGoToDimensionTask(_dimension);
        }

        if (isFinished()) {
            finishedTicks++;
        } else {
            finishedTicks = 0;
        }
        if (finishedTicks > 10*20) {
            wanderTimer.reset();
            Debug.logWarning("GetToBlock在10秒内已完成但仍在被调用，开始漫游");
            finishedTicks = 0;
            return new TimeoutWanderTask();
        }
        if (!wanderTimer.elapsed()) {
            return new TimeoutWanderTask();
        }

        return super.onTick();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 如果优先使用楼梯，设置行为配置
        if (_preferStairs) {
            AltoClef.getInstance().getBehaviour().push();
            AltoClef.getInstance().getBehaviour().setPreferredStairs(true);
        }
    }


    @Override
    protected void onStop(Task interruptTask) {
        super.onStop(interruptTask);
        // 如果优先使用楼梯，恢复行为配置
        if (_preferStairs) {
            AltoClef.getInstance().getBehaviour().pop();
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToBlockTask task) {
            return task._position.equals(_position) && task._preferStairs == _preferStairs && task._dimension == _dimension;
        }
        return false;
    }

    @Override
    public boolean isFinished() {
        return super.isFinished() && (_dimension == null || _dimension == WorldHelper.getCurrentDimension());
    }

    @Override
    protected String toDebugString() {
        return "到达方块 " + _position + (_dimension != null ? " 在维度 " + _dimension : "");
    }


    @Override
    protected Goal newGoal(AltoClef mod) {
        return new GoalBlock(_position);
    }

    @Override
    protected void onWander(AltoClef mod) {
        super.onWander(mod);
        mod.getBlockScanner().requestBlockUnreachable(_position);
    }
}
