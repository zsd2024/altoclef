package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * 给定一个带有火焰的方块位置，熄灭该位置的火焰
 */
public class PutOutFireTask extends Task {

    // 要熄灭的火焰位置
    private final BlockPos _firePosition;

    public PutOutFireTask(BlockPos firePosition) {
        _firePosition = firePosition;
    }

    @Override
    protected void onStart() {
        // 任务开始时无需特殊处理
    }

    @Override
    protected Task onTick() {
        // 使用空手左键点击火焰方块来熄灭火焰
        return new InteractWithBlockTask(ItemTarget.EMPTY, null, _firePosition, Input.CLICK_LEFT, false, false);
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时无需特殊处理
    }

    @Override
    public boolean isFinished() {
        // 检查目标位置是否还有火焰或灵魂火
        BlockState s = AltoClef.getInstance().getWorld().getBlockState(_firePosition);
        return (s.getBlock() != Blocks.FIRE && s.getBlock() != Blocks.SOUL_FIRE);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof PutOutFireTask task) {
            // 比较两个任务的目标火焰位置是否相同
            return (task._firePosition.equals(_firePosition));
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "正在熄灭 " + _firePosition + " 处的火焰";
    }
}
