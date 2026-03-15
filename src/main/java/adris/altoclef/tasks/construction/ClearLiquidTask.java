package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

/**
 * 清除指定位置的液体源方块
 */
public class ClearLiquidTask extends Task {

    // 液体位置
    private final BlockPos _liquidPos;

    public ClearLiquidTask(BlockPos liquidPos) {
        this._liquidPos = liquidPos;
    }

    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        // 如果有桶，则使用桶来清除液体
        if (AltoClef.getInstance().getItemStorage().hasItem(Items.BUCKET)) {
            AltoClef.getInstance().getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);
            return new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), _liquidPos, false);
        }

        // 如果没有桶，则使用结构方块任务来放置方块阻挡液体
        return new PlaceStructureBlockTask(_liquidPos);
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    public boolean isFinished() {
        // 检查区块是否已加载，如果已加载则检查液体是否已清除
        if (AltoClef.getInstance().getChunkTracker().isChunkLoaded(_liquidPos)) {
            return AltoClef.getInstance().getWorld().getBlockState(_liquidPos).getFluidState().isEmpty();
        }
        // 如果区块未加载则返回false
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ClearLiquidTask task) {
            // 比较液体位置是否相同
            return task._liquidPos.equals(_liquidPos);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "清理 " + _liquidPos + " 处的液体";
    }
}
