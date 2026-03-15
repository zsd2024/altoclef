package adris.altoclef.control;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.versionedfields.Entities;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 击杀光环（战斗控制）
 * 控制和应用击杀光环，自动攻击附近的敌对实体
 */
public class KillAura {
    // 智能光环数据
    private final List<Entity> targets = new ArrayList<>();
    // 是否正在使用盾牌保护
    boolean shielding = false;
    // 力场范围
    private double forceFieldRange = Double.POSITIVE_INFINITY;
    // 强制攻击的目标实体
    private Entity forceHit = null;
    // 上一tick是否攻击过
    public boolean attackedLastTick = false;

        /**
     * 装备最佳武器
     * @param mod AltoClef主模块实例
     */
    public static void equipWeapon(AltoClef mod) {
        List<ItemStack> invStacks = mod.getItemStorage().getItemStacksPlayerInventory(true);
        if (!invStacks.isEmpty()) {
            float handDamage = Float.NEGATIVE_INFINITY;
            for (ItemStack invStack : invStacks) {
                if (invStack.getItem() instanceof SwordItem item) {
                    float itemDamage = item.getMaterial().getAttackDamage();
                    Item handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
                    if (handItem instanceof SwordItem handToolItem) {
                        handDamage = handToolItem.getMaterial().getAttackDamage();
                    }
                    if (itemDamage > handDamage) {
                        mod.getSlotHandler().forceEquipItem(item);
                    } else {
                        mod.getSlotHandler().forceEquipItem(handItem);
                    }
                }
            }
        }
    }

        /**
     * 每tick开始时的重置操作
     * 清空目标列表、强制攻击实体和攻击状态
     */
    public void tickStart() {
        targets.clear();
        forceHit = null;
        attackedLastTick = false;
    }

        /**
     * 应用光环到指定实体
     * @param entity 要应用光环的实体
     */
    public void applyAura(Entity entity) {
        targets.add(entity);
        // 总是攻击恶魂火球。
        if (entity instanceof FireballEntity) forceHit = entity;
    }

        /**
     * 设置力场范围
     * @param range 力场范围
     */
    public void setRange(double range) {
        forceFieldRange = range;
    }

        /**
     * 每tick结束时的执行操作
     * 处理攻击逻辑和盾牌保护
     * @param mod AltoClef主模块实例
     */
    public void tickEnd(AltoClef mod) {
        Optional<Entity> entities = targets.stream().min(StlHelper.compareValues(entity -> entity.squaredDistanceTo(mod.getPlayer())));
        if (entities.isPresent() &&
                !mod.getEntityTracker().entityFound(PotionEntity.class) &&
                (Double.isInfinite(forceFieldRange) || entities.get().squaredDistanceTo(mod.getPlayer()) < forceFieldRange * forceFieldRange ||
                        entities.get().squaredDistanceTo(mod.getPlayer()) < 40) &&
                !mod.getMLGBucketChain().isFalling(mod) && mod.getMLGBucketChain().doneMLG() &&
                !mod.getMLGBucketChain().isChorusFruiting()) {
            PlayerSlot offhandSlot = PlayerSlot.OFFHAND_SLOT;
            Item offhandItem = StorageHelper.getItemStackInSlot(offhandSlot).getItem();
            if (entities.get().getClass() != CreeperEntity.class && entities.get().getClass() != HoglinEntity.class &&
                    entities.get().getClass() != ZoglinEntity.class && entities.get().getClass() != Entities.WARDEN &&
                    entities.get().getClass() != WitherEntity.class
                    && (mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD))
                    && !mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem)
                    && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                LookHelper.lookAt(mod, entities.get().getEyePos());
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else if (!WorldHelper.isSurroundedByHostiles()) {
                    startShielding(mod);
                }
            }
            performDelayedAttack(mod);
        } else {
            stopShielding(mod);
        }
        // 在地图上运行力场
        switch (mod.getModSettings().getForceFieldStrategy()) {
            case FASTEST:
                performFastestAttack(mod);
                break;
            case SMART:
                // 总是攻击强制目标。（目前仅用于火球）
                if (forceHit != null) {
                    attack(mod, forceHit, true);
                    break;
                }

                if (!mod.getFoodChain().needsToEat() && !mod.getMLGBucketChain().isFalling(mod) &&
                        mod.getMLGBucketChain().doneMLG() && !mod.getMLGBucketChain().isChorusFruiting()) {
                    performDelayedAttack(mod);
                }
                break;
            case DELAY:
                performDelayedAttack(mod);
                break;
            case OFF:
                break;
        }
    }

        /**
     * 执行延迟攻击（等待攻击冷却后攻击）
     * @param mod AltoClef主模块实例
     */
    private void performDelayedAttack(AltoClef mod) {
        if (!mod.getFoodChain().needsToEat() && !mod.getMLGBucketChain().isFalling(mod) &&
                mod.getMLGBucketChain().doneMLG() && !mod.getMLGBucketChain().isChorusFruiting()) {
            if (forceHit != null) {
                attack(mod, forceHit, true);
            }
            // 等待攻击延迟
            if (targets.isEmpty()) {
                return;
            }

            Optional<Entity> toHit = targets.stream().min(StlHelper.compareValues(entity -> entity.squaredDistanceTo(mod.getPlayer())));

            if (mod.getPlayer() == null || mod.getPlayer().getAttackCooldownProgress(0) < 1) {
                return;
            }

            toHit.ifPresent(entity -> attack(mod, entity, true));
        }
    }

        /**
     * 执行最快攻击（立即攻击所有目标）
     * @param mod AltoClef主模块实例
     */
    private void performFastestAttack(AltoClef mod) {
        if (!mod.getFoodChain().needsToEat() && !mod.getMLGBucketChain().isFalling(mod) &&
                mod.getMLGBucketChain().doneMLG() && !mod.getMLGBucketChain().isChorusFruiting()) {
            // 只要可以就攻击
            for (Entity entity : targets) {
                attack(mod, entity);
            }
        }
    }

        /**
     * 攻击实体（不装备武器）
     * @param mod AltoClef主模块实例
     * @param entity 要攻击的实体
     */
    private void attack(AltoClef mod, Entity entity) {
        attack(mod, entity, false);
    }

        /**
     * 攻击实体
     * @param mod AltoClef主模块实例
     * @param entity 要攻击的实体
     * @param equipSword 是否装备剑
     */
    private void attack(AltoClef mod, Entity entity, boolean equipSword) {
        if (entity == null) return;
        if (!(entity instanceof FireballEntity)) {
            double xAim = entity.getX();
            double yAim = entity.getY() + (entity.getHeight() / 1.4);
            double zAim = entity.getZ();
            LookHelper.lookAt(mod, new Vec3d(xAim, yAim, zAim));
        }
        if (Double.isInfinite(forceFieldRange) || entity.squaredDistanceTo(mod.getPlayer()) < forceFieldRange * forceFieldRange ||
                entity.squaredDistanceTo(mod.getPlayer()) < 40) {
            if (entity instanceof FireballEntity) {
                mod.getControllerExtras().attack(entity);
            }
            boolean canAttack;
            if (equipSword) {
                equipWeapon(mod);
                canAttack = true;
            } else {
                // 装备非工具
                canAttack = mod.getSlotHandler().forceDeequipHitTool();
            }
            if (canAttack) {
                if (mod.getPlayer().isOnGround() || mod.getPlayer().getVelocity().getY() < 0 || mod.getPlayer().isTouchingWater()) {
                    attackedLastTick = true;
                    mod.getControllerExtras().attack(entity);
                }
            }
        }
    }

        /**
     * 开始盾牌保护模式
     * @param mod AltoClef主模块实例
     */
    public void startShielding(AltoClef mod) {
        shielding = true;
        mod.getClientBaritone().getPathingBehavior().requestPause();
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
        if (!mod.getPlayer().isBlocking()) {
            ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
            if (ItemVer.isFood(handItem)) {
                List<ItemStack> spaceSlots = mod.getItemStorage().getItemStacksPlayerInventory(false);
                if (!spaceSlots.isEmpty()) {
                    for (ItemStack spaceSlot : spaceSlots) {
                        if (spaceSlot.isEmpty()) {
                            mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(), 0, SlotActionType.QUICK_MOVE);
                            return;
                        }
                    }
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                garbage.ifPresent(slot -> mod.getSlotHandler().forceEquipItem(StorageHelper.getItemStackInSlot(slot).getItem()));
            }
        }
        mod.getInputControls().hold(Input.SNEAK);
        mod.getInputControls().hold(Input.CLICK_RIGHT);
    }

        /**
     * 停止盾牌保护模式
     * @param mod AltoClef主模块实例
     */
    public void stopShielding(AltoClef mod) {
        if (shielding) {
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
            if (ItemVer.isFood(cursor)) {
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
                if (toMoveTo.isPresent()) {
                    Slot garbageSlot = toMoveTo.get();
                    mod.getSlotHandler().clickSlot(garbageSlot, 0, SlotActionType.PICKUP);
                }
            }
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getInputControls().release(Input.JUMP);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            shielding = false;
        }
    }

        /**
     * 检查是否正在使用盾牌保护
     * @return 如果正在盾牌保护返回true，否则返回false
     */
    public boolean isShielding() {
        return shielding;
    }

    /**
     * 力场策略枚举
     */
    public enum Strategy {
        // 关闭
        OFF,
        // 最快攻击
        FASTEST,
        // 延迟攻击
        DELAY,
        // 智能攻击
        SMART
    }
}