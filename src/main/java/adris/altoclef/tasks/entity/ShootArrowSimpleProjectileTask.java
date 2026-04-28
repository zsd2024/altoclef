package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

/**
 * 射箭投射物任务类
 * 用于向指定目标实体射箭的自动化任务
 */
public class ShootArrowSimpleProjectileTask extends Task {

    private final Entity target; // 目标实体
    private boolean shooting = false; // 是否正在射击
    private boolean shot = false; // 是否已经射出箭

    private final TimerGame shotTimer = new TimerGame(1); // 射击计时器

    /**
     * 构造函数
     * 
     * @param target 要射击的目标实体
     */
    public ShootArrowSimpleProjectileTask(Entity target) {
        this.target = target;
    }

    @Override
    protected void onStart() {
        shooting = false;
    }

    /**
     * 计算投掷视角的方法
     * 根据弓的充能程度和目标位置计算最佳射击角度
     * 
     * @param mod AltoClef主实例
     * @param target 目标实体
     * @return 计算出的旋转角度
     */
    private static Rotation calculateThrowLook(AltoClef mod, Entity target) {
        // 基于弓充能计算速度
        float velocity = (mod.getPlayer().getItemUseTime() - mod.getPlayer().getItemUseTimeLeft()) / 20f;
        velocity = (velocity * velocity + velocity * 2) / 3;
        if (velocity > 1) velocity = 1;

        // 获取目标中心位置
        Vec3d targetCenter = target.getBoundingBox().getCenter();

        double posX = targetCenter.getX();
        double posY = targetCenter.getY();
        double posZ = targetCenter.getZ();

        // 调整命中框高度
        posY -= 1.9f - target.getHeight();

        double relativeX = posX - mod.getPlayer().getX();
        double relativeY = posY - mod.getPlayer().getY();
        double relativeZ = posZ - mod.getPlayer().getZ();

        // 计算俯仰角
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        final float g = 0.006f;
        float velocitySq = velocity * velocity;
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))) / (g * hDistance)));

        // 设置玩家旋转角度
        if (Float.isNaN(pitch)) {
            return new Rotation(target.getYaw(), target.getPitch());
        } else {
            return new Rotation(Vec3dToYaw(mod, new Vec3d(posX, posY, posZ)), pitch);
        }
    }

    /**
     * 将Vec3d向量转换为偏航角(Yaw)
     * 
     * @param mod AltoClef主实例
     * @param vec 目标位置向量
     * @return 计算出的偏航角
     */
    private static float Vec3dToYaw(AltoClef mod, Vec3d vec) {
        return (mod.getPlayer().getYaw() +
                MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(vec.getZ() - mod.getPlayer().getZ(), vec.getX() - mod.getPlayer().getX())) - 90f - mod.getPlayer().getYaw()));
    }

    /**
     * 每帧执行的任务逻辑
     * 处理射击过程中的各种状态检查和操作
     * 
     * @return 返回null表示任务继续执行
     */
    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        setDebugState("正在射击投射物");
        List<Item> requiredArrows = Arrays.asList(Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW);

        // 检查是否拥有弓和箭
        if (!(mod.getItemStorage().hasItem(Items.BOW) &&
                requiredArrows.stream().anyMatch(mod.getItemStorage()::hasItem))) {
            Debug.logMessage("缺少必要物品，停止任务。");
            return null;
        }

        Rotation lookTarget = calculateThrowLook(mod, target);
        LookHelper.lookAt(lookTarget);

        // 检查是否正在持弓
        boolean charged = mod.getPlayer().getItemUseTime() > 20 && mod.getPlayer().getActiveItem().getItem() == Items.BOW;

        mod.getSlotHandler().forceEquipItem(Items.BOW);

        // 开始拉弓
        if (LookHelper.isLookingAt(mod, lookTarget) && !shooting) {
            mod.getInputControls().hold(Input.CLICK_RIGHT);
            shooting = true;
            shotTimer.reset();
        }
        // 射出箭矢
        if (shooting && charged) {
            List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
            // 如果已有属于我们的箭正在飞行，则不要再次射击
            // 防止向同一目标射出多支箭
            for (ArrowEntity arrow : arrows) {
                if (arrow.getOwner() == mod.getPlayer()) {
                    Vec3d velocity = arrow.getVelocity();
                    Vec3d delta = target.getPos().subtract(arrow.getPos());
                    boolean isMovingTowardsTarget = velocity.dotProduct(delta) > 0;
                    if (isMovingTowardsTarget) {
                        return null;
                    }
                }
            }

            mod.getInputControls().release(Input.CLICK_RIGHT); // 释放箭矢
            shot = true;
        }
        return null;
    }

    /**
     * 任务停止时的清理方法
     * 释放右键输入，确保不会一直拉弓
     * 
     * @param interruptTask 中断任务
     */
    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getInputControls().release(Input.CLICK_RIGHT);
    }

    /**
     * 检查任务是否完成
     * 
     * @return 如果已经射出箭则返回true
     */
    @Override
    public boolean isFinished() {
        return shot;
    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "向" + target.getType().getTranslationKey() + "射箭";
    }
}

    @Override
    protected void onStart() {
        shooting = false;
    }

    private static Rotation calculateThrowLook(AltoClef mod, Entity target) {
        // Velocity based on bow charge.
        float velocity = (mod.getPlayer().getItemUseTime() - mod.getPlayer().getItemUseTimeLeft()) / 20f;
        velocity = (velocity * velocity + velocity * 2) / 3;
        if (velocity > 1) velocity = 1;

        // Find the position of the center
        Vec3d targetCenter = target.getBoundingBox().getCenter();

        double posX = targetCenter.getX();
        double posY = targetCenter.getY();
        double posZ = targetCenter.getZ();

        // Adjusting for hitbox heights
        posY -= 1.9f - target.getHeight();

        double relativeX = posX - mod.getPlayer().getX();
        double relativeY = posY - mod.getPlayer().getY();
        double relativeZ = posZ - mod.getPlayer().getZ();

        // Calculate the pitch
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        final float g = 0.006f;
        float velocitySq = velocity * velocity;
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))) / (g * hDistance)));

        // Set player rotation
        if (Float.isNaN(pitch)) {
            return new Rotation(target.getYaw(), target.getPitch());
        } else {
            return new Rotation(Vec3dToYaw(mod, new Vec3d(posX, posY, posZ)), pitch);
        }
    }

    private static float Vec3dToYaw(AltoClef mod, Vec3d vec) {
        return (mod.getPlayer().getYaw() +
                MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(vec.getZ() - mod.getPlayer().getZ(), vec.getX() - mod.getPlayer().getX())) - 90f - mod.getPlayer().getYaw()));
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        setDebugState("Shooting projectile");
        List<Item> requiredArrows = Arrays.asList(Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW);

        if (!(mod.getItemStorage().hasItem(Items.BOW) &&
                requiredArrows.stream().anyMatch(mod.getItemStorage()::hasItem))) {
            Debug.logMessage("Missing items, stopping.");
            return null;
        }

        Rotation lookTarget = calculateThrowLook(mod, target);
        LookHelper.lookAt(lookTarget);

        // check if we are holding a bow
        boolean charged = mod.getPlayer().getItemUseTime() > 20 && mod.getPlayer().getActiveItem().getItem() == Items.BOW;

        mod.getSlotHandler().forceEquipItem(Items.BOW);

        if (LookHelper.isLookingAt(mod, lookTarget) && !shooting) {
            mod.getInputControls().hold(Input.CLICK_RIGHT);
            shooting = true;
            shotTimer.reset();
        }
        if (shooting && charged) {
            List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
            // If any of the arrows belong to us and are moving, do not shoot yet
            // Prevents from shooting multiple arrows to the same target
            for (ArrowEntity arrow : arrows) {
                if (arrow.getOwner() == mod.getPlayer()) {
                    Vec3d velocity = arrow.getVelocity();
                    Vec3d delta = target.getPos().subtract(arrow.getPos());
                    boolean isMovingTowardsTarget = velocity.dotProduct(delta) > 0;
                    if (isMovingTowardsTarget) {
                        return null;
                    }
                }
            }

            mod.getInputControls().release(Input.CLICK_RIGHT); // Release the arrow
            shot = true;
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getInputControls().release(Input.CLICK_RIGHT);
    }

    @Override
    public boolean isFinished() {
        return shot;
    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Shooting arrow at " + target.getType().getTranslationKey();
    }
}