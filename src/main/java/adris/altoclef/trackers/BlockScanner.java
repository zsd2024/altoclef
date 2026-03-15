package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.trackers.blacklisting.WorldLocateBlacklist;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.function.Predicate;

public class BlockScanner {

    private static final boolean LOG = false;
    private static final int RESCAN_TICK_DELAY = 4 * 20; // 重扫描延迟（刻度）
    private static final int CACHED_POSITIONS_PER_BLOCK = 40; // 每种方块缓存的位置数量


    private final AltoClef mod;
    private final TimerGame rescanTimer = new TimerGame(1);

    private final HashMap<Block, HashSet<BlockPos>> trackedBlocks = new HashMap<>(); // 已追踪的方块位置
    private final HashMap<Block, HashSet<BlockPos>> scannedBlocks = new HashMap<>(); // 已扫描的方块位置
    private final HashMap<ChunkPos, Long> scannedChunks = new HashMap<>(); // 已扫描的区块
    private final WorldLocateBlacklist blacklist = new WorldLocateBlacklist(); // 世界定位黑名单
    // 扫描时使用
    private HashMap<Block, HashSet<BlockPos>> cachedScannedBlocks = new HashMap<>();
    private Dimension scanDimension = Dimension.OVERWORLD; // 扫描维度
    private World scanWorld = null; // 扫描世界

    private boolean scanning = false; // 是否正在扫描
    private boolean forceStop = false; // 是否强制停止


    public BlockScanner(AltoClef mod) {
        this.mod = mod;

        EventBus.subscribe(BlockPlaceEvent.class, evt -> addBlock(evt.blockState.getBlock(), evt.blockPos));
    }


    /**
     * 添加一个方块位置到跟踪列表
     * @param block 方块类型
     * @param pos 方块位置
     */
    public void addBlock(Block block, BlockPos pos) {
        if (!isBlockAtPosition(pos, block)) {
            Debug.logInternal("无效设置: " + block + " " + pos);
            return;
        }

        if (trackedBlocks.containsKey(block)) {
            trackedBlocks.get(block).add(pos);
        } else {
            HashSet<BlockPos> set = new HashSet<>();
            set.add(pos);

            trackedBlocks.put(block, set);
        }
    }


    /**
     * 请求将方块位置标记为不可到达
     * @param pos 方块位置
     * @param allowedFailures 允许失败次数
     */
    public void requestBlockUnreachable(BlockPos pos, int allowedFailures) {
        blacklist.blackListItem(mod, pos, allowedFailures);
    }

    //TODO 使用配置替换数字4
    /**
     * 请求将方块位置标记为不可到达（默认允许失败4次）
     * @param pos 方块位置
     */
    public void requestBlockUnreachable(BlockPos pos) {
        blacklist.blackListItem(mod, pos, 4);
    }


    /**
     * 检查方块位置是否不可到达
     * @param pos 方块位置
     * @return 是否不可到达
     */
    public boolean isUnreachable(BlockPos pos) {
        return blacklist.unreachable(pos);
    }

    /**
     * 获取已知的方块位置列表
     * @param blocks 要查找的方块类型
     * @return 已知位置列表
     */
    public List<BlockPos> getKnownLocations(Block... blocks) {
        List<BlockPos> locations = new LinkedList<>();

        for (Block block : blocks) {
            if (!trackedBlocks.containsKey(block)) continue;

            locations.addAll(trackedBlocks.get(block));
        }
        locations.removeIf(this::isUnreachable);

        return locations;
    }

    /**
     * 扫描指定半径内最近的方块类型。
     *
     * @param pos    半径的中心
     * @param range  要扫描的半径
     * @param blocks 要检查的方块类型
     */
    public Optional<BlockPos> getNearestWithinRange(Vec3d pos, double range, Block... blocks) {
        Optional<BlockPos> nearest = getNearestBlock(pos, blocks);

        if (nearest.isEmpty() || nearest.get().isWithinDistance(pos, range)) return nearest;

        return Optional.empty();
    }

    /**
     * 扫描指定半径内最近的方块类型
     * @param pos 方块位置
     * @param range 要扫描的半径
     * @param blocks 要检查的方块类型
     * @return 最近的方块位置（如果在范围内）
     */
    public Optional<BlockPos> getNearestWithinRange(BlockPos pos, double range, Block... blocks) {
        return getNearestWithinRange(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), range, blocks);
    }


    /**
     * 检查是否存在指定方块
     * @param blocks 要检查的方块类型
     * @return 是否存在
     */
    public boolean anyFound(Block... blocks) {
        return anyFound((block) -> true, blocks);
    }


    /**
     * 检查是否存在满足条件的指定方块
     * @param isValidTest 有效性测试条件
     * @param blocks 要检查的方块类型
     * @return 是否存在
     */
    public boolean anyFound(Predicate<BlockPos> isValidTest, Block... blocks) {
        for (Block block : blocks) {
            if (!trackedBlocks.containsKey(block)) continue;

            for (BlockPos pos : trackedBlocks.get(block)) {
                if (isValidTest.test(pos) && mod.getWorld().getBlockState(pos).getBlock().equals(block) && !this.isUnreachable(pos))
                    return true;
            }
        }

        return false;
    }

    /**
     * 获取最近的方块位置
     * @param blocks 要查找的方块类型
     * @return 最近的方块位置
     */
    public Optional<BlockPos> getNearestBlock(Block... blocks) {
        // 添加一点点偏移，以防止总是向下挖掘/偏向玩家下方的方块
        return getNearestBlock(mod.getPlayer().getPos().add(0, 0.6f, 0), blocks);
    }

    /**
     * 从指定位置获取最近的方块位置
     * @param pos 起始位置
     * @param blocks 要查找的方块类型
     * @return 最近的方块位置
     */
    public Optional<BlockPos> getNearestBlock(Vec3d pos, Block... blocks) {
        return getNearestBlock(pos, p -> true, blocks);
    }

    /**
     * 获取满足条件的最近方块位置
     * @param isValidTest 有效性测试条件
     * @param blocks 要查找的方块类型
     * @return 最近的方块位置
     */
    public Optional<BlockPos> getNearestBlock(Predicate<BlockPos> isValidTest, Block... blocks) {
        return getNearestBlock(mod.getPlayer().getPos().add(0, 0.6f, 0), isValidTest, blocks);
    }

    /**
     * 从指定位置获取满足条件的最近方块位置
     * @param pos 起始位置
     * @param isValidTest 有效性测试条件
     * @param blocks 要查找的方块类型
     * @return 最近的方块位置
     */
    public Optional<BlockPos> getNearestBlock(Vec3d pos, Predicate<BlockPos> isValidTest, Block... blocks) {
        Optional<BlockPos> closest = Optional.empty();

        for (Block block : blocks) {
            Optional<BlockPos> p = getNearestBlock(block, isValidTest, pos);

            if (p.isPresent()) {
                if (closest.isEmpty()) closest = p;
                else {
                    if (BaritoneHelper.calculateGenericHeuristic(pos, WorldHelper.toVec3d(closest.get())) > BaritoneHelper.calculateGenericHeuristic(pos, WorldHelper.toVec3d(p.get()))) {
                        closest = p;
                    }
                }
            }
        }

        return closest;
    }

    /**
     * 获取指定方块类型的最近位置
     * @param block 方块类型
     * @param fromPos 起始位置
     * @return 最近的方块位置
     */
    public Optional<BlockPos> getNearestBlock(Block block, Vec3d fromPos) {
        return getNearestBlock(block, (pos) -> true, fromPos);
    }

    /**
     * 获取满足条件的指定方块类型的最近位置
     * @param block 方块类型
     * @param isValidTest 有效性测试条件
     * @param fromPos 起始位置
     * @return 最近的方块位置
     */
    public Optional<BlockPos> getNearestBlock(Block block, Predicate<BlockPos> isValidTest, Vec3d fromPos) {
        BlockPos pos = null;
        double nearest = Double.POSITIVE_INFINITY;

        if (!trackedBlocks.containsKey(block)) {
            return Optional.empty();
        }

        for (BlockPos p : trackedBlocks.get(block)) {
            // 确保方块存在（重新扫描时可能改变）
            if (!mod.getWorld().getBlockState(p).getBlock().equals(block)) continue;
            if (!isValidTest.test(p) || isUnreachable(p)) continue;

            double dist = BaritoneHelper.calculateGenericHeuristic(fromPos, WorldHelper.toVec3d(p));

            if (dist < nearest) {
                nearest = dist;
                pos = p;
            }
        }

        return pos != null ? Optional.of(pos) : Optional.empty();
    }

    /**
     * 检查指定距离内是否存在方块
     * @param distance 检查距离
     * @param blocks 要查找的方块类型
     * @return 是否存在
     */
    public boolean anyFoundWithinDistance(double distance, Block... blocks) {
        return anyFoundWithinDistance(mod.getPlayer().getPos().add(0, 0.6f, 0), distance, blocks);
    }

    /**
     * 从指定位置检查指定距离内是否存在方块
     * @param pos 起始位置
     * @param distance 检查距离
     * @param blocks 要查找的方块类型
     * @return 是否存在
     */
    public boolean anyFoundWithinDistance(Vec3d pos, double distance, Block... blocks) {
        Optional<BlockPos> blockPos = getNearestBlock(blocks);
        return blockPos.map(value -> value.isWithinDistance(pos, distance)).orElse(false);
    }

    /**
     * 获取到最近方块的距离
     * @param blocks 要查找的方块类型
     * @return 到最近方块的距离
     */
    public double distanceToClosest(Block... blocks) {
        return distanceToClosest(mod.getPlayer().getPos().add(0, 0.6f, 0), blocks);
    }

    /**
     * 从指定位置获取到最近方块的距离
     * @param pos 起始位置
     * @param blocks 要查找的方块类型
     * @return 到最近方块的距离
     */
    public double distanceToClosest(Vec3d pos, Block... blocks) {
        Optional<BlockPos> blockPos = getNearestBlock(blocks);
        return blockPos.map(value ->  Math.sqrt(BlockPosVer.getSquaredDistance(value, pos))).orElse(Double.POSITIVE_INFINITY);
    }

    // 检查位置是否为指定方块类型
    // 如果不正确或不确定则返回false
    public boolean isBlockAtPosition(BlockPos pos, Block... blocks) {
        if (isUnreachable(pos)) {
            return false;
        }

        if (!mod.getChunkTracker().isChunkLoaded(pos)) {
            return false;
        }

        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return false;
        }
        try {
            for (Block block : blocks) {
                if (world.isAir(pos) && WorldHelper.isAir(block)) {
                    return true;
                }
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() == block) {
                    return true;
                }
            }
            return false;
        } catch (NullPointerException e) {
            // 可能超出区块范围。这意味着我们无法判断它的状态。
            return false;
        }
    }

    /**
     * 重置方块扫描器
     */
    public void reset() {
        trackedBlocks.clear();
        scannedBlocks.clear();
        scannedChunks.clear();
        rescanTimer.forceElapse();
        blacklist.clear();
        forceStop = true;
    }

    /**
     * 每个游戏刻度执行一次
     */
    public void tick() {
        if (mod.getWorld() == null || mod.getPlayer() == null) return;
        // 最大程度地了解你周围的最近方块
        scanCloseBlocks();
        if (!rescanTimer.elapsed() || scanning) return;

        if (scanDimension != WorldHelper.getCurrentDimension() || mod.getWorld() != scanWorld) {
            if (LOG) {
                mod.log("方块扫描器: 检测到新维度或世界，正在重置数据！");
            }
            reset();
            scanWorld = mod.getWorld();
            scanDimension = WorldHelper.getCurrentDimension();
            return;
        }

        cachedScannedBlocks = new HashMap<>(scannedBlocks.size());
        for (Map.Entry<Block, HashSet<BlockPos>> entry : scannedBlocks.entrySet()) {
            cachedScannedBlocks.put(entry.getKey(), (HashSet<BlockPos>) entry.getValue().clone());
        }

        if (LOG) {
            mod.log("更新方块扫描器.. 大小: " + trackedBlocks.size() + " : " + cachedScannedBlocks.size());
        }

        scanning = true;
        forceStop = false;
        new Thread(() -> {
            try {
                rescan(Integer.MAX_VALUE, Integer.MAX_VALUE);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                rescanTimer.reset();
                scanning = false;
            }
        }).start();
    }

    /**
     * 扫描附近的方块
     */
    private void scanCloseBlocks() {
        for (Map.Entry<Block, HashSet<BlockPos>> entry : cachedScannedBlocks.entrySet()) {
            if (!trackedBlocks.containsKey(entry.getKey())) {
                trackedBlocks.put(entry.getKey(), new HashSet<>());
            }
            trackedBlocks.get(entry.getKey()).clear();

            trackedBlocks.get(entry.getKey()).addAll(entry.getValue());
        }

        HashMap<Block, HashSet<BlockPos>> map = new HashMap<>();

        BlockPos pos = mod.getPlayer().getBlockPos();
        World world = mod.getPlayer().getWorld();

        for (int x = pos.getX() - 8; x <= pos.getX() + 8; x++) {
            for (int y = pos.getY() - 8; y < pos.getY() + 8; y++) {
                for (int z = pos.getZ() - 8; z <= pos.getZ() + 8; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if (world.getBlockState(p).isAir()) continue;

                    Block block = state.getBlock();

                    if (map.containsKey(block)) {
                        map.get(block).add(p);
                    } else {
                        HashSet<BlockPos> set = new HashSet<>();
                        set.add(p);
                        map.put(block, set);
                    }
                }
            }
        }

        for (Map.Entry<Block, HashSet<BlockPos>> entry : map.entrySet()) {
            getFirstFewPositions(entry.getValue(),mod.getPlayer().getPos());

            if (!trackedBlocks.containsKey(entry.getKey())) {
                trackedBlocks.put(entry.getKey(), new HashSet<>());
            }

            trackedBlocks.get(entry.getKey()).addAll(entry.getValue());
        }
    }

    /**
     * 重新扫描指定数量和半径的区块
     * @param maxCount 最大扫描数量
     * @param cutOffRadius 截断半径
     */
    private void rescan(int maxCount, int cutOffRadius) {
        long ms = System.currentTimeMillis();

        ChunkPos playerChunkPos = mod.getPlayer().getChunkPos();
        Vec3d playerPos = mod.getPlayer().getPos();

        HashSet<ChunkPos> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(new Node(playerChunkPos, 0));

        while (!queue.isEmpty() && visited.size() < maxCount && !forceStop) {
            Node node = queue.poll();

            if (node.distance > cutOffRadius || visited.contains(node.pos) || !mod.getWorld().getChunkManager().isChunkLoaded(node.pos.x, node.pos.z))
                continue;

            boolean isPriorityChunk = getChunkDist(node.pos, playerChunkPos) <= 2;
            if (!isPriorityChunk && scannedChunks.containsKey(node.pos) && mod.getWorld().getTime() - scannedChunks.get(node.pos) < RESCAN_TICK_DELAY)
                continue;

            visited.add(node.pos);
            scanChunk(node.pos, playerChunkPos);

            queue.add(new Node(new ChunkPos(node.pos.x + 1, node.pos.z + 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(node.pos.x - 1, node.pos.z + 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(node.pos.x - 1, node.pos.z - 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(node.pos.x + 1, node.pos.z - 1), node.distance + 1));
        }
        if (forceStop) {
            // 再次重置，因为强制停止时可能已更改一些值
            reset();
            forceStop = false;
            return;
        }

        for (Iterator<ChunkPos> iterator = scannedChunks.keySet().iterator(); iterator.hasNext(); ) {
            ChunkPos pos = iterator.next();
            int distance = getChunkDist(pos, playerChunkPos);

            if (distance > cutOffRadius) {
                iterator.remove();
            }
        }

        for (HashSet<BlockPos> set : scannedBlocks.values()) {
            if (set.size() < CACHED_POSITIONS_PER_BLOCK) {
                continue;
            }

            getFirstFewPositions(set, playerPos);
        }

        if (LOG) {
            mod.log("重新扫描耗时: " + (System.currentTimeMillis() - ms) + " 毫秒; 访问了: " + visited.size() + " 个区块");
        }
    }

    /**
     * 获取两个区块之间的距离
     * @param pos1 区块位置1
     * @param pos2 区块位置2
     * @return 区块之间的距离
     */
    private int getChunkDist(ChunkPos pos1, ChunkPos pos2) {
        return Math.abs(pos1.x - pos2.x) + Math.abs(pos1.z - pos2.z);
    }


    //TODO 重命名
    /**
     * 获取最近的几个位置
     * @param set 方块位置集合
     * @param playerPos 玩家位置
     */
    private void getFirstFewPositions(HashSet<BlockPos> set, Vec3d playerPos) {
        Queue<BlockPos> queue = new PriorityQueue<>(Comparator.comparingDouble((pos) -> -BaritoneHelper.calculateGenericHeuristic(playerPos, WorldHelper.toVec3d(pos))));

        for (BlockPos pos : set) {
            queue.add(pos);

            if (queue.size() > CACHED_POSITIONS_PER_BLOCK) {
                queue.poll();
            }
        }

        set.clear();

        for (int i = 0; i < CACHED_POSITIONS_PER_BLOCK && !queue.isEmpty(); i++) {
            set.add(queue.poll());
        }
    }

    /**
     * 扫描一个区块并将对应的特定方块位置添加到列表中
     *
     * @param chunkPos 被扫描的区块位置
     */
    private void scanChunk(ChunkPos chunkPos, ChunkPos playerChunkPos) {
        World world = mod.getWorld();
        WorldChunk chunk = mod.getWorld().getChunk(chunkPos.x, chunkPos.z);
        scannedChunks.put(chunkPos, world.getTime());

        boolean isPriorityChunk = getChunkDist(chunkPos, playerChunkPos) <= 2;

        for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
            for (int y = world.getBottomY(); y < world.getTopY(); y++) {
                for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (this.isUnreachable(p) || world.isOutOfHeightLimit(p)) continue;

                    BlockState state = chunk.getBlockState(p);
                    if (state.isAir()) continue;

                    Block block = state.getBlock();
                    if (scannedBlocks.containsKey(block)) {
                        HashSet<BlockPos> set = scannedBlocks.get(block);

                        if ((set.size() > CACHED_POSITIONS_PER_BLOCK * 750 && !isPriorityChunk)) continue;

                        set.add(p);
                    } else {
                        HashSet<BlockPos> set = new HashSet<>();
                        set.add(p);
                        scannedBlocks.put(block, set);
                    }
                }
            }
        }
    }

    private record Node(ChunkPos pos, int distance) {
    }


}
