package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import //#if MC <= 11605
//$$ import net.minecraft.util.math.Direction;
//#endif

import java.util.Optional;

/**
 * 建造铁傀儡任务类
 * 该任务负责建造一个铁傀儡，需要4个铁块和1个雕刻南瓜来完成
 */
public class ConstructIronGolemTask extends Task {
    // 铁傀儡建造位置
    private BlockPos position;
    // 标识是否可以完成建造
    private boolean canBeFinished = false;

    public ConstructIronGolemTask() {

    }

    public ConstructIronGolemTask(BlockPos pos) {
        position = pos;
    }

    @Override
    protected void onStart() {
        // 开始任务时设置行为保护
        AltoClef.getInstance().getBehaviour().push();
        // 保护铁块和雕刻南瓜不被使用
        AltoClef.getInstance().getBehaviour().addProtectedItems(Items.IRON_BLOCK, Items.CARVED_PUMPKIN);
        // 设置避免破坏铁块
        AltoClef.getInstance().getClientBaritoneSettings().blocksToAvoidBreaking.value.add(Blocks.IRON_BLOCK);
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 检查是否拥有足够的材料
        if (!StorageHelper.itemTargetsMetInventory(golemMaterials(mod))) {
            setDebugState("获取铁傀儡建造材料");
            return new CataloguedResourceTask(golemMaterials(mod));
        }
        if (position == null) {
            for (BlockPos pos : WorldHelper.scanRegion(
                    new BlockPos(mod.getPlayer().getBlockX(), 64, mod.getPlayer().getBlockZ()),
                    new BlockPos(mod.getPlayer().getBlockX(), 128, mod.getPlayer().getBlockZ()))) {
                if (mod.getWorld().getBlockState(pos).getBlock() == Blocks.AIR) {
                    position = pos;
                    break;
                }
            }
            if (position == null) {
                position = mod.getPlayer().getBlockPos();
            }
        }
        if (!WorldHelper.isBlock(position, Blocks.IRON_BLOCK)) {
            if (!WorldHelper.isBlock(position, Blocks.AIR)) {
                setDebugState("清除底部铁块位置上的障碍物");
                return new DestroyBlockTask(position);
            }
            setDebugState("放置底部铁块");
            return new PlaceBlockTask(position, Blocks.IRON_BLOCK);
        }
//        mod.getPlayer().getServer().getPlayerManager().getPlayer("camelCasedSnivy").getAdvancementTracker()
        if (!WorldHelper.isBlock(position.up(), Blocks.IRON_BLOCK)) {
            if (!WorldHelper.isBlock(position.up(), Blocks.AIR)) {
                setDebugState("清除中间铁块位置上的障碍物");
                return new DestroyBlockTask(position.up());
            }
            setDebugState("放置中间铁块");
            return new PlaceBlockTask(position.up(), Blocks.IRON_BLOCK);
        }
        if (!WorldHelper.isBlock(position.up().east(), Blocks.IRON_BLOCK)) {
            if (!WorldHelper.isBlock(position.up().east(), Blocks.AIR)) {
                setDebugState("清除东侧铁块位置上的障碍物");
                return new DestroyBlockTask(position.up().east());
            }
            setDebugState("放置东侧铁块");
            return new PlaceBlockTask(position.up().east(), Blocks.IRON_BLOCK);
        }
        if (!WorldHelper.isBlock(position.up().west(), Blocks.IRON_BLOCK)) {
            if (!WorldHelper.isBlock(position.up().west(), Blocks.AIR)) {
                setDebugState("清除西侧铁块位置上的障碍物");
                return new DestroyBlockTask(position.up().west());
            }
            setDebugState("放置西侧铁块");
            return new PlaceBlockTask(position.up().west(), Blocks.IRON_BLOCK);
        }
        if (!WorldHelper.isBlock(position.east(), Blocks.AIR)) {
            setDebugState("清理东侧区域...");
            return new DestroyBlockTask(position.east());
        }
        if (!WorldHelper.isBlock(position.west(), Blocks.AIR)) {
            setDebugState("清理西侧区域...");
            return new DestroyBlockTask(position.west());
        }
        if (!WorldHelper.isBlock(position.up(2), Blocks.AIR)) {
            setDebugState("清除南瓜位置上的障碍物");
            return new DestroyBlockTask(position.up(2));
        }
        canBeFinished = true;
        setDebugState("放置南瓜（头部）");
        return new PlaceBlockTask(position.up(2), Blocks.CARVED_PUMPKIN);
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 停止任务时移除保护设置
        AltoClef.getInstance().getClientBaritoneSettings().blocksToAvoidBreaking.value.remove(Blocks.IRON_BLOCK);
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof ConstructIronGolemTask;
    }

    @Override
    public boolean isFinished() {
        if (position == null) return false;
        Optional<Entity> closestIronGolem = AltoClef.getInstance().getEntityTracker().getClosestEntity(new Vec3d(position.getX(), position.getY(), position.getZ()), IronGolemEntity.class);
        return closestIronGolem.isPresent() && closestIronGolem.get().getBlockPos().isWithinDistance(position, 2) && canBeFinished;
    }

    @Override
    protected String toDebugString() {
        return "建造铁傀儡";
    }

    private int ironBlocksNeeded(AltoClef mod) {
        if (position == null) {
            return 4;
        }
        int needed = 0;
        if (mod.getWorld().getBlockState(position).getBlock() != Blocks.IRON_BLOCK)
            needed++;
        if (mod.getWorld().getBlockState(position.up().west()).getBlock() != Blocks.IRON_BLOCK)
            needed++;
        if (mod.getWorld().getBlockState(position.up().east()).getBlock() != Blocks.IRON_BLOCK)
            needed++;
        if (mod.getWorld().getBlockState(position.up()).getBlock() != Blocks.IRON_BLOCK)
            needed++;
        return needed;
    }

    private ItemTarget[] golemMaterials(AltoClef mod) {
        if (position == null || mod.getWorld().getBlockState(position.up(2)).getBlock() != Blocks.CARVED_PUMPKIN)
            return new ItemTarget[]{
                    new ItemTarget(Items.IRON_BLOCK, ironBlocksNeeded(mod)),
                    new ItemTarget(Items.CARVED_PUMPKIN, 1)
            };
        else return new ItemTarget[]{
                new ItemTarget(Items.IRON_BLOCK, ironBlocksNeeded(mod))
        };
    }
}
