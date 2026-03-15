package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasks.resources.GetBuildingMaterialsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.function.Predicate;

// TODO:
// 10个传送门柱形成一个43方块的半径，但角度偏移/循环是随机的。
// 有一个内部的"cycle"值或其他东西来跟踪该循环的位置
// 通过在43方块半径内滚动搜索黑曜石并找到两个黑曜石点之间的"中点"
// 然后，在搭建柱子时，确保我们移动到这些区域之一（这样我们可以在不
// 风险撞击黑曜石塔的情况下向外移动）
/**
 * 等待龙并投掷末影珍珠任务类
 * 此任务负责等待末影龙停在柱子上并投掷末影珍珠
 */
public class WaitForDragonAndPearlTask extends Task {

    // 与传送门的距离，XZ方向
    private static final double XZ_RADIUS = 30;
    private static final double XZ_RADIUS_TOO_FAR = 38;
    // 搭建柱子的高度
    private static final int HEIGHT = 42; //增加高度因为这太低了

    private static final int CLOSE_ENOUGH_DISTANCE = 15;

    private final int Y_COORDINATE = 75;

    private static final double DRAGON_FIREBALL_TOO_CLOSE_RANGE = 40;
    private final Task buildingMaterialsTask = new GetBuildingMaterialsTask(HEIGHT + 10);
    boolean inCenter;
    private Task heightPillarTask; // 攀高柱子任务
    private Task throwPearlTask; // 投掷珍珠任务
    private BlockPos targetToPearl; // 投掷珍珠的目标位置
    private boolean dragonIsPerching; // 龙是否在柱子上停下
    // 为避免龙的吐息
    private Task pillarUpFurther; // 进一步搭建柱子的任务

    private boolean _hasPillar = false;

    /**
     * 设置退出传送门顶部位置
     * @param top 传送门顶部位置
     */
    public void setExitPortalTop(BlockPos top) {
        BlockPos actualTarget = top.down();
        if (!actualTarget.equals(targetToPearl)) {
            targetToPearl = actualTarget;
            throwPearlTask = new ThrowEnderPearlSimpleProjectileTask(actualTarget);
        }
    }

    /**
     * 设置停靠状态
     * @param perching 龙是否在停靠
     */
    public void setPerchState(boolean perching) {
        dragonIsPerching = perching;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        Optional<Entity> enderMen = mod.getEntityTracker().getClosestEntity(EndermanEntity.class);
        if (enderMen.isPresent() && (enderMen.get() instanceof EndermanEntity endermanEntity) &&
                endermanEntity.getTarget()==mod.getPlayer()) {
            setDebugState("击杀愤怒的末影人");
            Predicate<Entity> angry = entity -> endermanEntity.getTarget()==mod.getPlayer();
            return new KillEntitiesTask(angry, enderMen.get().getClass());
        }
        if (throwPearlTask != null && throwPearlTask.isActive() && !throwPearlTask.isFinished()) {
            setDebugState("投掷珍珠!");
            return throwPearlTask;
        }

        if (pillarUpFurther != null && pillarUpFurther.isActive() && !pillarUpFurther.isFinished() && (mod.getEntityTracker().getClosestEntity(AreaEffectCloudEntity.class).isPresent())) {

            Optional<Entity> cloud = mod.getEntityTracker().getClosestEntity(AreaEffectCloudEntity.class);

            if (cloud.isPresent() && cloud.get().isInRange(mod.getPlayer(), 4)) {
                setDebugState("进一步搭建柱子以避免龙的吐息");
                return pillarUpFurther;
            }

            Optional<Entity> fireball = mod.getEntityTracker().getClosestEntity(DragonFireballEntity.class);

            if (isFireballDangerous(mod, fireball)) {
                setDebugState("进一步搭建柱子以避免龙的吐息");
                return pillarUpFurther;
            }
        }

        if (!mod.getItemStorage().hasItem(Items.ENDER_PEARL) && inCenter) {
            setDebugState("首先获取末影珍珠。");
            return TaskCatalogue.getItemTask(Items.ENDER_PEARL, 1);
        }

        int minHeight = targetToPearl.getY() + HEIGHT - 3;

        int deltaY = minHeight - mod.getPlayer().getBlockPos().getY();
        if (StorageHelper.getBuildingMaterialCount() < Math.min(deltaY - 10, HEIGHT - 5) || buildingMaterialsTask.isActive() && !buildingMaterialsTask.isFinished()) {
            setDebugState("Collecting building materials...");
            return buildingMaterialsTask;
        }

        // 我们的投掷触发器是龙开始停靠。我们可以在任意距离投掷，我们仍然会这样做哈哈
        if (dragonIsPerching && canThrowPearl(mod)) {
            Debug.logMessage("投掷珍珠!!");
            return throwPearlTask;
        }
        if (mod.getPlayer().getBlockPos().getY() < minHeight) {
            if (mod.getEntityTracker().entityFound(entity ->
                    mod.getPlayer().getPos().isInRange(entity.getPos(), 4), AreaEffectCloudEntity.class)) {
                if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                        !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                    LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
                }
                return null;
            }
            if (heightPillarTask != null && heightPillarTask.isActive() && !heightPillarTask.isFinished()) {
                setDebugState("搭建柱子!");
                inCenter = true;
                if (mod.getEntityTracker().entityFound(EndCrystalEntity.class)) {
                    return new DoToClosestEntityTask(
                            (toDestroy) -> {
                                if (toDestroy.isInRange(mod.getPlayer(), 7)) {
                                    mod.getControllerExtras().attack(toDestroy);
                                }
                                if (mod.getPlayer().getBlockPos().getY() < minHeight) {
                                    return heightPillarTask;
                                } else {
                                    if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                                            !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                                        LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
                                    }
                                    return null;
                                }
                            },
                            EndCrystalEntity.class
                    );
                }
                return heightPillarTask;
            }
        } else {
            setDebugState("我们已经足够高了。");
            // 如果火球太近，向上移动
            Optional<Entity> dragonFireball = mod.getEntityTracker().getClosestEntity(DragonFireballEntity.class);
            if (dragonFireball.isPresent() && dragonFireball.get().isInRange(mod.getPlayer(), DRAGON_FIREBALL_TOO_CLOSE_RANGE) && LookHelper.cleanLineOfSight(mod.getPlayer(), dragonFireball.get().getPos(), DRAGON_FIREBALL_TOO_CLOSE_RANGE)) {
                pillarUpFurther = new GetToYTask(mod.getPlayer().getBlockY() + 5);
                Debug.logMessage("暂停");
                return pillarUpFurther;
            }
            if (mod.getEntityTracker().entityFound(EndCrystalEntity.class)) {
                return new DoToClosestEntityTask(
                        (toDestroy) -> {
                            if (toDestroy.isInRange(mod.getPlayer(), 7)) {
                                mod.getControllerExtras().attack(toDestroy);
                            }
                            if (mod.getPlayer().getBlockPos().getY() < minHeight) {
                                return heightPillarTask;
                            } else {
                                if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                                        !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                                    LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
                                }
                                return null;
                            }
                        },
                        EndCrystalEntity.class
                );
            }
            if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                    !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
            }
            return null;
        }
        if (!WorldHelper.inRangeXZ(mod.getPlayer(), targetToPearl, XZ_RADIUS_TOO_FAR) && mod.getPlayer().getPos().getY() < minHeight && !_hasPillar) {
            if (mod.getEntityTracker().entityFound(entity ->
                    mod.getPlayer().getPos().isInRange(entity.getPos(), 4), AreaEffectCloudEntity.class)) {
                if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                        !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                    LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
                }
                return null;
            }
            setDebugState("向内移动（太远了，可能会撞到柱子）");
            return new GetToXZTask(0, 0);
        }
        // 我们已经足够远了，搭建柱子！
        if (!_hasPillar) {
            _hasPillar = true;
        }
        heightPillarTask = new GetToBlockTask(new BlockPos(0, minHeight, Y_COORDINATE));
        return heightPillarTask;
    }

    /**
     * 基本上与LookHelper.cleanLineOfSight相同，但进行了编辑，使其具有较小的距离容差
     * @param mod AltoClef实例
     * @return 是否可以投掷珍珠
     */
    private boolean canThrowPearl(AltoClef mod) {
        Vec3d targetPosition = WorldHelper.toVec3d(targetToPearl.up());

        // 从实体的摄像机位置到目标位置执行射线投射，指定最大范围
        BlockHitResult hitResult = LookHelper.raycast(mod.getPlayer(), LookHelper.getCameraPos(mod.getPlayer()), targetPosition, 300);

        if (hitResult == null) {
            // 没有命中结果，视野清晰
            return true;
        } else {
            return switch (hitResult.getType()) {
                case MISS ->
                    // 错过了目标，视野清晰
                        true;
                case BLOCK ->
                    // 命中方块，检查是否与目标方块相同
                        hitResult.getBlockPos().isWithinDistance(targetToPearl.up(), 10);
                case ENTITY ->
                    // 命中实体，视野被阻挡
                        false;
            };
        }
    }

    /**
     * 检查火球是否危险
     * @param mod AltoClef实例
     * @param fireball 火球实体
     * @return 火球是否危险
     */
    private boolean isFireballDangerous(AltoClef mod, Optional<Entity> fireball) {
        if (fireball.isEmpty())
            return false;

        boolean fireballTooClose = fireball.get().isInRange(mod.getPlayer(), DRAGON_FIREBALL_TOO_CLOSE_RANGE);
        boolean fireballInSight = LookHelper.cleanLineOfSight(mod.getPlayer(), fireball.get().getPos(), DRAGON_FIREBALL_TOO_CLOSE_RANGE);

        return fireballTooClose && fireballInSight;
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof WaitForDragonAndPearlTask;
    }

    @Override
    public boolean isFinished() {
        return dragonIsPerching
                && ((throwPearlTask == null || (throwPearlTask.isActive() && throwPearlTask.isFinished()))
                || WorldHelper.inRangeXZ(AltoClef.getInstance().getPlayer(), targetToPearl, CLOSE_ENOUGH_DISTANCE));
    }

    @Override
    protected String toDebugString() {
        return "等待龙停靠+投掷珍珠";
    }
}