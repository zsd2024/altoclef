package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.control.KillAura;
import adris.altoclef.multiversion.versionedfields.Entities;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.construction.ProjectileProtectionWallTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.CustomBaritoneGoalTask;
import adris.altoclef.tasks.movement.DodgeProjectilesTask;
import adris.altoclef.tasks.movement.RunAwayFromCreepersTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import baritone.Baritone;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;


import java.util.*;


// TODO: 优化对蜘蛛和骷髅的护盾防御

/**
 * 怪物防御链 - 自动处理玩家面对敌对生物时的防御行为
 * 包括主动攻击、躲避、护盾防御、逃跑等多种防御策略
 */
public class MobDefenseChain extends SingleTaskChain {
    private static final double DANGER_KEEP_DISTANCE = 30; // 危险距离阈值
    private static final double CREEPER_KEEP_DISTANCE = 10; // 苦力怕距离阈值
    private static final double ARROW_KEEP_DISTANCE_HORIZONTAL = 2; // 箭矢水平距离阈值
    private static final double ARROW_KEEP_DISTANCE_VERTICAL = 10; // 箭矢垂直距离阈值
    private static final double SAFE_KEEP_DISTANCE = 8; // 安全距离阈值
    private static final List<Class<? extends Entity>> ignoredMobs = List.of(Entities.WARDEN, WitherEntity.class, EndermanEntity.class, BlazeEntity.class,
            WitherSkeletonEntity.class, HoglinEntity.class, ZoglinEntity.class, PiglinBruteEntity.class, VindicatorEntity.class, MagmaCubeEntity.class); // 忽略的怪物类型列表

    private static boolean shielding = false; // 标记是否正在护盾防御
    private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker(); // 龙息追踪器
    private final KillAura killAura = new KillAura(); // 自动攻击光环
    private Entity targetEntity; // 目标实体
    private boolean doingFunkyStuff = false; // 标记是否在做特殊操作
    private boolean wasPuttingOutFire = false; // 标记是否正在灭火
    private CustomBaritoneGoalTask runAwayTask; // 逃跑任务
    private float prevHealth = 20; // 前一时刻的健康值
    private boolean needsChangeOnAttack = false; // 标记是否需要在攻击后改变策略
    private Entity lockedOnEntity = null; // 锁定的实体

    private float cachedLastPriority; // 缓存的优先级

    public MobDefenseChain(TaskRunner runner) {
        super(runner);
    }

    /**
     * 计算苦力怕的安全值
     * @param pos 位置向量
     * @param creeper 苦力怕实体
     * @return 安全值（更小表示更危险）
     */
    public static double getCreeperSafety(Vec3d pos, CreeperEntity creeper) {
        double distance = creeper.squaredDistanceTo(pos);
        float fuse = creeper.getClientFuseTime(1);

        // 未引燃
        if (fuse <= 0.001f) return distance;
        return distance * 0.2; // 更小表示更危险
    }

    /**
     * 开始护盾防御
     * @param mod AltoClef实例
     */
    private static void startShielding(AltoClef mod) {
        shielding = true;
        mod.getClientBaritone().getPathingBehavior().requestPause();
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
        if (!mod.getPlayer().isBlocking()) {
            ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
            if (ItemVer.isFood(handItem)) {
                List<ItemStack> spaceSlots = mod.getItemStorage().getItemStacksPlayerInventory(false);
                for (ItemStack spaceSlot : spaceSlots) {
                    if (spaceSlot.isEmpty()) {
                        mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(), 0, SlotActionType.QUICK_MOVE);
                        return;
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
     * 获取危险度评分
     * @param toDealWithList 需要处理的实体列表
     * @return 危险度评分
     */
    private static int getDangerousnessScore(List<LivingEntity> toDealWithList) {
        int numberOfProblematicEntities = toDealWithList.size();
        for (LivingEntity toDealWith : toDealWithList) {
            if (toDealWith instanceof EndermanEntity || toDealWith instanceof SlimeEntity || toDealWith instanceof BlazeEntity) {

                numberOfProblematicEntities += 1;
            } else if (toDealWith instanceof DrownedEntity && toDealWith.getEquippedItems() == Items.TRIDENT) {
                // 持有三叉戟的溺尸也非常危险，也许我们应该增加这个值？？
                numberOfProblematicEntities += 5;
            }
        }
        return numberOfProblematicEntities;
    }

    @Override
    public float getPriority() {
        cachedLastPriority = getPriorityInner();
        prevHealth = AltoClef.getInstance().getPlayer().getHealth();
        return cachedLastPriority;
    }

    /**
     * 停止护盾防御
     * @param mod AltoClef实例
     */
    private void stopShielding(AltoClef mod) {
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
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            shielding = false;
        }
    }

    /**
     * 检查是否正在护盾防御
     * @return 如果正在护盾防御则返回true
     */
    public boolean isShielding() {
        return shielding || killAura.isShielding();
    }

    /**
     * 检查是否需要躲避龙息
     * @param mod AltoClef实例
     * @return 如果需要躲避龙息则返回true
     */
    private boolean escapeDragonBreath(AltoClef mod) {
        dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer()) {
            if (dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取内部优先级
     * @return 优先级值
     */
    private float getPriorityInner() {
        if (!AltoClef.inGame()) {
            return Float.NEGATIVE_INFINITY;
        }
        AltoClef mod = AltoClef.getInstance();

        if (!mod.getModSettings().isMobDefense()) {
            return Float.NEGATIVE_INFINITY;
        }

        if (mod.getWorld().getDifficulty() == Difficulty.PEACEFUL) return Float.NEGATIVE_INFINITY;

        if (needsChangeOnAttack && (mod.getPlayer().getHealth() < prevHealth || killAura.attackedLastTick)) {
            needsChangeOnAttack = false;
        }

        // 灭火，如果我们像傻瓜一样站在火上
        BlockPos fireBlock = isInsideFireAndOnFire(mod);
        if (fireBlock != null) {
            putOutFire(mod, fireBlock);
            wasPuttingOutFire = true;
        } else {
            // 如果不再需要灭火，则停止灭火
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
            wasPuttingOutFire = false;
        }

        // 如果附近有危险的怪物就逃跑
        Optional<Entity> universallyDangerous = getUniversallyDangerousMob(mod);
        if (universallyDangerous.isPresent() && mod.getPlayer().getHealth() <= 10) {
            runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
            setTask(runAwayTask);
            return 70;
        }

        doingFunkyStuff = false;
        PlayerSlot offhandSlot = PlayerSlot.OFFHAND_SLOT;
        Item offhandItem = StorageHelper.getItemStackInSlot(offhandSlot).getItem();
        // 远离苦力怕
        CreeperEntity blowingUp = getClosestFusingCreeper(mod);
        if (blowingUp != null) {
            if ((!mod.getFoodChain().needsToEat() || mod.getPlayer().getHealth() < 9)
                    && hasShield(mod)
                    && !mod.getEntityTracker().entityFound(PotionEntity.class)
                    && !mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem)
                    && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
                    && blowingUp.getClientFuseTime(blowingUp.getFuseSpeed()) > 0.5) {
                LookHelper.lookAt(mod, blowingUp.getEyePos());
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
            } else {
                doingFunkyStuff = true;
                runAwayTask = new RunAwayFromCreepersTask(CREEPER_KEEP_DISTANCE);
                setTask(runAwayTask);
                return 50 + blowingUp.getClientFuseTime(1) * 50;
            }
        }
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            // 用护盾阻挡投射物
            if (mod.getModSettings().isDodgeProjectiles()
                    && hasShield(mod)
                    && !mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem)
                    && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
                    && !mod.getEntityTracker().entityFound(PotionEntity.class) && isProjectileClose(mod)) {
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
                return 60;
            }
            if (blowingUp == null && !isProjectileClose(mod)) {
                stopShielding(mod);
            }
        }

        if (mod.getFoodChain().needsToEat() || mod.getMLGBucketChain().isFalling(mod)
                || !mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isChorusFruiting()) {
            killAura.stopShielding(mod);
            stopShielding(mod);
            return Float.NEGATIVE_INFINITY;
        }

        // 力场
        doForceField(mod);

        // 躲避投射物
        if (mod.getPlayer().getHealth() <= 10 && !hasShield(mod)) {

            if (StorageHelper.getNumberOfThrowawayBlocks(mod) > 0 && !mod.getFoodChain().needsToEat()
                    && mod.getModSettings().isDodgeProjectiles() && isProjectileClose(mod)) {
                doingFunkyStuff = true;
                setTask(new ProjectileProtectionWallTask(mod));
                return 65;
            }

            runAwayTask = new DodgeProjectilesTask(ARROW_KEEP_DISTANCE_HORIZONTAL, ARROW_KEEP_DISTANCE_VERTICAL);
            setTask(runAwayTask);
            return 65;
        }
        // 避开所有怪物，因为我们快死了
        if (isInDanger(mod) && !escapeDragonBreath(mod) && !mod.getFoodChain().isShouldStop()) {
            if (targetEntity == null || WorldHelper.isSurroundedByHostiles()) {
                runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
                setTask(runAwayTask);
                return 70;
            }
        }

        if (mod.getModSettings().shouldDealWithAnnoyingHostiles()) {
            // 处理敌对生物，因为它们很烦人
            List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();

            List<LivingEntity> toDealWithList = new ArrayList<>();

            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                for (LivingEntity hostile : hostiles) {
                    boolean isRangedOrPoisonous = (hostile instanceof SkeletonEntity
                            || hostile instanceof WitchEntity || hostile instanceof PillagerEntity
                            || hostile instanceof PiglinEntity || hostile instanceof StrayEntity
                            || hostile instanceof CaveSpiderEntity);
                    int annoyingRange = 10;

                    if (isRangedOrPoisonous) {
                        annoyingRange = 20;
                        if (!hasShield(mod)) {
                            annoyingRange = 35;
                        }
                    }

                    // 给每个敌对生物一个计时器，如果它们接近太久就处理它们
                    if (hostile.isInRange(mod.getPlayer(), annoyingRange) && LookHelper.seesPlayer(hostile, mod.getPlayer(), annoyingRange)) {

                        boolean isIgnored = false;
                        for (Class<? extends Entity> ignored : ignoredMobs) {
                            if (ignored.isInstance(hostile)) {
                                isIgnored = true;
                                break;
                            }
                        }

                        // 不要攻击这些怪物，只有在低血量或距离很近时才攻击
                        if (isIgnored) {
                            if (mod.getPlayer().getHealth() <= 10) {
                                toDealWithList.add(hostile);
                            }
                        } else {
                            toDealWithList.add(hostile);
                        }
                    }
                }
            }

            // 优先攻击距离玩家最近的实体
            toDealWithList.sort(Comparator.comparingDouble((entity) -> mod.getPlayer().distanceTo(entity)));

            if (!toDealWithList.isEmpty()) {

                // 根据我们的武器/护甲，如果我们不躲避箭矢，我们可能会选择直接杀死敌对生物
                SwordItem bestSword = getBestSword(mod);

                int armor = mod.getPlayer().getArmor();
                float damage = bestSword == null ? 0 : (bestSword.getMaterial().getAttackDamage()) + 1;

                int shield = hasShield(mod) && bestSword != null ? 3 : 0;

                int canDealWith = (int) Math.ceil((armor * 3.6 / 20.0) + (damage * 0.8) + (shield));

                if (canDealWith >= getDangerousnessScore(toDealWithList) || needsChangeOnAttack) {
                    // 我们决定攻击，所以我们应该要么获取它，要么在再次逃跑前击中一些东西
                    if (!(mainTask instanceof KillEntitiesTask)) {
                        needsChangeOnAttack = true;
                    }

                    // 我们可以处理它
                    runAwayTask = null;
                    Entity toKill = toDealWithList.get(0);
                    lockedOnEntity = toKill;

                    setTask(new KillEntitiesTask(toKill.getClass()));
                    return 65;
                } else {
                    // 我们无法处理它
                    runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
                    setTask(runAwayTask);
                    return 80;
                }
            }
        }
        // 默认情况下，如果我们不是"立即"危险但正在逃跑，继续
        // 逃跑直到安全
        if (runAwayTask != null && !runAwayTask.isFinished()) {
            setTask(runAwayTask);
            return cachedLastPriority;
        } else {
            runAwayTask = null;
        }

        if (needsChangeOnAttack && lockedOnEntity != null && lockedOnEntity.isAlive()) {
            setTask(new KillEntitiesTask(lockedOnEntity.getClass()));
            return 65;
        } else {
            needsChangeOnAttack = false;
            lockedOnEntity = null;
        }

        return 0;
    }

    /**
     * 检查是否拥有护盾
     * @param mod AltoClef实例
     * @return 如果拥有护盾则返回true
     */
    private static boolean hasShield(AltoClef mod) {
        return mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD);
    }

    /**
     * 获取最佳剑
     * @param mod AltoClef实例
     * @return 最佳剑
     */
    private static SwordItem getBestSword(AltoClef mod) {
        Item[] SWORDS = new Item[]{Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD,
                Items.STONE_SWORD, Items.WOODEN_SWORD};

        SwordItem bestSword = null;
        for (Item item : SWORDS) {
            if (mod.getItemStorage().hasItem(item)) {
                bestSword = (SwordItem) item;
                break;
            }
        }
        return bestSword;
    }

    /**
     * 检查是否站在火上且着火
     * @param mod AltoClef实例
     * @return 如果站在火上且着火则返回火块位置，否则返回null
     */
    private BlockPos isInsideFireAndOnFire(AltoClef mod) {
        boolean onFire = mod.getPlayer().isOnFire();
        if (!onFire) return null;
        BlockPos p = mod.getPlayer().getBlockPos();
        BlockPos[] toCheck = new BlockPos[]{
                p,
                p.add(1,0,0),
                p.add(1,0,-1),
                p.add(0,0,-1),
                p.add(-1,0,-1),
                p.add(-1,0,0),
                p.add(-1,0,1),
                p.add(0,0,1),
                p.add(1,0,1)
        };
        for (BlockPos check : toCheck) {
            Block b = mod.getWorld().getBlockState(check).getBlock();
            if (b instanceof AbstractFireBlock) {
                return check;
            }
        }
        return null;
    }

    /**
     * 灭火
     * @param mod AltoClef实例
     * @param pos 火块位置
     */
    private void putOutFire(AltoClef mod, BlockPos pos) {
        Optional<Rotation> reach = LookHelper.getReach(pos);
        if (reach.isPresent()) {
            Baritone b = mod.getClientBaritone();
            if (LookHelper.isLookingAt(mod, pos)) {
                b.getPathingBehavior().requestPause();
                b.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
                return;
            }
            LookHelper.lookAt(reach.get());
        }
    }

    /**
     * 执行力场防御
     * @param mod AltoClef实例
     */
    private void doForceField(AltoClef mod) {
        killAura.tickStart();

        // 攻击所有接近我们的敌对生物
        List<Entity> entities = mod.getEntityTracker().getCloseEntities();
        try {
            for (Entity entity : entities) {
                boolean shouldForce = false;
                if (mod.getBehaviour().shouldExcludeFromForcefield(entity)) continue;
                if (entity instanceof MobEntity) {
                    if (EntityHelper.isProbablyHostileToPlayer(mod, entity)) {
                        if (LookHelper.seesPlayer(entity, mod.getPlayer(), 10)) {
                            shouldForce = true;
                        }
                    }
                } else if (entity instanceof FireballEntity) {
                    // 恶魂火球
                    shouldForce = true;
                }

                if (shouldForce) {
                    killAura.applyAura(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        killAura.tickEnd(mod);
    }


    /**
     * 获取最近的引燃苦力怕
     * @param mod AltoClef实例
     * @return 最近的引燃苦力怕，如果没有则返回null
     */
    private CreeperEntity getClosestFusingCreeper(AltoClef mod) {
        double worstSafety = Float.POSITIVE_INFINITY;
        CreeperEntity target = null;
        try {
            List<CreeperEntity> creepers = mod.getEntityTracker().getTrackedEntities(CreeperEntity.class);
            for (CreeperEntity creeper : creepers) {
                if (creeper == null) continue;
                if (creeper.getClientFuseTime(1) < 0.001) continue;

                // 我们想选择最近的苦力怕，但首先选择即将爆炸的苦力怕
                // 在最大引燃时间时，成本基本为零
                double safety = getCreeperSafety(mod.getPlayer().getPos(), creeper);
                if (safety < worstSafety) {
                    target = creeper;
                }
            }
        } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            // 我不知道为什么，但这些异常有时会发生。这非常奇怪，我
            // 不知道为什么
            Debug.logWarning("扫描苦力怕时捕获并忽略奇怪的异常: " + e.getMessage());
            return target;
        }
        return target;
    }

    /**
     * 检查是否有投射物接近
     * @param mod AltoClef实例
     * @return 如果有接近的投射物则返回true
     */
    private boolean isProjectileClose(AltoClef mod) {
        List<CachedProjectile> projectiles = mod.getEntityTracker().getProjectiles();
        try {
            for (CachedProjectile projectile : projectiles) {
                if (projectile.position.squaredDistanceTo(mod.getPlayer().getPos()) < 150) {
                    boolean isGhastBall = projectile.projectileType == FireballEntity.class;
                    if (isGhastBall) {
                        Optional<Entity> ghastBall = mod.getEntityTracker().getClosestEntity(FireballEntity.class);
                        Optional<Entity> ghast = mod.getEntityTracker().getClosestEntity(GhastEntity.class);
                        if (ghastBall.isPresent() && ghast.isPresent() && runAwayTask == null
                                && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                            mod.getClientBaritone().getPathingBehavior().requestPause();
                            LookHelper.lookAt(mod, ghast.get().getEyePos());
                        }
                        return false;
                        // 忽略恶魂火球
                    }
                    if (projectile.projectileType == DragonFireballEntity.class) {
                        // 忽略龙火球
                        continue;
                    }
                    if (projectile.projectileType == ArrowEntity.class || projectile.projectileType == SpectralArrowEntity.class || projectile.projectileType == SmallFireballEntity.class) {
                        // 检查投射物是否正在远离我们
                        // 不太复杂的数学...这应该比之前的方法更好（我希望只添加速度不会引起任何问题..）
                        PlayerEntity player = mod.getPlayer();
                        if (player.squaredDistanceTo(projectile.position) < player.squaredDistanceTo(projectile.position.add(projectile.velocity))) {
                            continue;
                        }
                    }

                    Vec3d expectedHit = ProjectileHelper.calculateArrowClosestApproach(projectile, mod.getPlayer());

                    Vec3d delta = mod.getPlayer().getPos().subtract(expectedHit);

                    double horizontalDistanceSq = delta.x * delta.x + delta.z * delta.z;
                    double verticalDistance = Math.abs(delta.y);
                    if (horizontalDistanceSq < ARROW_KEEP_DISTANCE_HORIZONTAL * ARROW_KEEP_DISTANCE_HORIZONTAL
                            && verticalDistance < ARROW_KEEP_DISTANCE_VERTICAL) {
                        if (mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
                                && hasShield(mod)) {
                            mod.getClientBaritone().getPathingBehavior().requestPause();
                            LookHelper.lookAt(mod, projectile.position.add(0, 0.3, 0));
                        }
                        return true;
                    }
                }
            }

        } catch (ConcurrentModificationException e) {
            Debug.logWarning(e.getMessage());
        }

        // TODO 将此重构为对所有怪物更可靠的方法
        for (SkeletonEntity skeleton : mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class)) {
            if (skeleton.distanceTo(mod.getPlayer()) > 10 || !skeleton.canSee(mod.getPlayer())) continue;

            // 当骷髅即将射击时（举起护盾需要5个刻度）
            if (skeleton.getItemUseTime() > 15) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取普遍危险的怪物
     * @param mod AltoClef实例
     * @return 危险怪物的可选对象
     */
    private Optional<Entity> getUniversallyDangerousMob(AltoClef mod) {
        // 凋零骷髅很危险，因为凋零效果。嗯，有点明显。
        // 如果我们只是力场它们，我们会撞到它们并获得凋零效果，这会杀死我们。

        Class<?>[] dangerousMobs = new Class[]{Entities.WARDEN, WitherEntity.class, WitherSkeletonEntity.class,
                HoglinEntity.class, ZoglinEntity.class, PiglinBruteEntity.class, VindicatorEntity.class};

        double range = SAFE_KEEP_DISTANCE - 2;

        for (Class<?> dangerous : dangerousMobs) {
            Optional<Entity> entity = mod.getEntityTracker().getClosestEntity(dangerous);

            if (entity.isPresent()) {
                if (entity.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, entity.get())) {
                    return entity;
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 检查是否处于危险中
     * @param mod AltoClef实例
     * @return 如果处于危险则返回true
     */
    private boolean isInDanger(AltoClef mod) {
        boolean witchNearby = mod.getEntityTracker().entityFound(WitchEntity.class);

        float health = mod.getPlayer().getHealth();
        if (health <= 10 && !witchNearby) {
            return true;
        }
        if (mod.getPlayer().hasStatusEffect(StatusEffects.WITHER) ||
                (mod.getPlayer().hasStatusEffect(StatusEffects.POISON) && !witchNearby)) {
            return true;
        }
        if (WorldHelper.isVulnerable()) {
            // 如果附近有敌对生物...
            try {
                ClientPlayerEntity player = mod.getPlayer();
                List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();

                synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                    for (Entity entity : hostiles) {
                        if (entity.isInRange(player, SAFE_KEEP_DISTANCE)
                                && !mod.getBehaviour().shouldExcludeFromForcefield(entity)
                                && EntityHelper.isAngryAtPlayer(mod, entity)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                Debug.logWarning("奇怪的多线程异常。稍后修复。 " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 设置目标实体
     * @param entity 目标实体
     */
    public void setTargetEntity(Entity entity) {
        targetEntity = entity;
    }

    /**
     * 重置目标实体
     */
    public void resetTargetEntity() {
        targetEntity = null;
    }

    /**
     * 设置力场范围
     * @param range 力场范围
     */
    public void setForceFieldRange(double range) {
        killAura.setRange(range);
    }

    /**
     * 重置力场
     */
    public void resetForceField() {
        killAura.setRange(Double.POSITIVE_INFINITY);
    }

    /**
     * 检查是否正在做特殊操作
     * @return 如果正在做特殊操作则返回true
     */
    public boolean isDoingAcrobatics() {
        return doingFunkyStuff;
    }

    /**
     * 检查是否正在灭火
     * @return 如果正在灭火则返回true
     */
    public boolean isPuttingOutFire() {
        return wasPuttingOutFire;
    }

    @Override
    public boolean isActive() {
        // 我们始终在检查怪物
        return true;
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        // 任务完成，所以我想我们继续前进？
    }

    @Override
    public String getName() {
        return "怪物防御";
    }
}