package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.BlockPos;

/**
 * 接近方块任务 - 尽可能接近指定方块
 * 使用baritone可以轻松接近方块，但我们有一个问题：baritone需要目标可用。
 * <p>
 * 例如，假设我们想接近熔岩池的中心。
 * 这是不可能的，所以假设我们期望机器人尽可能接近。
 * 我们必须指定"半径"，并且这个半径必须在池子外面，
 * 否则baritone会卡住，甚至不会尝试接近。
 */
public class GetCloseToBlockTask extends Task {

    private final BlockPos _toApproach; // 要接近的方块位置
    private int _currentRange; // 当前范围

    public GetCloseToBlockTask(BlockPos toApproach) {
        _toApproach = toApproach;
    }

    @Override
    protected void onStart() {
        _currentRange = Integer.MAX_VALUE;
    }

    @Override
    protected Task onTick() {
        // 如果我们达到了范围，总是将范围减小。
        // 我们有一个严格递减的范围，这意味着我们最终会
        // 尽可能接近。
        if (inRange()) {
            _currentRange = getCurrentDistance() - 1;
        }
        return new GetWithinRangeOfBlockTask(_toApproach, _currentRange);
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    private int getCurrentDistance() {
        return (int) Math.sqrt(AltoClef.getInstance().getPlayer().getBlockPos().getSquaredDistance(_toApproach));
    }

    private boolean inRange() {
        // 检查玩家是否在当前范围内
        return AltoClef.getInstance().getPlayer().getBlockPos().getSquaredDistance(_toApproach) <= _currentRange * _currentRange;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetCloseToBlockTask task) {
            return task._toApproach.equals(_toApproach);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "接近 " + _toApproach.toShortString();
    }


}
