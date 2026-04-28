package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 将物品存入任意容器中，如果找不到容器则会放置一个箱子。
 */
public class StoreInAnyContainerTask extends Task {

    private static final Block[] TO_SCAN = Stream.concat(Arrays.stream(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}), Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.SHULKER_BOXES))).toArray(Block[]::new);
    private final ItemTarget[] _toStore; // 要存储的物品目标数组
    private final boolean _getIfNotPresent; // 如果背包中没有物品，是否获取
    private final HashSet<BlockPos> _dungeonChests = new HashSet<>(); // 地牢箱子位置集合
    private final HashSet<BlockPos> _nonDungeonChests = new HashSet<>(); // 非地牢箱子位置集合
    private final MovementProgressChecker _progressChecker = new MovementProgressChecker(); // 移动进度检查器
    private final ContainerStoredTracker _storedItems = new ContainerStoredTracker(slot -> true); // 已存储物品跟踪器
    private BlockPos _currentChestTry = null; // 当前尝试的箱子位置

    public StoreInAnyContainerTask(boolean getIfNotPresent, ItemTarget... toStore) {
        _getIfNotPresent = getIfNotPresent;
        _toStore = toStore;
    }

    @Override
    protected void onStart() {
        // 开始跟踪已存储的物品
        _storedItems.startTracking();
        // 清空地牢和非地牢箱子缓存
        _dungeonChests.clear();
        _nonDungeonChests.clear();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果背包中没有且"_getIfNotPresent"为true，则获取更多物品。
        if (_getIfNotPresent) {
            for (ItemTarget target : _toStore) {
                int inventoryNeed = target.getTargetCount() - _storedItems.getStoredCount(target.getMatches());
                if (inventoryNeed > mod.getItemStorage().getItemCount(target)) {
                    return TaskCatalogue.getItemTask(new ItemTarget(target, inventoryNeed));
                }
            }
        }

        // 获取尚未存储的物品目标
        ItemTarget[] notStored = _storedItems.getUnstoredItemTargetsYouCanStore(mod, _toStore);

        // 验证容器是否有效的谓词
        Predicate<BlockPos> validContainer = containerPos -> {

            // 如果是箱子且上方方块无法破坏，则无法打开此箱子。
            boolean isChest = WorldHelper.isChest(containerPos);
            if (isChest && WorldHelper.isSolidBlock(containerPos.up()) && !WorldHelper.canBreak(containerPos.up()))
                return false;

            //if (!_acceptableContainer.test(containerPos))
            //    return false;

            // 检查容器是否已满
            Optional<ContainerCache> data = mod.getItemStorage().getContainerAtPosition(containerPos);

            if (data.isPresent() && data.get().isFull()) return false;

            // 如果是箱子且设置为避免搜索地牢箱子
            if (isChest && mod.getModSettings().shouldAvoidSearchingForDungeonChests()) {
                boolean cachedDungeon = _dungeonChests.contains(containerPos) && !_nonDungeonChests.contains(containerPos);
                if (cachedDungeon) {
                    return false;
                }
                // 检查刷怪笼（地牢标识）
                int range = 6;
                for (int dx = -range; dx <= range; ++dx) {
                    for (int dz = -range; dz <= range; ++dz) {
                        BlockPos offset = containerPos.add(dx,0,dz);
                        if (mod.getWorld().getBlockState(offset).getBlock() == Blocks.SPAWNER) {
                            _dungeonChests.add(containerPos);
                            return false;
                        }
                    }
                }
                _nonDungeonChests.add(containerPos);
            }
            return true;
        };

        // 如果找到有效容器
        if (mod.getBlockScanner().anyFound(validContainer, TO_SCAN)) {

            setDebugState("前往容器并存放物品");

            // 检查移动进度，如果失败则标记容器为不可达
            if (!_progressChecker.check(mod) && _currentChestTry != null) {
                Debug.logMessage("无法打开容器。建议该容器可能不可达。");
                mod.getBlockScanner().requestBlockUnreachable(_currentChestTry, 2);
                _currentChestTry = null;
                _progressChecker.reset();
            }

            return new DoToClosestBlockTask(
                    blockPos -> {
                        if (_currentChestTry != blockPos) {
                            _progressChecker.reset();
                        }
                        _currentChestTry = blockPos;
                        return new StoreInContainerTask(blockPos, _getIfNotPresent, notStored);
                    },
                    validContainer,
                    TO_SCAN);
        }

        _progressChecker.reset();
        // 制作并在附近放置箱子
        for (Block couldPlace : TO_SCAN) {
            if (mod.getItemStorage().hasItem(couldPlace.asItem())) {
                setDebugState("在附近放置容器");
                return new PlaceBlockNearbyTask(canPlace -> {
                    // 对于箱子，上方必须是空气或可破坏的方块。
                    if (WorldHelper.isChest(couldPlace)) {
                        return WorldHelper.isAir(canPlace.up()) || WorldHelper.canBreak(canPlace.up());
                    }
                    return true;
                }, couldPlace);
            }
        }
        setDebugState("获取箱子物品（默认）");
        return TaskCatalogue.getItemTask(Items.CHEST, 1);
    }

    @Override
    public boolean isFinished() {
        // 所有物品都已存储完成
        return _storedItems.getUnstoredItemTargetsYouCanStore(AltoClef.getInstance(), _toStore).length == 0;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 停止跟踪已存储的物品
        _storedItems.stopTracking();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof StoreInAnyContainerTask task) {
            return task._getIfNotPresent == _getIfNotPresent && Arrays.equals(task._toStore, _toStore);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "在任意容器中存储: " + Arrays.toString(_toStore);
    }
}
