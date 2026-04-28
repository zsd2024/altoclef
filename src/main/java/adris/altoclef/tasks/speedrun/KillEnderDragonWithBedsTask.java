package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.LandingApproachPhase;
import net.minecraft.entity.boss.dragon.phase.LandingPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

/**
 * 使用床击杀末影龙任务
 * 通过在末地传送门顶部放置床并引爆来击杀末影龙的自动化任务
 */
public class KillEnderDragonWithBedsTask extends Task {
    private final WaitForDragonAndPearlTask whenNotPerchingTask;
    TimerGame placeBedTimer = new TimerGame(0.6);
    TimerGame waiTimer = new TimerGame(0.3);
    TimerGame waitBeforePlaceTimer = new TimerGame(0.5);
    boolean waited = false;
    double prevDist = 100;
    private BlockPos endPortalTop;
    private Task freePortalTopTask = null;
    private Task placeObsidianTask = null;
    private boolean dragonDead = false;

    public KillEnderDragonWithBedsTask() {
        whenNotPerchingTask = new WaitForDragonAndPearlTask();
    }

    /**
     * 定位末地出口传送门顶部位置
     * @param mod AltoClef主实例
     * @return 末地传送门顶部的方块位置，如果未找到则返回null
     */
    public static BlockPos locateExitPortalTop(AltoClef mod) {
        if (!mod.getChunkTracker().isChunkLoaded(new BlockPos(0, 64, 0))) return null;
        int height = WorldHelper.getGroundHeight(0, 0, Blocks.BEDROCK);
        if (height != -1) return new BlockPos(0, height, 0);
        return null;
    }

    @Override
    protected void onStart() {
        // 不要阻挡我们的视野
        AltoClef.getInstance().getBehaviour().avoidBlockPlacing((pos) -> pos.getZ() == 0 && Math.abs(pos.getX()) < 5);
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        /*
            如果末影龙正在栖息：
                如果我们不在正确位置（XZ坐标）：
                    移动到正确位置（XZ坐标）
                如果没有床：
                    如果我们无法"够到"柱子顶部：
                        跳跃
                    放置一张床
                如果末影龙头部碰撞箱足够接近床：
                    右键点击床
            否则：
                // 执行"默认漫游"模式并避开龙息
         */
        if (endPortalTop == null) {
            endPortalTop = locateExitPortalTop(mod);
            if (endPortalTop != null) {
                whenNotPerchingTask.setExitPortalTop(endPortalTop);
            }
        }

        if (endPortalTop == null) {
            setDebugState("正在搜索末地传送门顶部。");
            return new GetToXZTask(0, 0);
        }

        BlockPos obsidianTarget = endPortalTop.up().offset(Direction.NORTH);
        if (!mod.getWorld().getBlockState(obsidianTarget).getBlock().equals(Blocks.OBSIDIAN)) {
            if (WorldHelper.inRangeXZ(mod.getPlayer().getPos(), new Vec3d(0, 0, 0), 10)) {
                if (placeObsidianTask == null) {
                    placeObsidianTask = new PlaceBlockTask(obsidianTarget, Blocks.OBSIDIAN);
                }
                return placeObsidianTask;
            } else {
                return new GetToXZTask(0, 0);
            }
        }
        BlockState stateAtPortal = mod.getWorld().getBlockState(endPortalTop.up());
        if (!stateAtPortal.isAir() && !stateAtPortal.getBlock().equals(Blocks.FIRE) &&
                !Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.BED)).toList().contains(stateAtPortal.getBlock())) {

            if (freePortalTopTask == null) {
                freePortalTopTask = new DestroyBlockTask(endPortalTop.up());
            }
            return freePortalTopTask;
        }


        if (dragonDead) {
            setDebugState("等待主世界传送门生成。");
            return new GetToBlockTask(endPortalTop.down(4).west());
        }

        if (!mod.getEntityTracker().entityFound(EnderDragonEntity.class) || dragonDead) {
            setDebugState("未找到末影龙。");

            if (!WorldHelper.inRangeXZ(mod.getPlayer(), endPortalTop, 1)) {
                setDebugState("前往末地传送门顶部" + endPortalTop.toString() + "。");
                return new GetToBlockTask(endPortalTop);
            }
        }
        List<EnderDragonEntity> dragons = mod.getEntityTracker().getTrackedEntities(EnderDragonEntity.class);
        for (EnderDragonEntity dragon : dragons) {
            Phase dragonPhase = dragon.getPhaseManager().getCurrent();

            if (dragonPhase.getType() == PhaseType.DYING) {
                Debug.logMessage("末影龙已死亡。");
                if (mod.getPlayer().getPitch() != -90) {
                    mod.getPlayer().setPitch(-90);
                }
                dragonDead = true;
                return null;
            }

            boolean perching = dragonPhase instanceof LandingPhase || dragonPhase instanceof LandingApproachPhase || dragonPhase.isSittingOrHovering();
            if (dragon.getY() < endPortalTop.getY() + 2) {
                // 末影龙已经栖息。
                perching = false;
            }

            whenNotPerchingTask.setPerchState(perching);
            // 当末影龙不处于栖息状态时...
            if (whenNotPerchingTask.isActive() && !whenNotPerchingTask.isFinished()) {
                setDebugState("末影龙未栖息，执行特殊行为...");
                return whenNotPerchingTask;
            }
            if (perching) {
                return performOneCycle(mod, dragon);
            }
        }
        mod.getFoodChain().shouldStop(false);
        // 开始我们的"非栖息状态任务"
        return whenNotPerchingTask;
    }

    /**
     * 执行一次完整的击杀循环
     * @param mod AltoClef主实例
     * @param dragon 末影龙实体
     * @return 下一个要执行的任务
     */
    private Task performOneCycle(AltoClef mod, EnderDragonEntity dragon) {
        mod.getFoodChain().shouldStop(true);
        if (mod.getInputControls().isHeldDown(Input.SNEAK)) {
            mod.getInputControls().release(Input.SNEAK);
        }
        // 不要让盾牌破坏我们的关键时刻 :3
        mod.getSlotHandler().forceEquipItemToOffhand(Items.AIR);

        BlockPos endPortalTop = KillEnderDragonWithBedsTask.locateExitPortalTop(mod).up();
        BlockPos obsidian = null;
        Direction dir = null;

        for (Direction direction : new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}) {
            if (mod.getWorld().getBlockState(endPortalTop.offset(direction)).getBlock().equals(Blocks.OBSIDIAN)) {
                obsidian = endPortalTop.offset(direction);
                dir = direction.getOpposite();
                break;
            }
        }

        if (dir == null) {
            mod.log("没有黑曜石？ :(");
            return null;
        }

        Direction offsetDir = dir.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.WEST;
        BlockPos targetBlock = endPortalTop.down(3).offset(offsetDir, 3).offset(dir);

        double d = distanceIgnoreY(WorldHelper.toVec3d(targetBlock), mod.getPlayer().getPos());
        if (d > 0.7 || mod.getPlayer().getBlockPos().down().getY() > endPortalTop.getY() - 4) {
            mod.log(d + "");
            return new GetToBlockTask(targetBlock);
        } else if (!waited) {
            waited = true;
            waitBeforePlaceTimer.reset();
        }
        if (!waitBeforePlaceTimer.elapsed()) {
            mod.log(waitBeforePlaceTimer.getDuration() + " 等待中...");
            return null;
        }

        LookHelper.lookAt(mod, obsidian, dir);

        BlockPos bedHead = WorldHelper.getBedHead(endPortalTop);
        mod.getSlotHandler().forceEquipItem(ItemHelper.BED);

        if (bedHead == null) {
            if (placeBedTimer.elapsed() && Math.abs(dragon.getY() - endPortalTop.getY()) < 10) {
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                waiTimer.reset();
            }
            return null;
        }
        if (!waiTimer.elapsed()) {
            return null;
        }

        // 这些数值大多是通过一些测试任意添加的，可能并非所有情况都需要测试
        // 但似乎效果相当不错，所以我宁愿不要改动它 :p
        Vec3d dragonHeadPos = dragon.head.getBoundingBox().getCenter();
        Vec3d bedHeadPos = WorldHelper.toVec3d(bedHead);

        double dist = dragonHeadPos.distanceTo(bedHeadPos);
        double distXZ = distanceIgnoreY(dragonHeadPos, bedHeadPos);

        EnderDragonPart body = dragon.getBodyParts()[2];

        double destroyDistance = Math.abs(body.getBoundingBox().getMin(Direction.Axis.Y) - bedHeadPos.getY());
        boolean tooClose = destroyDistance < 1.1;
        boolean skip = destroyDistance > 3 && dist > 4.5 && distXZ > 2.5;

        mod.log(destroyDistance + " : " + dist + " : " + distXZ);

        if ((dist < 1.5 || (prevDist < distXZ && destroyDistance < 4 && prevDist < 2.9)) || (destroyDistance < 2 && dist < 4)
                || (destroyDistance < 1.7 && dist < 4.5) || tooClose || (destroyDistance < 2.4 && distXZ < 3.7) || (destroyDistance < 3.5 && distXZ < 2.4)) {

            if (!skip) {
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                placeBedTimer.reset();
            }
        }

        prevDist = distXZ;
        return null;
    }

    /**
     * 计算两个向量在忽略Y轴情况下的距离
     * @param vec 第一个向量
     * @param vec1 第二个向量
     * @return 忽略Y轴的距离
     */
    public double distanceIgnoreY(Vec3d vec, Vec3d vec1) {
        double d = vec.x - vec1.x;
        double f = vec.z - vec1.z;
        return Math.sqrt(d * d + f * f);
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getFoodChain().shouldStop(false);
    }

    @Override
    public boolean isFinished() {
        return super.isFinished();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof KillEnderDragonWithBedsTask;
    }

    @Override
    protected String toDebugString() {
        return "用床击杀末影龙";
    }
}