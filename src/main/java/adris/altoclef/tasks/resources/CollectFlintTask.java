package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 收集燧石任务
 * 用于收集燧石，通过挖掘沙砾获得，或放置沙砾后挖掘获得
 */
public class CollectFlintTask extends ResourceTask {
    private static final float CLOSE_ENOUGH_FLINT = 10; // 距离足够近的沙砾阈值

    private final int _count; // 目标燧石数量

    public CollectFlintTask(int targetCount) {
        super(Items.FLINT, targetCount);
        _count = targetCount;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {

        // 我们可能只是想挖掘最近的沙砾
        Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock(mod.getPlayer().getPos(), validGravel -> WorldHelper.fallingBlockSafeToBreak(validGravel) && WorldHelper.canBreak(validGravel), Blocks.GRAVEL);
        if (closest.isPresent() && closest.get().isWithinDistance(mod.getPlayer().getPos(), CLOSE_ENOUGH_FLINT)) {
            return new DoToClosestBlockTask(DestroyBlockTask::new, Blocks.GRAVEL);
        }

        // 如果我们有沙砾，放置它
        if (mod.getItemStorage().hasItem(Items.GRAVEL)) {
            // 放置沙砾
            return new PlaceBlockNearbyTask(Blocks.GRAVEL);
        }

        // 我们没有沙砾，需要寻找燧石。获取一些！
        return TaskCatalogue.getItemTask(Items.GRAVEL, 1);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CollectFlintTask task) {
            return task._count == _count;
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个燧石";
    }


}
