package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChunkLoadEvent;
import adris.altoclef.eventbus.events.ChunkUnloadEvent;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.EmptyChunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 简单区块跟踪器 - 跟踪当前已加载的区块。仅此而已。
 */
public class SimpleChunkTracker {

    private final AltoClef mod;
    private final Set<ChunkPos> loaded = new HashSet<>();

    public SimpleChunkTracker(AltoClef mod) {
        this.mod = mod;

        // 当区块加载时...
        EventBus.subscribe(ChunkLoadEvent.class, evt -> onLoad(evt.chunk.getPos()));
        EventBus.subscribe(ChunkUnloadEvent.class, evt -> onUnload(evt.chunkPos));
    }

    /**
     * 区块加载时的回调函数
     * @param pos 加载的区块位置
     */
    private void onLoad(ChunkPos pos) {
        //Debug.logInternal("LOADED: " + pos);
        loaded.add(pos);
    }

    /**
     * 区块卸载时的回调函数
     * @param pos 卸载的区块位置
     */
    private void onUnload(ChunkPos pos) {
        //Debug.logInternal("unloaded: " + pos);
        loaded.remove(pos);
    }

    /**
     * 检查区块是否已加载
     * @param pos 区块位置
     * @return 是否已加载
     */
    public boolean isChunkLoaded(ChunkPos pos) {
        return !(mod.getWorld().getChunk(pos.x, pos.z) instanceof EmptyChunk);
    }

    /**
     * 检查指定位置的区块是否已加载
     * @param pos 坐标位置
     * @return 是否已加载
     */
    public boolean isChunkLoaded(BlockPos pos) {
        return isChunkLoaded(new ChunkPos(pos));
    }

    /**
     * 获取已加载的区块列表
     * @return 已加载的区块位置列表
     */
    public List<ChunkPos> getLoadedChunks() {
        List<ChunkPos> result = new ArrayList<>(loaded);
        // 只显示已加载的区块。
        result = result.stream()
                .filter(this::isChunkLoaded)
                .distinct()
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 遍历区块中的每个方块（如果区块已加载）。
     * 如果区块未加载，则不扫描任何内容。
     *
     * @param chunk       要扫描的区块位置
     * @param onBlockStop 对每个方块运行，直到返回true时停止扫描。
     * @return `onBlockStop` 是否在任何时刻返回了true。
     */
    public boolean scanChunk(ChunkPos chunk, Predicate<BlockPos> onBlockStop) {
        if (!isChunkLoaded(chunk)) return false;
        int bottomY = mod.getWorld().getBottomY();
        int topY = mod.getWorld().getTopY();

        //Debug.logInternal("SCANNED CHUNK " + chunk.toString());
        for (int xx = chunk.getStartX(); xx <= chunk.getEndX(); ++xx) {
            for (int yy = bottomY; yy <= topY; ++yy) {
                for (int zz = chunk.getStartZ(); zz <= chunk.getEndZ(); ++zz) {
                    if (onBlockStop.test(new BlockPos(xx, yy, zz))) return true;
                }
            }
        }
        return false;
    }

    /**
     * 扫描区块中的每个方块
     * @param chunk 要扫描的区块位置
     * @param onBlock 对每个方块执行的操作
     */
    public void scanChunk(ChunkPos chunk, Consumer<BlockPos> onBlock) {
        scanChunk(chunk, (block) -> {
            onBlock.accept(block);
            return false;
        });
    }

    /**
     * 重置区块跟踪器
     * @param mod AltoClef实例
     */
    public void reset(AltoClef mod) {
        Debug.logInternal("区块已重置");
        loaded.clear();
    }
}
