package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Optional;

/**
 * 抽象实体操作任务 - 在保持距离的同时与实体进行交互。
 * <p>
 * 该交互是抽象的，具体实现由子类完成。
 */
public abstract class AbstractDoToEntityTask extends Task implements ITaskRequiresGrounded {
    /** 进度检查器，用于跟踪移动进度 */
    protected final MovementProgressChecker progress = new MovementProgressChecker();
    /** 需要保持的最小距离 */
    private final double maintainDistance;
    /** 战斗防护的下限范围 */
    private final double combatGuardLowerRange;
    /** 战斗防护力场的半径 */
    private final double combatGuardLowerFieldRadius;
    /** 游荡任务，用于在找不到目标时随机移动 */
    private TimeoutWanderTask wanderTask;

    /**
     * 构造函数，指定所有参数
     * @param maintainDistance 需要保持的距离
     * @param combatGuardLowerRange 战斗防护下限范围
     * @param combatGuardLowerFieldRadius 战斗防护力场半径
     */
    protected AbstractDoToEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        this.maintainDistance = maintainDistance;
        this.combatGuardLowerRange = combatGuardLowerRange;
        this.combatGuardLowerFieldRadius = combatGuardLowerFieldRadius;
    }

    /**
     * 构造函数，仅指定保持距离
     * @param maintainDistance 需要保持的距离
     */
    protected AbstractDoToEntityTask(double maintainDistance) {
        this(maintainDistance, 0, Double.POSITIVE_INFINITY);
    }

    /**
     * 构造函数，仅指定战斗防护参数
     * @param combatGuardLowerRange 战斗防护下限范围
     * @param combatGuardLowerFieldRadius 战斗防护力场半径
     */
    protected AbstractDoToEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        this(-1, combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        // 重置进度检查器
        progress.reset();
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            // 尝试将光标中的物品移动到玩家库存中合适的槽位
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                // 如果可以丢弃该物品，则直接丢弃
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // 如果光标槽位中的物品是垃圾，尝试丢弃
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            // 关闭当前打开的界面
            StorageHelper.closeScreen();
        } // 这种方式有点像胶带修复，但应该具有一定的未来兼容性
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果正在寻路，则重置进度检查器
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            progress.reset();
        }

        // 获取目标实体
        Optional<Entity> checkEntity = getEntityTarget(mod);


        // 哎呀（找不到目标实体）
        if (checkEntity.isEmpty()) {
            // 重置目标实体和力场
            mod.getMobDefenseChain().resetTargetEntity();
            mod.getMobDefenseChain().resetForceField();
        } else {
            // 设置目标实体
            mod.getMobDefenseChain().setTargetEntity(checkEntity.get());
        }
        if (checkEntity.isPresent()) {
            Entity entity = checkEntity.get();

            // 获取玩家的实体交互范围
            double playerReach = mod.getModSettings().getEntityReachRange();

            // TODO: 这基本上是无用的。
            EntityHitResult result = LookHelper.raycast(mod.getPlayer(), entity, playerReach);

            // 计算与实体的平方距离
            double sqDist = entity.squaredDistanceTo(mod.getPlayer());

            // 如果距离小于战斗防护下限范围，则设置力场范围
            if (sqDist < combatGuardLowerRange * combatGuardLowerRange) {
                mod.getMobDefenseChain().setForceFieldRange(combatGuardLowerFieldRadius);
            } else {
                // 否则重置力场
                mod.getMobDefenseChain().resetForceField();
            }

            // 如果未指定保持距离，默认为在交互范围内1个方块的距离
            double maintainDistance = this.maintainDistance >= 0 ? this.maintainDistance : playerReach - 1;

            // 判断是否太近
            boolean tooClose = sqDist < maintainDistance * maintainDistance;

            // 如果太近且没有活跃的自定义目标进程，则远离目标
            if (tooClose && !mod.getClientBaritone().getCustomGoalProcess().isActive()) {
                mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(new GoalRunAway(maintainDistance, entity.getBlockPos()));
            }

            // 检查是否满足交互条件
            if (mod.getControllerExtras().inRange(entity) && result != null &&
                    result.getType() == HitResult.Type.ENTITY && !mod.getFoodChain().needsToEat() &&
                    !mod.getMLGBucketChain().isFalling(mod) && mod.getMLGBucketChain().doneMLG() &&
                    !mod.getMLGBucketChain().isChorusFruiting() &&
                    mod.getClientBaritone().getPathingBehavior().isSafeToCancel() &&
                    mod.getPlayer().isOnGround()) {
                progress.reset();
                // 执行实体交互
                return onEntityInteract(mod, entity);
            } else if (!tooClose) {
                setDebugState("正在接近目标");
                if (!progress.check(mod)) {
                    progress.reset();
                    Debug.logMessage("无法到达目标，已将其加入黑名单。");
                    mod.getEntityTracker().requestEntityUnreachable(entity);
                }
                // 移动到目标
                return new GetToEntityTask(entity, maintainDistance);
            }
        }
        if (BeatMinecraftTask.isTaskRunning(mod,wanderTask)) {
            return wanderTask;
        }

        if (!mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
            return null;
        }
        wanderTask = new TimeoutWanderTask();
        return wanderTask;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof AbstractDoToEntityTask task) {
            if (!doubleCheck(task.maintainDistance, maintainDistance)) return false;
            if (!doubleCheck(task.combatGuardLowerFieldRadius, combatGuardLowerFieldRadius)) return false;
            if (!doubleCheck(task.combatGuardLowerRange, combatGuardLowerRange)) return false;
            return isSubEqual(task);
        }
        return false;
    }

    /**
     * 比较两个double值是否相等（考虑浮点数精度问题）
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean doubleCheck(double a, double b) {
        if (Double.isInfinite(a) == Double.isInfinite(b)) return true;
        return Math.abs(a - b) < 0.1;
    }

    /**
     * 子类需要实现的相等性检查方法
     */
    protected abstract boolean isSubEqual(AbstractDoToEntityTask other);

    /**
     * 子类需要实现的实体交互方法
     * @param mod AltoClef实例
     * @param entity 目标实体
     * @return 下一个要执行的任务
     */
    protected abstract Task onEntityInteract(AltoClef mod, Entity entity);

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef mod = AltoClef.getInstance();

        // 停止时重置目标实体和力场
        mod.getMobDefenseChain().setTargetEntity(null);
        mod.getMobDefenseChain().resetForceField();
    }

    /**
     * 获取目标实体
     * @param mod AltoClef实例
     * @return 目标实体的Optional包装
     */
    protected abstract Optional<Entity> getEntityTarget(AltoClef mod);

}
