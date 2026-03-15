package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.ClearLiquidTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.HashSet;

@SuppressWarnings("ALL")
@Deprecated
/**
 * 注意：这是不可靠的，我认为最多有大约70%的成功几率。
 * 这里的问题是水源偶尔会到处溢出，这会导致
 * Baritone卡住
 * 使用"ConstructNetherPortalBucketTask"，它更有条理且没有这个陷阱。
 */
public class ConstructNetherPortalSpeedrunTask extends adris.altoclef.tasksystem.Task {

    // "可建造传送门"区域包含传送门(1 x 6 x 4结构)及其构造和水相关操作的外层缓冲区。
    // "区域相对传送门原点"对应于相对于"可建造传送门"区域的传送门原点(参见_portalOrigin)。
    // 这只能通过视觉方式解释，抱歉！
    private static final Vec3i PORTALABLE_REGION_SIZE = new Vec3i(4, 6, 6);
    // 也要破坏这些方块。
    private static final Vec3i[] PORTALABLE_REGION_EXTRA = new Vec3i[]{
            // 底部两个槽位
            new Vec3i(0, -1, 0),
            new Vec3i(0, -1, 1),
            // 减少额外水的水入口
            new Vec3i(2, -1, 0),
            new Vec3i(2, -1, 1)
    };
    private static final Vec3i PORTAL_ORIGIN_RELATIVE_TO_REGION = new Vec3i(1, 0, 2);
    // 相对于传送门原点
    private static final Vec3i[] PORTAL_CONSTRUCTION_FRAME = new Vec3i[]{
            // 左侧倒L形: 从底部开始
            new Vec3i(1, 0, -1),
            new Vec3i(1, 1, -1),
            new Vec3i(1, 2, -1),
            new Vec3i(1, 3, -1),
            new Vec3i(0, 3, -1),

            // T形/右侧延伸
            new Vec3i(1, 0, 0),
            new Vec3i(1, 0, 1),
            new Vec3i(1, 1, 1),
            new Vec3i(1, 0, 2),
            // 阻挡水的右侧额外推动
            new Vec3i(1, 1, 2),
            new Vec3i(1, 2, 2),
            new Vec3i(2, 0, 2),

            // 底部两个黑曜石下方的部分
            new Vec3i(0, -2, 0),
            new Vec3i(0, -2, 1)
    };
    // 熔岩如何放置以形成传送门。(相对于原点放置位置和放置方向)
    // !! 还表示熔岩放置的顺序。
    private static final LavaTarget[] PORTAL_FRAME_LAVA = new LavaTarget[]{
            // 左侧
            new LavaTarget(0, 0, -1, Direction.fromVector(-1, 0, 0)),
            new LavaTarget(0, 1, -1, Direction.fromVector(-1, 0, 0)),
            new LavaTarget(0, 2, -1, Direction.fromVector(0, 1, 0)),
            // 右侧
            new LavaTarget(0, 0, 2, Direction.fromVector(-1, 0, 0)),
            new LavaTarget(0, 1, 2, Direction.fromVector(0, 1, 0)),
            new LavaTarget(0, 2, 2, Direction.fromVector(0, 1, 0)),
            // 底部
            new LavaTarget(0, -1, 0, Direction.fromVector(0, 1, 0)),
            new LavaTarget(0, -1, 1, Direction.fromVector(0, 1, 0)),
            // 顶部
            new LavaTarget(0, 3, 0, Direction.fromVector(0, 0, 1)),
            new LavaTarget(0, 3, 1, Direction.fromVector(0, 0, 1))
    };
    private static final Vec3i[] PORTAL_INTERIOR = new Vec3i[]{
            new Vec3i(0, 0, 0),
            new Vec3i(0, 1, 0),
            new Vec3i(0, 2, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 1, 1),
            new Vec3i(0, 2, 1)
    };
    private static final Vec3i WATER_SOURCE_ORIGIN = new Vec3i(1, 3, 0);
    private final TimerGame lavaSearchTimer = new TimerGame(5);
    private final adris.altoclef.tasksystem.Task collectLavaTask = TaskCatalogue.getItemTask(Items.LAVA_BUCKET, 1);
    private final TimerGame placeLavaWeCanBreakAgainTimer = new TimerGame(5);
    private final TimerGame specialBottomCaseCloserTimer = new TimerGame(10);
    private final TimerGame specialBottomCaseCloserTimerForcePlace = new TimerGame(5);
    // Corresponds to the LEFT most side of where the player will stand on the portal.
    private BlockPos portalOrigin = null;
    private boolean isPlacingLiquid;
    private boolean portalFrameBuilt;
    private BlockPos destroyTarget = null;
    private boolean firstSearch = false;

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        isPlacingLiquid = false;
        portalFrameBuilt = false;
        mod.getBehaviour().push();
        //mod.getConfigState().setAllowWalkThroughFlowingWater(true);
        // Avoid breaking frame.
        mod.getBehaviour().avoidBlockBreaking((block) -> {
            if (portalOrigin != null) {

                for (Vec3i framePosRelative : PORTAL_CONSTRUCTION_FRAME) {
                    BlockPos framePos = portalOrigin.add(framePosRelative);
                    if (block.equals(framePos)) return true;
                }
                // If we're the water source block...
                if (block.equals(portalOrigin.add(WATER_SOURCE_ORIGIN))) {
                    if (MinecraftClient.getInstance().world.getBlockState(block).getBlock() == Blocks.WATER)
                        return true;
                }
            }
            return false;
        });

        lavaSearchTimer.reset();
        firstSearch = true;

    }

    @Override
    protected adris.altoclef.tasksystem.Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // Pre-affirmed thing
        mod.getBehaviour().setAllowWalkThroughFlowingWater(false);

        // 如果没有桶，获取一个。
        if (!mod.getItemStorage().hasItem(Items.BUCKET) && !mod.getItemStorage().hasItem(Items.WATER_BUCKET) && !mod.getItemStorage().hasItem(Items.LAVA_BUCKET)) {
            setDebugState("获取桶");
            return TaskCatalogue.getItemTask(Items.BUCKET, 1);
        }

        // 如果没有打火石和铁，获取一个
        if (!mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL)) {
            setDebugState("获取打火石和铁");
            return TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
        }

        boolean needsToLookForPortal = portalOrigin == null;
        if (needsToLookForPortal) {
            if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                setDebugState("获取水桶");
                return TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
            }

            boolean foundSpot = false;

            if (firstSearch || lavaSearchTimer.elapsed()) {
                firstSearch = false;
                lavaSearchTimer.reset();
                Debug.logMessage("(搜索附近有可建造传送门位置的熔岩湖...)");
                BlockPos lavaPos = findLavaLake(mod, mod.getPlayer().getBlockPos());
                if (lavaPos != null) {
                    // 我们有一个熔岩湖，设置我们的传送门原点！
                    BlockPos foundPortalRegion = getPortalableRegion(lavaPos, mod.getPlayer().getBlockPos(), new Vec3i(-1, 0, 0), PORTALABLE_REGION_SIZE, 20);
                    if (foundPortalRegion == null) {
                        Debug.logWarning("未能找到附近的可建造传送门区域。考虑增加搜索超时范围");
                    } else {
                        portalOrigin = foundPortalRegion.add(PORTAL_ORIGIN_RELATIVE_TO_REGION);
                        foundSpot = true;
                    }
                } else {
                    Debug.logMessage("(未找到熔岩湖)");
                }
            }

            if (!foundSpot) {
                setDebugState("(超时: 寻找熔岩湖)");
                return new TimeoutWanderTask(100);
            }
        }

        // 现在... 建造基础

        if (!portalFrameBuilt) {
            BlockPos requiredFrame = getRequiredFrameLeft();
            if (requiredFrame != null) {
                setDebugState("创建构造框架");
                return new PlaceStructureBlockTask(requiredFrame);
            }
        }

        // 清理位置
        if (!portalFrameBuilt && !isPlacingLiquid) {
            BlockPos toDestroy = getPortalRegionUnclearedBlock();
            if (toDestroy != null) {
                setDebugState("清理传送门区域");
                placeLavaWeCanBreakAgainTimer.reset();
                destroyTarget = toDestroy;
                return new DestroyBlockTask(toDestroy);//new ClearRegionTask(getPortalRegionCorner(), getPortalRegionCorner().add(PORTALABLE_REGION_SIZE));
            }
        }

        // 放置我们的水源
        if (!portalFrameBuilt) {
            BlockPos waterSourcePos = portalOrigin.add(WATER_SOURCE_ORIGIN);
            if (MinecraftClient.getInstance().world.getBlockState(waterSourcePos).getBlock() != Blocks.WATER) {
                if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                    setDebugState("获取水桶");
                    return TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
                }
                setDebugState("放置水: " + waterSourcePos);
                isPlacingLiquid = true;
                // 放置水
                // 南方向对应+z
                Direction placeWaterFrom = Direction.SOUTH;
                return new InteractWithBlockTask(new ItemTarget(Items.WATER_BUCKET, 1), placeWaterFrom, waterSourcePos.offset(placeWaterFrom.getOpposite()), true);
            }
        }
        //_isPlacingLiquid = false;


        // 放置熔岩
        for (LavaTarget lavaTarget : PORTAL_FRAME_LAVA) {
            //mod.getConfigState().setAllowWalkThroughFlowingWater(true);
            if (!lavaTarget.isSatisfied(portalOrigin)) {

                // 如果没有熔岩桶，获取一个。
                if (!mod.getItemStorage().hasItem(Items.LAVA_BUCKET)) {
                    setDebugState("获取熔岩桶");
                    isPlacingLiquid = true;
                    return collectLavaTask;
                }

                if (placeLavaWeCanBreakAgainTimer.elapsed()) {
                    isPlacingLiquid = false;
                    placeLavaWeCanBreakAgainTimer.reset();
                }
                portalFrameBuilt = false;
                // 穿过水到达底部，我们必须到达那里以进一步保证放置。
                mod.getBehaviour().setAllowWalkThroughFlowingWater(lavaTarget.isBelow());

                // 特殊情况：如果我们放置在危险区域，靠近我们的基地
                if (lavaTarget.isBelow()) {
                    BlockPos posClose = portalOrigin.add(lavaTarget.where).add(-1,1,0);
                    // 如果我们不在那个点上，并且我们注册了继续争夺它，那就去吧。
                    if (!mod.getPlayer().getBlockPos().equals(posClose)) {
                        if (!specialBottomCaseCloserTimer.elapsed()) {
                            setDebugState("特殊情况：靠近底部熔岩以放置它。");
                            specialBottomCaseCloserTimerForcePlace.reset();
                            return new GetToBlockTask(posClose, false);
                        } else {
                            if (specialBottomCaseCloserTimerForcePlace.elapsed()) {
                                specialBottomCaseCloserTimer.reset();
                            }
                        }
                    }
                }

                isPlacingLiquid = true;
                setDebugState("放置黑曜石");
                return lavaTarget.placeTask(portalOrigin, lavaTarget.isBelow());
            }
        }
        mod.getBehaviour().setAllowWalkThroughFlowingWater(false);

        portalFrameBuilt = true;

        // 删除水源
        BlockPos waterSourcePos = portalOrigin.add(WATER_SOURCE_ORIGIN);
        BlockState waterSource = MinecraftClient.getInstance().world.getBlockState(waterSourcePos);
        if (waterSource.getBlock() == Blocks.WATER) {
            setDebugState("移除水源");

            return new ClearLiquidTask(waterSourcePos);
        }

        // 清理传送门内部
        for (Vec3i offs : PORTAL_INTERIOR) {
            BlockPos p = portalOrigin.add(offs);
            if (!MinecraftClient.getInstance().world.getBlockState(p).isAir()) {
                setDebugState("清理传送门内部");
                return new DestroyBlockTask(p);
            }
        }
        setDebugState("使用打火石点火");

        // 用打火石点火，宝贝
        return new InteractWithBlockTask(new ItemTarget(Items.FLINT_AND_STEEL, 1), Direction.UP, portalOrigin.down(), true);

        // Pick up water
        // Clear inner portal area
        // Flint and we're done.

        // If no portal position current:
        //      - Get water if we don't have it.
        //      - Timer. Run "findLavaLake (rename to SCAN) and find the nearest lava lake
        //      - If we found a lava lake, find a spot nearby for the portal that is big enough (figure out the size) and set the portal position to be
        //        the center of that.
        // Otherwise, we have a portal position and must begin grabbing lava

        // - Find lava lake/area with a lot of lava nearby
        // - Clear an area nearby (that doesn't have obsidian or lava in it, for now)
        // - Construct the speedrun structure (empty spot for the bottom, upside down L left, upside down T right, water flow that goes all the way down)
        // - Once structure is done (and the flowing water is all the way down), begin placing lava at each point in the portal. (Bonus: If the lava spreads even a little, grab it back and try again before abandoning this portal)
        // - Once the portal is constructed, pick up the original water source block. Wait for flowing water to no longer exist all the way down. (and have a timeout or something)
        // - Light portal.

    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(adris.altoclef.tasksystem.Task other) {
        return other instanceof ConstructNetherPortalSpeedrunTask;
    }

    @Override
    protected String toDebugString() {
        return "建造下界传送门（酷炫方式）";
    }


    // Scans to find the nearest lava lake (collection of lava bigger than 12 blocks)
    private BlockPos findLavaLake(AltoClef mod, BlockPos playerPos) {
        HashSet<BlockPos> alreadyExplored = new HashSet<>();

        double nearestSqDistance = Double.POSITIVE_INFINITY;
        BlockPos nearestLake = null;
        for (BlockPos pos : mod.getBlockScanner().getKnownLocations(Blocks.LAVA)) {
            if (alreadyExplored.contains(pos)) continue;
            double sqDist = playerPos.getSquaredDistance(pos);
            if (sqDist < nearestSqDistance) {
                int depth = getNumberOfBlocksAdjacent(alreadyExplored, pos);
                Debug.logMessage("Found with depth " + depth);
                if (depth >= 12) {
                    nearestSqDistance = sqDist;
                    nearestLake = pos;
                }
            }
        }

        return nearestLake;
    }

    // Used to flood-scan for blocks of lava.
    private int getNumberOfBlocksAdjacent(HashSet<BlockPos> alreadyExplored, BlockPos origin) {
        // Base case: We already explored this one
        if (alreadyExplored.contains(origin)) return 0;
        alreadyExplored.add(origin);

        // Base case: We hit a non-full lava block.
        BlockState s = MinecraftClient.getInstance().world.getBlockState(origin);
        if (s.getBlock() != Blocks.LAVA) {
            return 0;
        } else {
            // We may not be a full lava block
            if (!s.getFluidState().isStill()) return 0;
            int level = s.getFluidState().getLevel();
            //Debug.logMessage("TEST LEVEL: " + level + ", " + height);
            // Only accept FULL SOURCE BLOCKS
            if (level != 8) return 0;
        }

        BlockPos[] toCheck = new BlockPos[]{origin.north(), origin.south(), origin.east(), origin.west(), origin.up(), origin.down()};

        int bonus = 0;
        for (BlockPos check : toCheck) {
            // This block is new! Explore out from it.
            bonus += getNumberOfBlocksAdjacent(alreadyExplored, check);
        }

        return bonus + 1;
    }

    // Get a region that a portal can fit into
    private BlockPos getPortalableRegion(BlockPos lava, BlockPos playerPos, Vec3i sizeOffset, Vec3i sizeAllocation, int timeoutRange) {
        Vec3i[] directions = new Vec3i[]{new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)};

        double minDistanceToPlayer = Double.POSITIVE_INFINITY;
        BlockPos bestPos = null;

        for (Vec3i direction : directions) {

            // Inch along
            for (int offs = 1; offs < timeoutRange; ++offs) {

                Vec3i offset = new Vec3i(direction.getX() * offs, direction.getY() * offs, direction.getZ() * offs);

                boolean found = true;
                // check for collision with lava in box
                moveAlongLine:
                // We have an extra buffer to make sure we never break a block NEXT to lava.
                for (int dx = -1; dx < sizeAllocation.getX() + 1; ++dx) {
                    for (int dz = -1; dz < sizeAllocation.getZ() + 1; ++dz) {
                        for (int dy = -1; dy < sizeAllocation.getY(); ++dy) {
                            BlockPos toCheck = lava.add(offset).add(sizeOffset).add(dx,dy,dz);
                            BlockState state = MinecraftClient.getInstance().world.getBlockState(toCheck);
                            if (state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.BEDROCK) {
                                found = false;
                                break moveAlongLine;
                            }
                        }
                    }
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

    private BlockPos getPortalRegionUnclearedBlock() {
        if (destroyTarget != null) {
            BlockState state = MinecraftClient.getInstance().world.getBlockState(destroyTarget);
            Block block = state.getBlock();
            if (state.isAir() || block == Blocks.WATER) {
                destroyTarget = null;
            }
        }
        if (destroyTarget != null) return destroyTarget;
        // Region
        for (int dx = 0; dx < PORTALABLE_REGION_SIZE.getX(); ++dx) {
            for (int dz = 0; dz < PORTALABLE_REGION_SIZE.getZ(); ++dz) {
                for (int dy = 0; dy < PORTALABLE_REGION_SIZE.getY(); ++dy) {
                    BlockPos toCheck = getPortalRegionCorner().add(dx,dy,dz);
                    if (shouldBeDestroyed(toCheck)) return toCheck;
                }
            }
        }
        // Extra places
        for (Vec3i relativeToOrigin : PORTALABLE_REGION_EXTRA) {
            BlockPos toCheck = portalOrigin.add(relativeToOrigin);
            if (shouldBeDestroyed(toCheck)) return toCheck;
        }

        return null;
    }

    private boolean shouldBeDestroyed(BlockPos toCheck) {
        BlockState state = MinecraftClient.getInstance().world.getBlockState(toCheck);
        Block block = state.getBlock();

        // Ignore air
        if (state.isAir()) {
            return false;
        }

        // If it's water ignore it.
        if (block == Blocks.WATER) return false;

        // If we're supposed to have structures here, ignore.
        Vec3i relativeToOrigin = toCheck.subtract(portalOrigin);//new Vec3i(dx - PORTAL_ORIGIN_RELATIVE_TO_REGION.getX(), dy  - PORTAL_ORIGIN_RELATIVE_TO_REGION.getY(), dz - PORTAL_ORIGIN_RELATIVE_TO_REGION.getZ());
        boolean foundFrame = false;
        for (Vec3i framePos : PORTAL_CONSTRUCTION_FRAME) {
            if (framePos.equals(relativeToOrigin)) {
                return false;
            }
        }
        for (LavaTarget frame : PORTAL_FRAME_LAVA) {
            if (frame.where.equals(relativeToOrigin) && (block == Blocks.LAVA || block == Blocks.OBSIDIAN)) {
                return false;
            }
        }
        return true;
    }

    private BlockPos getRequiredFrameLeft() {
        for (Vec3i framePos : PORTAL_CONSTRUCTION_FRAME) {
            BlockPos worldPos = portalOrigin.add(framePos);
            if (!MinecraftClient.getInstance().world.getBlockState(worldPos).isSolidBlock(MinecraftClient.getInstance().world, worldPos)) {
                return worldPos;
            }
        }
        return null;
    }

    private BlockPos getPortalRegionCorner() {
        if (portalOrigin == null) return null;
        return portalOrigin.subtract(PORTAL_ORIGIN_RELATIVE_TO_REGION);
    }

    private static class LavaTarget {
        public Vec3i where;
        public Direction fromWhere;

        public LavaTarget(int dx, int dy, int dz, Direction fromWhere) {
            where = new Vec3i(dx, dy, dz);
            this.fromWhere = fromWhere;
        }

        public boolean isBelow() {
            return where.getY() == -1;
        }

        // Place lava at a point, but from a direction.
        private adris.altoclef.tasksystem.Task placeTask(BlockPos portalOrigin, boolean below) {
            BlockPos placeAt = portalOrigin.add(where);
            BlockPos placeOn = placeAt.offset(fromWhere.getOpposite());
            // Clear first
            BlockState b = MinecraftClient.getInstance().world.getBlockState(placeAt);
            if (!b.isAir() && b.getBlock() != Blocks.WATER) {
                return new DestroyBlockTask(placeAt);
            }

            // Place lava there
            return new InteractWithBlockTask(new ItemTarget(Items.LAVA_BUCKET, 1), fromWhere, placeOn, below);
        }

        private boolean isSatisfied(BlockPos portalOrigin) {
            Block b = MinecraftClient.getInstance().world.getBlockState(portalOrigin.add(where)).getBlock();
            return b == Blocks.OBSIDIAN || b == Blocks.LAVA;
        }
    }

}
