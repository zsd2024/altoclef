package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 抽象存储容器任务类
 * 用于打开存储容器并在其中执行任意操作
 */
public abstract class AbstractDoToStorageContainerTask extends Task {

    private ContainerType currentContainerType = null;

    @Override
    protected void onStart() {

    }

    @Override
    protected Task onTick() {
        Optional<BlockPos> containerTarget = getContainerTarget();

        AltoClef mod = AltoClef.getInstance();
        // 未找到容器
        if (containerTarget.isEmpty()) {
            setDebugState("漫游中");
            currentContainerType = null;
            return onSearchWander();
        }

        BlockPos targetPos = containerTarget.get();

        // 容器已打开
        if (currentContainerType != null && ContainerType.screenHandlerMatches(currentContainerType)) {

            Optional<ContainerCache> cache = mod.getItemStorage().getContainerAtPosition(targetPos);
            if (cache.isPresent()) {
                return onContainerOpenSubtask(mod, cache.get());
            }
        }

        // 移动到容器位置
        if (mod.getChunkTracker().isChunkLoaded(targetPos)) {
            Block type = mod.getWorld().getBlockState(targetPos).getBlock();
            currentContainerType = ContainerType.getFromBlock(type);
        }
        if (WorldHelper.isChest(targetPos) && WorldHelper.isSolidBlock(targetPos.up()) && WorldHelper.canBreak(targetPos.up())) {
            setDebugState("清理箱子上方的方块");
            return new DestroyBlockTask(targetPos.up());
        }
        setDebugState("打开容器: " + targetPos.toShortString());
        return new InteractWithBlockTask(targetPos);
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    /**
     * 获取目标容器的位置
     * @return 容器位置的Optional对象
     */
    protected abstract Optional<BlockPos> getContainerTarget();

    /**
     * 容器打开后的子任务处理
     * @param mod AltoClef实例
     * @param containerCache 容器缓存
     * @return 要执行的子任务
     */
    protected abstract Task onContainerOpenSubtask(AltoClef mod, ContainerCache containerCache);

    // 虚方法
    // TODO: 接口化此方法
    /**
     * 搜索时的漫游任务
     * @return 漫游任务
     */
    protected Task onSearchWander() {
        return new TimeoutWanderTask();
    }
}
