package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.ChunkLoadEvent;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用于遍历和搜索互连结构或生物群系的任务
 * <p>
 * 使用示例：
 * - 在黑森林中搜索林地府邸，避免前往不同生物群系
 * - 在下界要塞中搜索烈焰人刷怪笼
 * - 在要塞中搜索传送门
 */
abstract class ChunkSearchTask extends Task {

    // 起始点
    private final BlockPos _startPoint;
    // 搜索互斥锁
    private final Object _searchMutex = new Object();
    // 我们已经搜索过或稍后将被搜索的区块
    private final Set<ChunkPos> _consideredAlready = new HashSet<>();
    // 我们确实已经搜索过的区块
    private final Set<ChunkPos> _searchedAlready = new HashSet<>();
    // 稍后搜索的区块列表
    private final ArrayList<ChunkPos> _searchLater = new ArrayList<>();
    // 刚加载的区块列表
    private final ArrayList<ChunkPos> _justLoaded = new ArrayList<>();
    // 是否是首次运行
    private boolean _first = true;
    // 是否已完成
    private boolean _finished = false;

    // 区块加载事件订阅
    private Subscription<ChunkLoadEvent> _onChunkLoad;

    /**
     * 构造函数，使用方块位置作为起始点
     * @param startPoint 起始点
     */
    public ChunkSearchTask(BlockPos startPoint) {
        _startPoint = startPoint;
    }

    /**
     * 构造函数，使用区块位置
     * @param chunkPos 区块位置
     */
    public ChunkSearchTask(ChunkPos chunkPos) {
        this(chunkPos.getStartPos().add(1,1,1));
    }

    /**
     * 获取已搜索的区块集合
     * @return 返回已搜索的区块集合
     */
    public Set<ChunkPos> getSearchedChunks() {
        return _searchedAlready;
    }

    /**
     * 检查是否已完成搜索
     * @return 如果已完成返回true
     */
    public boolean finished() {
        return _finished;
    }

    @Override
    protected void onStart() {

        //Debug.logMessage("(deleteme) start. Finished: " + _finished);
        if (_first) {
            _finished = false;
            _first = false;
            ChunkPos startPos = AltoClef.getInstance().getWorld().getChunk(_startPoint).getPos();
            synchronized (_searchMutex) {
                searchChunkOrQueueSearch(AltoClef.getInstance(), startPos);
            }
        }

        // 订阅区块加载事件
        _onChunkLoad = EventBus.subscribe(ChunkLoadEvent.class, evt -> {
            WorldChunk chunk = evt.chunk;
            if (chunk == null) return;
            synchronized (_searchMutex) {
                if (!_searchedAlready.contains(chunk.getPos())) {
                    _justLoaded.add(chunk.getPos());
                }
            }
        });
    }

    @Override
    protected Task onTick() {

        // WTF 这是个糟糕的想法。
        // 区块搜索失败时的备份？
        //onChunkLoad((WorldChunk) mod.getWorld().getChunk(mod.getPlayer().getBlockPos()));

        synchronized (_searchMutex) {
            // 搜索所有从 _justLoaded 中应当搜索的项目
            if (!_justLoaded.isEmpty()) {
                for (ChunkPos justLoaded : _justLoaded) {
                    if (_searchLater.contains(justLoaded)) {
                        // 搜索这个。如果成功，我们不再需要搜索。
                        if (trySearchChunk(AltoClef.getInstance(), justLoaded)) {
                            _searchLater.remove(justLoaded);
                        }
                    }
                }
            }
            _justLoaded.clear();
        }

        // 现在我们有了更新的地图，前往最近的区块
        ChunkPos closest = getBestChunk(AltoClef.getInstance(), _searchLater);

        if (closest == null) {
            _finished = true;
            Debug.logWarning("未能找到要前往的任何区块。如果我们完成，这意味着我们扫描了所有可能的区块。");
            //Debug.logMessage("wtf??????: " + _finished);
            return null;
        }

        return new GetToChunkTask(closest);
    }

    // 虚方法
    /**
     * 获取最佳的区块
     * @param mod AltoClef实例
     * @param chunks 区块列表
     * @return 返回最佳的区块位置
     */
    protected ChunkPos getBestChunk(AltoClef mod, List<ChunkPos> chunks) {
        double lowestScore = Double.POSITIVE_INFINITY;
        ChunkPos bestChunk = null;
        if (!chunks.isEmpty()) {
            for (ChunkPos toSearch : chunks) {
                double cx = (toSearch.getStartX() + toSearch.getEndX() + 1) / 2.0, cz = (toSearch.getStartZ() + toSearch.getEndZ() + 1) / 2.0;
                double px = mod.getPlayer().getX(), pz = mod.getPlayer().getZ();
                double distanceSq = (cx - px) * (cx - px) + (cz - pz) * (cz - pz);
                double distanceToCenterSq = new Vec3d(_startPoint.getX() - cx, 0, _startPoint.getZ() - cz).lengthSquared();
                double score = distanceSq + distanceToCenterSq * 0.8;
                if (score < lowestScore) {
                    lowestScore = score;
                    bestChunk = toSearch;
                }
            }
        }
        return bestChunk;
    }

    @Override
    protected void onStop(Task interruptTask) {
        EventBus.unsubscribe(_onChunkLoad);
    }

    @Override
    public boolean isFinished() {
        return _searchLater.size() == 0;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ChunkSearchTask task) {
            if (!task._startPoint.equals(_startPoint)) return false;
            return isChunkSearchEqual(task);
        }
        return false;
    }

    /**
     * 搜索区块或将其加入队列进行搜索
     * @param mod AltoClef实例
     * @param pos 区块位置
     */
    private void searchChunkOrQueueSearch(AltoClef mod, ChunkPos pos) {
        // 不要再次搜索/考虑这个区块。
        if (_consideredAlready.contains(pos)) {
            return;
        }
        _consideredAlready.add(pos);

        if (!trySearchChunk(mod, pos)) {
            // 如果我们还没有搜索过，我们会稍后检查它。
            if (!_searchedAlready.contains(pos)) {
                _searchLater.add(pos);
            }
        }
    }

    /**
     * 尝试搜索区块。
     *
     * @param pos 要搜索的区块
     * @return 如果我们已完成搜索这个区块则返回true，如果需要亲自搜索则返回false
     */
    private boolean trySearchChunk(AltoClef mod, ChunkPos pos) {
        // 不要稍后搜索。
        if (_searchedAlready.contains(pos)) {
            return true;
        }
        if (mod.getChunkTracker().isChunkLoaded(pos)) {
            _searchedAlready.add(pos);
            if (isChunkPartOfSearchSpace(mod, pos)) {
                // 这个区块可能会通向更多地方，所以搜索或加入邻居到队列
                searchChunkOrQueueSearch(mod, new ChunkPos(pos.x + 1, pos.z));
                searchChunkOrQueueSearch(mod, new ChunkPos(pos.x - 1, pos.z));
                searchChunkOrQueueSearch(mod, new ChunkPos(pos.x, pos.z + 1));
                searchChunkOrQueueSearch(mod, new ChunkPos(pos.x, pos.z - 1));
            }
            return true;
        }
        return false;
    }

    /**
     * 检查区块是否属于搜索空间
     * @param mod AltoClef实例
     * @param pos 区块位置
     * @return 如果属于搜索空间返回true
     */
    protected abstract boolean isChunkPartOfSearchSpace(AltoClef mod, ChunkPos pos);

    /**
     * 检查区块搜索任务是否相等
     * @param other 其他区块搜索任务
     * @return 如果相等返回true
     */
    protected abstract boolean isChunkSearchEqual(ChunkSearchTask other);
}
