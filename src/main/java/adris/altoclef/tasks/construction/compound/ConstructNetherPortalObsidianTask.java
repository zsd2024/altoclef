package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.LinkedList;

/**
 * 使用黑曜石方块建造下界传送门。
 */
public class ConstructNetherPortalObsidianTask extends Task {

    // 这里和ConstructNetherPortalBucketTask之间存在一些代码重复...
    // 但由于它们高度交织/修改，要解耦并重新整合两者需要很长时间。

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

    private static final Vec3i PORTALABLE_REGION_SIZE = new Vec3i(3, 6, 6);

    private final TimerGame _areaSearchTimer = new TimerGame(5);

    private BlockPos origin;

    private BlockPos _destroyTarget;

    private static BlockPos getBuildableAreaNearby(AltoClef mod) {
        BlockPos checkOrigin = mod.getPlayer().getBlockPos();
        for (BlockPos toCheck : WorldHelper.scanRegion(checkOrigin, checkOrigin.add(PORTALABLE_REGION_SIZE))) {
            if (MinecraftClient.getInstance().world == null) {
                return null;
            }
            BlockState state = MinecraftClient.getInstance().world.getBlockState(toCheck);
            boolean validToWorld = (WorldHelper.canPlace(toCheck) || WorldHelper.canBreak(toCheck));
            if (!validToWorld || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BEDROCK) {
                return null;
            }
        }
        return checkOrigin;
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        mod.getBehaviour().push();

        // Avoid breaking portal frame if we're obsidian.
        mod.getBehaviour().avoidBlockBreaking(block -> {
            if (origin != null) {
                // Don't break frame
                for (Vec3i framePosRelative : PORTAL_FRAME) {
                    BlockPos framePos = origin.add(framePosRelative);
                    if (block.equals(framePos)) {
                        return mod.getWorld().getBlockState(framePos).getBlock() == Blocks.OBSIDIAN;
                    }
                }
            }
            return false;
        });
        mod.getBehaviour().addProtectedItems(Items.FLINT_AND_STEEL);
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        if (origin != null) {
            if (mod.getWorld().getBlockState(origin.up()).getBlock() == Blocks.NETHER_PORTAL) {
                setDebugState("下界传送门建造完成。");
                mod.getBlockScanner().addBlock(Blocks.NETHER_PORTAL, origin.up());
                return null;
            }
        }
        int neededObsidian = 10;
        BlockPos placeTarget = null;
        if (origin != null) {
            for (Vec3i frameOffs : PORTAL_FRAME) {
                BlockPos framePos = origin.add(frameOffs);
                if (!mod.getBlockScanner().isBlockAtPosition(framePos, Blocks.OBSIDIAN)) {
                    placeTarget = framePos;
                    break;
                }
                neededObsidian--;
            }
        }

        // 如果没有黑曜石，获取一些。
        if (mod.getItemStorage().getItemCount(Items.OBSIDIAN) < neededObsidian) {
            setDebugState("获取黑曜石");
            return TaskCatalogue.getItemTask(Items.OBSIDIAN, neededObsidian);
        }

        // 寻找位置
        if (origin == null) {
            if (_areaSearchTimer.elapsed()) {
                _areaSearchTimer.reset();
                Debug.logMessage("(搜索附近可建造传送门的区域...)");
                origin = getBuildableAreaNearby(mod);
            }
            setDebugState("寻找可建造传送门的区域...");
            return new TimeoutWanderTask();
        }

        // 获取打火石和铁
        if (!mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL)) {
            setDebugState("获取打火石和铁");
            return TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
        }

        // Place frame
        if (placeTarget != null) {
            World world = mod.getWorld();

            if (surroundedByAir(world,placeTarget)) {
                LinkedList<BlockPos> queue = new LinkedList<>();
                queue.add(placeTarget);
                while (surroundedByAir(world, placeTarget)) {
                    BlockPos pos = queue.removeFirst();
                    
                    if (surroundedByAir(world,pos)) {
                        queue.add(pos.up());
                        queue.add(pos.down());
                        queue.add(pos.east());
                        queue.add(pos.west());
                        queue.add(pos.north());
                        queue.add(pos.south());
                    } else {
                        return new PlaceStructureBlockTask(pos);
                    }
                }
                
                mod.logWarning("未找到可放置黑曜石的方块");
            }

            if (!world.getBlockState(placeTarget).isAir() && !world.getBlockState(placeTarget).getBlock().equals(Blocks.OBSIDIAN)) {
                return new DestroyBlockTask(placeTarget);
            }
            setDebugState("放置框架...");
            return new PlaceBlockTask(placeTarget, Blocks.OBSIDIAN);
        }

        // 清理中间
        if (_destroyTarget != null && !WorldHelper.isAir(_destroyTarget)) {
            return new DestroyBlockTask(_destroyTarget);
        }
        for (Vec3i middleOffs : PORTAL_INTERIOR) {
            BlockPos middlePos = origin.add(middleOffs);
            if (!WorldHelper.isAir(middlePos)) {
                _destroyTarget = middlePos;
                return new DestroyBlockTask(_destroyTarget);
            }
        }
        // 用打火石点火
        return new InteractWithBlockTask(new ItemTarget(Items.FLINT_AND_STEEL, 1), Direction.UP, origin.down(), true);
    }

    private boolean surroundedByAir(World world, BlockPos pos) {
        return world.getBlockState(pos.west()).isAir() && world.getBlockState(pos.south()).isAir() && world.getBlockState(pos.east()).isAir() &&
                world.getBlockState(pos.up()).isAir() && world.getBlockState(pos.down()).isAir() && world.getBlockState(pos.north()).isAir();
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof ConstructNetherPortalObsidianTask;
    }

    @Override
    protected String toDebugString() {
        return "使用黑曜石建造下界传送门";
    }
}
