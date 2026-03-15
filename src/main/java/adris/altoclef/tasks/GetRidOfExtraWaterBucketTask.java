package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.resources.CollectBucketLiquidTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Items;

/**
 * 当机器人有太多水桶但你不想丢弃它们时使用
 */
public class GetRidOfExtraWaterBucketTask extends Task {

    // 是否需要拾取
    private boolean needsPickup = false;

    @Override
    protected void onStart() {
        // 任务开始时不需要特殊处理
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果有水桶且不需要拾取，则将水倒掉
        if (mod.getItemStorage().getItemCount(Items.WATER_BUCKET) != 0 && !needsPickup) {
            return new InteractWithBlockTask(new ItemTarget(Items.WATER_BUCKET, 1),mod.getPlayer().getBlockPos().down(), false);
        }

        // 标记为需要拾取
        needsPickup = true;
        if (mod.getItemStorage().getItemCount(Items.WATER_BUCKET) < 1) {
            // 收集一个水桶
            return new CollectBucketLiquidTask.CollectWaterBucketTask(1);
        }

        return null;
    }

    @Override
    public boolean isFinished() {
        // 当只有一个水桶且需要拾取时完成任务
        return AltoClef.getInstance().getItemStorage().getItemCount(Items.WATER_BUCKET) == 1 && needsPickup;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特殊处理
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof GetRidOfExtraWaterBucketTask;
    }

    @Override
    protected String toDebugString() {
        return "处理多余水桶任务";
    }
}
