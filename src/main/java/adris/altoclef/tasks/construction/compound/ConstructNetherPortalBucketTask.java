package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.ClearLiquidTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceObsidianBucketTask;
import adris.altoclef.tasks.movement.GetWithinRangeOfBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.*;

/**
 * 通过水和熔岩浇筑来建造下界传送门
 * <p>
 * 目前最可靠的传送门建造方法。
 */
public class ConstructNetherPortalBucketTask extends Task {

    // 顺序很重要
    private static final Vec3i[] PORTAL_FRAME = new Vec3i[]{
            // 左侧
            new Vec3i(0, 0, -1),
            new Vec3i(0, 1, -1),
            new Vec3i(0, 2, -1),
            // 右侧
            new Vec3i(0, 0, 2),
            new Vec3i(0, 1, 2),
            new Vec3i(0, 2, 2),
            // 顶部
            new Vec3i(0, 3, 0),
            new Vec3i(0, 3, 1),
            // 底部
            new Vec3i(0, -1, 0),
            new Vec3i(0, -1, 1)
    };

    private static final Vec3i[] PORTAL_INTERIOR = new Vec3i[]{
            //内部
            new Vec3i(0, 0, 0),
            new Vec3i(0, 1, 0),
            new Vec3i(0, 2, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 1, 1),
            new Vec3i(0, 2, 1),
            //外部 1
            new Vec3i(1, 0, 0),
            new Vec3i(1, 1, 0),
            new Vec3i(1, 2, 0),
            new Vec3i(1, 0, 1),
            new Vec3i(1, 1, 1),
            new Vec3i(1, 2, 1),
            //外部 2
            new Vec3i(-1, 0, 0),
            new Vec3i(-1, 1, 0),
            new Vec3i(-1, 2, 0),
            new Vec3i(-1, 0, 1),
            new Vec3i(-1, 1, 1),
            new Vec3i(-1, 2, 1)
    };

    // "可建造传送门"区域包含传送门(1 x 6 x 4结构)及其构造和水相关操作的外层缓冲区
    // "区域相对传送门原点"对应于相对于"可建造传送门"区域的传送门原点(参见_portalOrigin)
    // 这只能通过视觉方式解释，抱歉！
    private static final Vec3i PORTALABLE_REGION_SIZE = new Vec3i(4, 6, 6);
    private static final Vec3i PORTAL_ORIGIN_RELATIVE_TO_REGION = new Vec3i(1, 0, 2);
    private final TimerGame lavaSearchTimer = new TimerGame(5);
    private final MovementProgressChecker progressChecker = new MovementProgressChecker();
    private final TimeoutWanderTask wanderTask = new TimeoutWanderTask(5);
    // Stored here to cache lava blacklist
    private final Task collectLavaTask = TaskCatalogue.getItemTask(Items.LAVA_BUCKET, 1);
    private final TimerGame refreshTimer = new TimerGame(11);
    private BlockPos portalOrigin = null;
    private Task getToLakeTask = null;
    private BlockPos currentDestroyTarget = null;

    private boolean firstSearch = false;

    @Override
    protected void onStart() {
        currentDestroyTarget = null;

        AltoClef mod = AltoClef.getInstance();
        mod.getBehaviour().push();

        // Avoid breaking portal frame if we're obsidian.
        // Also avoid placing on the lava + water
        // Also avoid breaking the cast frame
        mod.getBehaviour().avoidBlockBreaking(block -> {
            if (portalOrigin != null) {
                // Don't break frame
                for (Vec3i framePosRelative : PORTAL_FRAME) {
                    BlockPos framePos = portalOrigin.add(framePosRelative);
                    if (block.equals(framePos)) {
                        return mod.getWorld().getBlockState(framePos).getBlock() == Blocks.OBSIDIAN;
                    }
                }
            }
            return false;
        });

        // Protect some used items
        mod.getBehaviour().addProtectedItems(Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.FLINT_AND_STEEL, Items.FIRE_CHARGE);

        progressChecker.reset();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        if (portalOrigin != null) {
            if (mod.getWorld().getBlockState(portalOrigin.up()).getBlock() == Blocks.NETHER_PORTAL) {
                setDebugState("下界传送门建造完成。");
                mod.getBlockScanner().addBlock(Blocks.NETHER_PORTAL, portalOrigin.up());
                return null;
            }
        }
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            progressChecker.reset();
        }
        if (wanderTask.isActive() && !wanderTask.isFinished()) {
            setDebugState("重新尝试。");
            progressChecker.reset();
            return wanderTask;
        }

        if (!progressChecker.check(mod)) {
            mod.getClientBaritone().getPathingBehavior().forceCancel();
            if (portalOrigin != null && currentDestroyTarget != null) {
                mod.getBlockScanner().requestBlockUnreachable(portalOrigin);
                mod.getBlockScanner().requestBlockUnreachable(currentDestroyTarget);
                if (mod.getBlockScanner().isUnreachable(portalOrigin) && mod.getBlockScanner().isUnreachable(currentDestroyTarget)) {
                    portalOrigin = null;
                    currentDestroyTarget = null;
                }
                return wanderTask;
            }
        }
        if (refreshTimer.elapsed()) {
            Debug.logMessage("临时解决方案：再次刷新库存以确保");
            refreshTimer.reset();
            mod.getSlotHandler().refreshInventory();
        }

        //If too far, reset.
        if (portalOrigin != null && !portalOrigin.isWithinDistance(mod.getPlayer().getPos(), 2000)) {
            portalOrigin = null;
            currentDestroyTarget = null;
        }

        if (currentDestroyTarget != null) {
            if (!WorldHelper.isSolidBlock(currentDestroyTarget)) {
                currentDestroyTarget = null;
            } else {
                return new DestroyBlockTask(currentDestroyTarget);
            }
        }
        // 如果没有打火石和铁，获取一个
        if (!mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL) && !mod.getItemStorage().hasItem(Items.FIRE_CHARGE)) {
            setDebugState("获取打火石和铁");
            progressChecker.reset();
            return TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
        }
        // 如果没有桶，获取一个。
        int bucketCount = mod.getItemStorage().getItemCount(Items.BUCKET, Items.LAVA_BUCKET, Items.WATER_BUCKET);
        if (bucketCount < 2) {
            setDebugState("获取桶");
            progressChecker.reset();
            // 如果我们有熔岩/水桶，获取另一种。否则我们掉了桶，只需获取一个桶。
            if (mod.getItemStorage().hasItem(Items.LAVA_BUCKET)) {
                return TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
            } else if (mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                return TaskCatalogue.getItemTask(Items.LAVA_BUCKET, 1);
            }
            if (mod.getEntityTracker().itemDropped(Items.WATER_BUCKET, Items.LAVA_BUCKET)) {
                return new PickupDroppedItemTask(new ItemTarget(new Item[]{Items.WATER_BUCKET, Items.LAVA_BUCKET}, 1), true);
            }
            return TaskCatalogue.getItemTask(Items.BUCKET, 2);
        }

        boolean needsToLookForPortal = portalOrigin == null;
        if (needsToLookForPortal) {
            progressChecker.reset();
            // 搜索前先获取水，方便操作。
            if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                setDebugState("获取水桶");
                progressChecker.reset();
                return TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
            }

            boolean foundSpot = false;

                if (firstSearch || lavaSearchTimer.elapsed()) {
                    firstSearch = false;
                    lavaSearchTimer.reset();
                    Debug.logMessage("(搜索附近的熔岩湖和可建造传送门的位置...)");
                    BlockPos lavaPos = findLavaLake(mod, mod.getPlayer().getBlockPos());
                    if (lavaPos != null) {
                        // 找到了熔岩湖，设置我们的传送门原点！
                        BlockPos foundPortalRegion = getPortalableRegion(mod, lavaPos, mod.getPlayer().getBlockPos(), new Vec3i(-1, 0, 0), PORTALABLE_REGION_SIZE, 20);
                        if (foundPortalRegion == null) {
                            Debug.logWarning("未能找到附近的可建造传送门区域。考虑增加搜索超时范围");
                        } else {
                            portalOrigin = foundPortalRegion.add(PORTAL_ORIGIN_RELATIVE_TO_REGION);
                            foundSpot = true;

                            getToLakeTask = new GetWithinRangeOfBlockTask(portalOrigin,7);
                            return getToLakeTask;
                        }
                    } else {
                        Debug.logMessage("(未找到熔岩湖)");
                    }
                }

            if (!foundSpot) {
                setDebugState("(超时: 寻找熔岩湖)");
                return new TimeoutWanderTask();
            }
        }

        if (BeatMinecraftTask.isTaskRunning(mod,getToLakeTask)) {
            return getToLakeTask;
        }

        // We have a portal, now build it.
        for (Vec3i framePosRelative : PORTAL_FRAME) {
            BlockPos framePos = portalOrigin.add(framePosRelative);
            Block frameBlock = mod.getWorld().getBlockState(framePos).getBlock();
            if (frameBlock == Blocks.OBSIDIAN) {
                // 已经满足条件，如有需要清除上方的水。
                BlockPos waterCheck = framePos.up();
                if (mod.getWorld().getBlockState(waterCheck).getBlock() == Blocks.WATER && WorldHelper.isSourceBlock(waterCheck, true)) {
                    setDebugState("清除浇筑的水");
                    return new ClearLiquidTask(waterCheck);
                }
                continue;
            }

            // 提前获取熔岩以便更快放置
            if (!mod.getItemStorage().hasItem(Items.LAVA_BUCKET) && frameBlock != Blocks.LAVA) {
                setDebugState("收集熔岩");
                progressChecker.reset();
                return collectLavaTask;
            }

            // 我们需要在这里放置黑曜石。
            if (mod.getBlockScanner().isUnreachable(framePos)) {
                portalOrigin = null;
            }
            return new PlaceObsidianBucketTask(framePos);
        }

        // 现在，清理内部。
        for (Vec3i offs : PORTAL_INTERIOR) {
            BlockPos p = portalOrigin.add(offs);
            assert MinecraftClient.getInstance().world != null;
            if (!MinecraftClient.getInstance().world.getBlockState(p).isAir()) {
                setDebugState("清理传送门内部");
                currentDestroyTarget = p;
                return null;
                //return new DestroyBlockTask(p);
            }
        }

        setDebugState("使用打火石点火");
        // 用打火石点火，宝贝
        return new InteractWithBlockTask(new ItemTarget(new Item[]{Items.FLINT_AND_STEEL, Items.FIRE_CHARGE}, 1), Direction.UP, portalOrigin.down(), true);
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof ConstructNetherPortalBucketTask;
    }

    @Override
    protected String toDebugString() {
        return "建造下界传送门";
    }

    private BlockPos findLavaLake(AltoClef mod, BlockPos playerPos) {
        HashSet<BlockPos> alreadyExplored = new HashSet<>();
        double nearestSqDistance = Double.POSITIVE_INFINITY;
        BlockPos nearestLake = null;
        List<BlockPos> lavas = mod.getBlockScanner().getKnownLocations(Blocks.LAVA);

        if (!lavas.isEmpty()) {
            for (BlockPos pos : lavas) {
                if (alreadyExplored.contains(pos)) continue;
                double sqDist = playerPos.getSquaredDistance(pos);
                if (sqDist < nearestSqDistance) {
                    int depth = getNumberOfBlocksAdjacent(alreadyExplored, pos);
                    if (depth != 0) {
                        Debug.logMessage("Found with depth " + depth);
                        if (depth >= 12) {
                            nearestSqDistance = sqDist;
                            nearestLake = pos;
                        }
                    }
                }
            }
        }
        return nearestLake;
    }

    private int getNumberOfBlocksAdjacent(HashSet<BlockPos> alreadyExplored, BlockPos start) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);

        int bonus = 0;

        while (!queue.isEmpty()) {
            BlockPos origin = queue.poll();
            if (alreadyExplored.contains(origin)) continue;
            alreadyExplored.add(origin);

            // Base case: We hit a non-full lava block.
            assert MinecraftClient.getInstance().world != null;
            BlockState s = MinecraftClient.getInstance().world.getBlockState(origin);
            if (s.getBlock() != Blocks.LAVA) {
                continue;
            } else {
                // We may not be a full lava block
                if (!s.getFluidState().isStill()) continue;
                int level = s.getFluidState().getLevel();
                //Debug.logMessage("TEST LEVEL: " + level + ", " + height);
                // Only accept FULL SOURCE BLOCKS
                if (level != 8) continue;
            }

            queue.addAll(List.of(origin.north(), origin.south(), origin.east(), origin.west(), origin.up(), origin.down()));

            bonus++;
        }

        return bonus;
    }

    // Get a region that a portal can fit into
    private BlockPos getPortalableRegion(AltoClef mod, BlockPos lava, BlockPos playerPos, Vec3i sizeOffset, Vec3i sizeAllocation, int timeoutRange) {
        Vec3i[] directions = new Vec3i[]{new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)};

        double minDistanceToPlayer = Double.POSITIVE_INFINITY;
        BlockPos bestPos = null;

        for (Vec3i direction : directions) {

            // Inch along
            for (int offs = 1; offs < timeoutRange; ++offs) {

                Vec3i offset = new Vec3i(direction.getX() * offs, direction.getY() * offs, direction.getZ() * offs);

                boolean found = true;
                boolean solidFound = false;
                // check for collision with lava in box
                // We have an extra buffer to make sure we never break a block NEXT to lava.
                moveAlongLine:
                for (int dx = -1; dx < sizeAllocation.getX() + 1; ++dx) {
                    for (int dz = -1; dz < sizeAllocation.getZ() + 1; ++dz) {
                        for (int dy = -1; dy < sizeAllocation.getY(); ++dy) {
                            BlockPos toCheck = lava.add(offset).add(sizeOffset).add(dx,dy,dz);
                            assert MinecraftClient.getInstance().world != null;
                            BlockState state = MinecraftClient.getInstance().world.getBlockState(toCheck);
                            if (state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BEDROCK) {
                                found = false;
                                break moveAlongLine;
                            }
                            // Also check for at least 1 solid block for us to place on...
                            if (dy <= 1 && !solidFound && WorldHelper.isSolidBlock(toCheck)) {
                                solidFound = true;
                            }
                        }
                    }
                }
                // Check for solid ground at least somewhere
                if (!solidFound) {
                    break;
                }

                if (found) {
                    BlockPos foundBoxCorner = lava.add(offset).add(sizeOffset);
                    double sqDistance = foundBoxCorner.getSquaredDistance(playerPos);
                    if (sqDistance < minDistanceToPlayer) {
                        minDistanceToPlayer = sqDistance;
                        bestPos = foundBoxCorner;
                    }
                    break;
                }
            }

        }

        return bestPos;
    }
}
