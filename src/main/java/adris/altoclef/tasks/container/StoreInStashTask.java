package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.BlockRange;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 在藏匿点存储物品的任务
 * 该任务负责在指定范围内将物品存入箱子、潜影盒等存储容器中
 */
public class StoreInStashTask extends Task {

    // 这里有很多代码重复...
    // 需要扫描的存储方块类型：箱子、陷阱箱、木桶、潜影盒
    private static final Block[] TO_SCAN = Stream.concat(Arrays.stream(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}), Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.SHULKER_BOXES))).toArray(Block[]::new);
    // 要存储的物品目标
    private final ItemTarget[] _toStore;
    // 如果不存在是否获取物品
    private final boolean _getIfNotPresent;
    // 藏匿点范围
    private final BlockRange _stashRange;
    // 存储物品追踪器
    private ContainerStoredTracker _storedItems;

    /**
     * 构造函数
     * @param getIfNotPresent 如果没有物品是否获取
     * @param stashRange 藏匿点范围
     * @param toStore 要存储的物品目标
     */
    public StoreInStashTask(boolean getIfNotPresent, BlockRange stashRange, ItemTarget... toStore) {
        _getIfNotPresent = getIfNotPresent;
        _stashRange = stashRange;
        _toStore = toStore;
    }

    @Override
    protected void onStart() {
        if (_storedItems == null) {
            _storedItems = new ContainerStoredTracker(slot -> {
                Optional<BlockPos> currentContainer = AltoClef.getInstance().getItemStorage().getLastBlockPosInteraction();
                return currentContainer.isPresent() && _stashRange.contains(currentContainer.get());
            });
        }
        _storedItems.startTracking();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果没有物品且"不存在时获取"为真，则获取更多物品
        if (_getIfNotPresent) {
            for (ItemTarget target : _toStore) {
                int inventoryNeed = target.getTargetCount() - _storedItems.getStoredCount(target.getMatches());
                if (inventoryNeed > mod.getItemStorage().getItemCount(target)) {
                    return TaskCatalogue.getItemTask(new ItemTarget(target, inventoryNeed));
                }
            }
        }

        // 定义有效容器的判断条件
        Predicate<BlockPos> validContainer = blockPos -> {
            if (!_stashRange.contains(blockPos))
                return false;
            Optional<ContainerCache> container = mod.getItemStorage().getContainerAtPosition(blockPos);
            // 我们没有打开过这个容器或它已打开但未满
            return container.isEmpty() || !container.get().isFull();
        };

        // 在有效容器中存储
        if (mod.getBlockScanner().anyFound(validContainer, TO_SCAN)) {
            setDebugState("在最近的藏匿容器中存储");
            return new DoToClosestBlockTask(
                    (BlockPos bpos) -> new StoreInContainerTask(bpos, false, _storedItems.getUnstoredItemTargetsYouCanStore(mod, _toStore)),
                    validContainer,
                    TO_SCAN
            );
        }

        setDebugState("前往藏匿点（在藏匿范围内未找到未满的容器）");
        BlockPos centerStash = _stashRange.getCenter();
        return new GetToXZTask(centerStash.getX(), centerStash.getZ());
    }

    @Override
    protected void onStop(Task interruptTask) {
        _storedItems.stopTracking();
    }

    @Override
    public boolean isFinished() {
        return _storedItems != null && _storedItems.getUnstoredItemTargetsYouCanStore(AltoClef.getInstance(), _toStore).length == 0;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof StoreInStashTask task) {
            return task._stashRange.equals(_stashRange) && task._getIfNotPresent == _getIfNotPresent && Arrays.equals(task._toStore, _toStore);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "在藏匿点存储" + _stashRange + ": " + Arrays.toString(_toStore);
    }
}
