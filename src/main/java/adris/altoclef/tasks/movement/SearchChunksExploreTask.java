package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.ChunkLoadEvent;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 搜索/探索连续的区块"团块"，尝试加载此"团块"中的所有附近区块
 * <p>
 * 您必须定义一个函数来确定区块是否应包含在此"团块"中。
 * <p>
 * 例如，如果您希望探索整个沙漠，此函数将返回区块是否为沙漠区块。
 */
public class SearchChunksExploreTask extends Task {

    private final Object searcherMutex = new Object(); // 搜索器互斥锁
    private final Set<ChunkPos> alreadyExplored = new HashSet<>(); // 已探索的区块位置集合
    private ChunkSearchTask searcher; // 区块搜索任务
    private Subscription<ChunkLoadEvent> _chunkLoadedSubscription; // 区块加载事件订阅

    // 虚方法
    protected ChunkPos getBestChunkOverride(AltoClef mod, List<ChunkPos> chunks) {
        return null;
    }

    @Override
    protected void onStart() {
        // 监听区块加载
        _chunkLoadedSubscription = EventBus.subscribe(ChunkLoadEvent.class, evt -> onChunkLoad(evt.chunk.getPos()));

        resetSearch();
    }

    @Override
    protected Task onTick() {
        synchronized (searcherMutex) {
            if (searcher == null) {
                setDebugState("探索/搜索有效区块");
                // 探索
                return getWanderTask();
            }

            if (searcher.isActive() && searcher.isFinished()) {
                Debug.logWarning("目标对象搜索失败。");
                alreadyExplored.addAll(searcher.getSearchedChunks());
                searcher = null;
            } else if (searcher.finished()) {
                setDebugState("搜索目标对象...");
                Debug.logMessage("搜索完成。");
                alreadyExplored.addAll(searcher.getSearchedChunks());
                searcher = null;
            }
            //Debug.logMessage("wtf: " + (_searcher == null? "(null)" :_searcher.finished()));
            setDebugState("在区块中搜索...");
            return searcher;
        }
    }

    @Override
    protected void onStop(Task interruptTask) {
        EventBus.unsubscribe(_chunkLoadedSubscription);
    }

    // 当我们找到一个有效区块时，在那里开始搜索。
    private void onChunkLoad(ChunkPos pos) {
        if (searcher != null) return;
        if (!this.isActive()) return;

        if (isChunkWithinSearchSpace(AltoClef.getInstance(), pos)) {
            synchronized (searcherMutex) {
                if (!alreadyExplored.contains(pos)) {
                    Debug.logMessage("新搜寻器: " + pos);
                    searcher = new SearchSubTask(pos);
                }
            }
        }

    }

    /**
     * 获取漫步任务
     * @return 漫步任务
     */
    protected Task getWanderTask() {
        return new TimeoutWanderTask(true);
    }

    /**
     * 检查区块是否在搜索空间内
     * @param mod AltoClef实例
     * @param pos 区块位置
     * @return 是否在搜索空间内
     */
    protected abstract boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos);

    /**
     * 检查搜索是否失败
     * @return 搜索是否失败
     */
    public boolean failedSearch() {
        return searcher == null;
    }

    /**
     * 重置搜索
     */
    public void resetSearch() {
        //Debug.logMessage("Search reset");
        searcher = null;
        alreadyExplored.clear();
        // 我们也想搜索当前加载的区块！！！
        for (ChunkPos start : AltoClef.getInstance().getChunkTracker().getLoadedChunks()) {
            onChunkLoad(start);
        }
    }

    /**
     * 搜索子任务类
     */
    class SearchSubTask extends ChunkSearchTask {

        public SearchSubTask(ChunkPos start) {
            super(start);
        }

        @Override
        protected boolean isChunkPartOfSearchSpace(AltoClef mod, ChunkPos pos) {
            return isChunkWithinSearchSpace(mod, pos);
        }

        @Override
        public ChunkPos getBestChunk(AltoClef mod, List<ChunkPos> chunks) {
            ChunkPos override = getBestChunkOverride(mod, chunks);
            if (override != null) return override;
            return super.getBestChunk(mod, chunks);
        }

        @Override
        protected boolean isChunkSearchEqual(ChunkSearchTask other) {
            // 由于我们正在追踪"_searcher"，我们期望子例程始终一致！
            return other == this;//return other instanceof SearchSubTask;
        }

        @Override
        protected String toDebugString() {
            return "搜索区块...";
        }
    }

}

    @Override
    protected void onStart() {
        // Listen for chunk loading
        _chunkLoadedSubscription = EventBus.subscribe(ChunkLoadEvent.class, evt -> onChunkLoad(evt.chunk.getPos()));

        resetSearch();
    }

    @Override
    protected Task onTick() {
        synchronized (searcherMutex) {
            if (searcher == null) {
                setDebugState("Exploring/Searching for valid chunk");
                // Explore
                return getWanderTask();
            }

            if (searcher.isActive() && searcher.isFinished()) {
                Debug.logWarning("Target object search failed.");
                alreadyExplored.addAll(searcher.getSearchedChunks());
                searcher = null;
            } else if (searcher.finished()) {
                setDebugState("Searching for target object...");
                Debug.logMessage("Search finished.");
                alreadyExplored.addAll(searcher.getSearchedChunks());
                searcher = null;
            }
            //Debug.logMessage("wtf: " + (_searcher == null? "(null)" :_searcher.finished()));
            setDebugState("Searching within chunks...");
            return searcher;
        }
    }

    @Override
    protected void onStop(Task interruptTask) {
        EventBus.unsubscribe(_chunkLoadedSubscription);
    }

    // When we find a valid chunk, start our search there.
    private void onChunkLoad(ChunkPos pos) {
        if (searcher != null) return;
        if (!this.isActive()) return;

        if (isChunkWithinSearchSpace(AltoClef.getInstance(), pos)) {
            synchronized (searcherMutex) {
                if (!alreadyExplored.contains(pos)) {
                    Debug.logMessage("New searcher: " + pos);
                    searcher = new SearchSubTask(pos);
                }
            }
        }

    }

    protected Task getWanderTask() {
        return new TimeoutWanderTask(true);
    }

    protected abstract boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos);

    public boolean failedSearch() {
        return searcher == null;
    }

    public void resetSearch() {
        //Debug.logMessage("Search reset");
        searcher = null;
        alreadyExplored.clear();
        // We want to search the currently loaded chunks too!!!
        for (ChunkPos start : AltoClef.getInstance().getChunkTracker().getLoadedChunks()) {
            onChunkLoad(start);
        }
    }

    class SearchSubTask extends ChunkSearchTask {

        public SearchSubTask(ChunkPos start) {
            super(start);
        }

        @Override
        protected boolean isChunkPartOfSearchSpace(AltoClef mod, ChunkPos pos) {
            return isChunkWithinSearchSpace(mod, pos);
        }

        @Override
        public ChunkPos getBestChunk(AltoClef mod, List<ChunkPos> chunks) {
            ChunkPos override = getBestChunkOverride(mod, chunks);
            if (override != null) return override;
            return super.getBestChunk(mod, chunks);
        }

        @Override
        protected boolean isChunkSearchEqual(ChunkSearchTask other) {
            // Since we're keeping track of "_searcher", we expect the subchild routine to ALWAYS be consistent!
            return other == this;//return other instanceof SearchSubTask;
        }

        @Override
        protected String toDebugString() {
            return "Searching chunks...";
        }
    }

}
