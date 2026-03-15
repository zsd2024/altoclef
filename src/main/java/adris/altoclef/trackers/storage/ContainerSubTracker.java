package adris.altoclef.trackers.storage;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockInteractEvent;
import adris.altoclef.eventbus.events.ScreenOpenEvent;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.trackers.Tracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;

/**
 * 容器子跟踪器 - 跟踪容器中的物品
 */
public class ContainerSubTracker extends Tracker {

    private final HashMap<Dimension, HashMap<BlockPos, ContainerCache>> containerCaches = new HashMap<>(); // 容器缓存
    private boolean containerOpen; // 容器是否打开
    private BlockPos lastBlockPosInteraction; // 最后方块位置交互
    private Block lastBlockInteraction; // 最后方块交互
    private ContainerCache enderChestCache; // 末影箱缓存
    private boolean hasSentError; // 是否已发送错误

    public ContainerSubTracker(TrackerManager manager) {
        super(manager);
        for (Dimension dimension : Dimension.values()) {
            containerCaches.put(dimension, new HashMap<>());
        }

        // 监听与方块交互时的事件
        EventBus.subscribe(BlockInteractEvent.class, evt -> {
            BlockPos blockPos = evt.hitResult.getBlockPos();
            BlockState bs = mod.getWorld().getBlockState(blockPos);
            onBlockInteract(blockPos, bs.getBlock());
        });
        EventBus.subscribe(ScreenOpenEvent.class, evt -> {
            if (evt.preOpen) {
                onScreenOpenFirstTick(evt.screen);
            } else {
                if (evt.screen == null)
                    onScreenClose();
            }
        });
    }

    /**
     * 方块交互时的回调
     * @param pos 方块位置
     * @param block 方块
     */
    private void onBlockInteract(BlockPos pos, Block block) {
        if (block instanceof AbstractFurnaceBlock ||
                block instanceof ChestBlock ||
                block.equals(Blocks.ENDER_CHEST) ||
                block instanceof HopperBlock ||
                block instanceof ShulkerBoxBlock ||
                block instanceof DispenserBlock ||
                block instanceof BarrelBlock) {
            lastBlockPosInteraction = pos;
            lastBlockInteraction = block;
        }
    }

    /**
     * 屏幕打开时的第一刻回调
     * @param screen 屏幕
     */
    private void onScreenOpenFirstTick(final Screen screen) {
        containerOpen = screen instanceof FurnaceScreen
                || screen instanceof GenericContainerScreen
                || screen instanceof SmokerScreen
                || screen instanceof BlastFurnaceScreen
                || screen instanceof HopperScreen
                || screen instanceof ShulkerBoxScreen;
    }

    /**
     * 屏幕关闭时的回调
     */
    private void onScreenClose() {
        containerOpen = false;
        lastBlockPosInteraction = null;
        lastBlockInteraction = null;
        hasSentError = false;
    }

    /**
     * 服务器刻度时执行
     */
    public void onServerTick() {
        if (MinecraftClient.getInstance().player == null)
            return;
        // 如果我们没有注册与方块的交互，尝试当前"注视"的方块
        if (containerOpen && lastBlockPosInteraction == null && lastBlockInteraction == null) {
            if (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhit) {
                Debug.logWarning("屏幕打开但未检测到方块交互，使用我们当前注视的方块。");
                lastBlockPosInteraction = bhit.getBlockPos();
                lastBlockInteraction = mod.getWorld().getBlockState(lastBlockPosInteraction).getBlock();
            }
        }
        if (containerOpen && lastBlockPosInteraction != null && lastBlockInteraction != null) {
            BlockPos containerPos = lastBlockPosInteraction;
            ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
            if (handler == null)
                return;

            HashMap<BlockPos, ContainerCache> dimCache = containerCaches.get(WorldHelper.getCurrentDimension());

            // 容器类型不匹配，重置。
            if (dimCache.containsKey(containerPos)) {
                ContainerType currentType = dimCache.get(containerPos).getContainerType();
                if (!ContainerType.screenHandlerMatches(currentType, handler)) {
                    if (!hasSentError) {
                        Debug.logMessage("在 " + containerPos.toShortString() + " 处的容器屏幕不匹配，将覆盖容器数据: " + handler.getType() + " ?=> " + currentType);
                        hasSentError = true;
                    }
                    dimCache.remove(containerPos);
                }
            }

            // 发现新容器
            if (!dimCache.containsKey(containerPos)) {
                Block containerBlock = lastBlockInteraction;
                ContainerType interactType = ContainerType.getFromBlock(containerBlock);
                ContainerCache newCache = new ContainerCache(WorldHelper.getCurrentDimension(), containerPos, interactType);
                dimCache.put(containerPos, newCache);
                // 特殊末影箱缓存
                if (interactType == ContainerType.ENDER_CHEST) {
                    enderChestCache = newCache;
                }
            }

            ContainerCache toUpdate = dimCache.get(containerPos);
            toUpdate.update(handler, stack -> {

            });
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    /**
     * 检查容器缓存是否有效
     * @param dimension 维度
     * @param cache 容器缓存
     * @return 是否有效
     */
    private boolean isContainerCacheValid(Dimension dimension, ContainerCache cache) {
        BlockPos pos = cache.getBlockPos();
        if (WorldHelper.getCurrentDimension() == dimension && mod.getChunkTracker().isChunkLoaded(pos)) {
            ContainerType actualType = ContainerType.getFromBlock(mod.getWorld().getBlockState(pos).getBlock());
            if (actualType == ContainerType.EMPTY) {
                return false;
            }
            return actualType == cache.getContainerType();
        }
        return true;
    }

    /**
     * 获取指定维度和位置的容器缓存
     * @param dimension 维度
     * @param pos 位置
     * @return 容器缓存
     */
    public Optional<ContainerCache> getContainerAtPosition(Dimension dimension, BlockPos pos) {
        Optional<ContainerCache> cache = Optional.ofNullable(containerCaches.get(dimension).getOrDefault(pos, null));
        if (cache.isPresent() && !isContainerCacheValid(dimension, cache.get())) {
            containerCaches.get(dimension).remove(pos);
            return Optional.empty();
        }
        return cache;
    }

    /**
     * 获取指定位置的容器缓存
     * @param pos 位置
     * @return 容器缓存
     */
    public Optional<ContainerCache> getContainerAtPosition(BlockPos pos) {
        return getContainerAtPosition(WorldHelper.getCurrentDimension(), pos);
    }

    /**
     * 获取末影箱存储缓存
     * @return 末影箱存储缓存
     */
    public Optional<ContainerCache> getEnderChestStorage() {
        return Optional.ofNullable(enderChestCache);
    }

    /**
     * 获取满足条件的已缓存容器列表
     * @param accept 接受条件
     * @return 容器缓存列表
     */
    public List<ContainerCache> getCachedContainers(Predicate<ContainerCache> accept) {
        List<ContainerCache> result = new ArrayList<>();
        List<Pair<Dimension, BlockPos>> toRemove = new ArrayList<>();
        for (Dimension dim : containerCaches.keySet()) {
            HashMap<BlockPos, ContainerCache> map = containerCaches.get(dim);
            for (ContainerCache cache : map.values()) {
                if (!isContainerCacheValid(dim, cache)) {
                    toRemove.add(new Pair<>(dim, cache.getBlockPos()));
                    continue;
                }
                if (accept.test(cache))
                    result.add(cache);
            }
        }
        for (Pair<Dimension, BlockPos> remove : toRemove) {
            containerCaches.get(remove.getLeft()).remove(remove.getRight());
        }
        return result;
    }

    /**
     * 获取指定类型的已缓存容器列表
     * @param types 容器类型数组
     * @return 容器缓存列表
     */
    public List<ContainerCache> getCachedContainers(ContainerType... types) {
        Set<ContainerType> typeSet = new HashSet<>(Arrays.asList(types));
        return getCachedContainers(cache -> typeSet.contains(cache.getContainerType()));
    }

    /**
     * 获取距离指定位置最近的满足条件的容器缓存
     * @param pos 位置
     * @param accept 接受条件
     * @return 最近的容器缓存
     */
    public Optional<ContainerCache> getClosestTo(Vec3d pos, Predicate<ContainerCache> accept) {
        double bestDist = Double.POSITIVE_INFINITY;
        Dimension dim = WorldHelper.getCurrentDimension();

        List<BlockPos> toRemove = new ArrayList<>();

        ContainerCache bestCache = null;
        for (ContainerCache cache : containerCaches.get(dim).values()) {
            if (!isContainerCacheValid(dim, cache)) {
                toRemove.add(cache.getBlockPos());
                continue;
            }
            double dist = BlockPosVer.getSquaredDistance(cache.getBlockPos(),pos);
            if (dist < bestDist) {
                if (accept.test(cache)) {
                    bestDist = dist;
                    bestCache = cache;
                }
            }
        }
        // 清除任何无效项
        for (BlockPos remove : toRemove) {
            containerCaches.get(dim).remove(remove);
        }
        return Optional.ofNullable(bestCache);
    }

    /**
     * 获取距离指定位置最近的指定类型容器缓存
     * @param pos 位置
     * @param types 容器类型数组
     * @return 最近的容器缓存
     */
    public Optional<ContainerCache> getClosestTo(Vec3d pos, ContainerType... types) {
        Set<ContainerType> typeSet = new HashSet<>(Arrays.asList(types));
        return getClosestTo(pos, cache -> typeSet.contains(cache.getContainerType()));
    }

    /**
     * 获取包含指定物品的容器列表
     * @param items 物品数组
     * @return 容器缓存列表
     */
    public List<ContainerCache> getContainersWithItem(Item... items) {
        return getCachedContainers(cache -> cache.hasItem(items));
    }

    /**
     * 获取距离指定位置最近的包含指定物品的容器缓存
     * @param pos 位置
     * @param items 物品数组
     * @return 最近的容器缓存
     */
    public Optional<ContainerCache> getClosestWithItem(Vec3d pos, Item... items) {
        return getClosestTo(pos, cache -> cache.hasItem(items));
    }

    /**
     * 检查是否有所需物品，满足指定条件
     * @param accept 接受条件
     * @param items 物品数组
     * @return 是否有所需物品
     */
    public boolean hasItem(Predicate<ContainerCache> accept, Item... items) {
        for (HashMap<BlockPos, ContainerCache> map : containerCaches.values()) {
            for (ContainerCache cache : map.values()) {
                if (cache.hasItem(items) && accept.test(cache))
                    return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有所需物品
     * @param items 物品数组
     * @return 是否有所需物品
     */
    public boolean hasItem(Item... items) {
        return hasItem(cache -> true, items);
    }

    /**
     * 获取最后方块位置交互
     * @return 最后方块位置交互
     */
    public BlockPos getLastBlockPosInteraction() {
        return lastBlockPosInteraction;
    }

    @Override
    protected void updateState() {
        // umm lol
    }

    @Override
    protected void reset() {
        for (Dimension key : containerCaches.keySet()) {
            containerCaches.get(key).clear();
        }
    }

}
