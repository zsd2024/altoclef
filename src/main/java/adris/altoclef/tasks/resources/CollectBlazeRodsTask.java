package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PutOutFireTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 收集烈焰棒任务
 * 用于前往下界要塞击杀烈焰人以获取烈焰棒，常用于酿造台和炼药
 */
public class CollectBlazeRodsTask extends ResourceTask {

    private static final double SPAWNER_BLAZE_RADIUS = 32; // 刷怪笼周围烈焰人有效半径
    private static final double TOO_LITTLE_HEALTH_BLAZE = 10; // 血量过低时避免战斗的阈值
    private static final int TOO_MANY_BLAZES = 5; // 烈焰人过多时避免战斗的阈值
    private final int _count; // 目标烈焰棒数量
    private final Task _searcher = new SearchChunkForBlockTask(Blocks.NETHER_BRICKS); // 搜索下界砖块以定位要塞

    // Why was this here???
    //private Entity _toKill;
    private BlockPos _foundBlazeSpawner = null; // 找到的烈焰人刷怪笼位置

    public CollectBlazeRodsTask(int count) {
        super(Items.BLAZE_ROD, count);
        _count = count;
    }

    /**
     * 检查实体是否悬浮在岩浆上方或位置过高
     * @param mod AltoClef实例
     * @param entity 要检查的实体
     * @return 如果实体悬浮在岩浆上方或位置过高则返回true
     */
    private static boolean isHoveringAboveLavaOrTooHigh(AltoClef mod, Entity entity) {
        int MAX_HEIGHT = 11;
        for (BlockPos check = entity.getBlockPos(); entity.getBlockPos().getY() - check.getY() < MAX_HEIGHT; check = check.down()) {
            if (mod.getWorld().getBlockState(check).getBlock() == Blocks.LAVA) return true;
            if (WorldHelper.isSolidBlock(check)) return false;
        }
        return true;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 必须前往下界
        if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
            setDebugState("前往下界");
            return new DefaultGoToDimensionTask(Dimension.NETHER);
        }

        Optional<Entity> toKill = Optional.empty();
        // 如果有烈焰人，击杀它
        if (mod.getEntityTracker().entityFound(BlazeEntity.class)) {
            toKill = mod.getEntityTracker().getClosestEntity(BlazeEntity.class);
            if (toKill.isPresent()) {
                if (mod.getPlayer().getHealth() <= TOO_LITTLE_HEALTH_BLAZE &&
                        mod.getEntityTracker().getTrackedEntities(BlazeEntity.class).size() >= TOO_MANY_BLAZES) {
                    setDebugState("附近烈焰人过多，正在逃跑。");
                    return new RunAwayFromHostilesTask(15 * 2, true);
                }
            }

            if (_foundBlazeSpawner != null && toKill.isPresent()) {
                Entity kill = toKill.get();
                Vec3d nearest = kill.getPos();

                double sqDistanceToPlayer = nearest.squaredDistanceTo(mod.getPlayer().getPos());//_foundBlazeSpawner.getX(), _foundBlazeSpawner.getY(), _foundBlazeSpawner.getZ());
                // 忽略距离过远的烈焰人
                if (sqDistanceToPlayer > SPAWNER_BLAZE_RADIUS * SPAWNER_BLAZE_RADIUS) {
                    // 如果烈焰人能看到我们，需要处理
                    BlockHitResult hit = mod.getWorld().raycast(new RaycastContext(mod.getPlayer().getCameraPosVec(1.0F), kill.getCameraPosVec(1.0F), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mod.getPlayer()));
                    if (hit != null && BlockPosVer.getSquaredDistance(hit.getBlockPos(),mod.getPlayer().getPos()) < sqDistanceToPlayer) {
                        toKill = Optional.empty();
                    }
                }
            }
        }
        // 如果有可击杀的烈焰人且它活着且位置安全，则击杀它
        if (toKill.isPresent() && toKill.get().isAlive() && !isHoveringAboveLavaOrTooHigh(mod, toKill.get())) {
            setDebugState("击杀烈焰人");
            Predicate<Entity> safeToPursue = entity -> !isHoveringAboveLavaOrTooHigh(mod, entity);
            return new KillEntitiesTask(safeToPursue, toKill.get().getClass());
        }


        // 如果烈焰人刷怪笼无效
        if (_foundBlazeSpawner != null && mod.getChunkTracker().isChunkLoaded(_foundBlazeSpawner) && !isValidBlazeSpawner(mod, _foundBlazeSpawner)) {
            Debug.logMessage("烈焰人刷怪笼在 " + _foundBlazeSpawner + " 距离过远或无效。重新搜索。");
            _foundBlazeSpawner = null;
        }

        // 如果我们有烈焰人刷怪笼，靠近它
        if (_foundBlazeSpawner != null) {
            if (!_foundBlazeSpawner.isWithinDistance(mod.getPlayer().getPos(), 4)) {
                setDebugState("前往烈焰人刷怪笼");
                return new GetToBlockTask(_foundBlazeSpawner.up(), false);
            } else {

                // 扑灭可能干扰我们的火
                Optional<BlockPos> nearestFire = mod.getBlockScanner().getNearestWithinRange(_foundBlazeSpawner, 5, Blocks.FIRE);
                if (nearestFire.isPresent()) {
                    setDebugState("清理刷怪笼周围的火以防止烈焰棒丢失。");
                    return new PutOutFireTask(nearestFire.get());
                }

                setDebugState("在烈焰人刷怪笼附近等待烈焰人生成");
                return null;
            }
        } else {
            // 搜索烈焰人刷怪笼
            Optional<BlockPos> pos = mod.getBlockScanner().getNearestBlock(blockPos->isValidBlazeSpawner(mod, blockPos),Blocks.SPAWNER);

            pos.ifPresent(blockPos -> _foundBlazeSpawner = blockPos);
        }

        // 我们需要找到要塞
        setDebugState("搜索要塞/在要塞周围移动");
        return _searcher;
    }

    /**
     * 检查刷怪笼是否为有效的烈焰人刷怪笼
     * @param mod AltoClef实例
     * @param pos 刷怪笼位置
     * @return 如果是有效的烈焰人刷怪笼则返回true
     */
    private boolean isValidBlazeSpawner(AltoClef mod, BlockPos pos) {
        if (!mod.getChunkTracker().isChunkLoaded(pos)) {
            // 如果区块未加载，前往它。除非距离过远。
            return false;
            //return pos.isWithinDistance(mod.getPlayer().getPos(),3000);
        }
        return WorldHelper.getSpawnerEntity(pos) instanceof BlazeEntity;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务停止时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectBlazeRodsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集烈焰棒 - "+ AltoClef.getInstance().getItemStorage().getItemCount(Items.BLAZE_ROD)+"/"+_count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }
}
