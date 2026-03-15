package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.multiversion.OptionsVer;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.entity.AbstractKillEntityTask;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.resources.CollectBlockByOneTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 杀死末影龙任务
 * 这里是最终的冲刺
 * <p>
 * 除非不可避免地出现问题而我必须回到这里修复它
 * 在这种情况下这会相当讽刺。
 */
public class KillEnderDragonTask extends Task {

    private static final String[] DIAMOND_ARMORS = new String[]{"diamond_chestplate", "diamond_leggings", "diamond_helmet", "diamond_boots"};
    // 不要意外激怒末影人 lol
    private final TimerGame lookDownTimer = new TimerGame(0.5);
    private final Task collectBuildMaterialsTask = new CollectBlockByOneTask.CollectEndStoneTask(100);
    private final PunkEnderDragonTask punkTask = new PunkEnderDragonTask();
    private BlockPos exitPortalTop;

    /**
     * 如果有任何物品掉落，则获取拾取任务
     * @param mod AltoClef实例
     * @param itemsToPickup 要拾取的物品数组
     * @return 如果有物品掉落返回拾取任务，否则返回null
     */
    private static Task getPickupTaskIfAny(AltoClef mod, Item... itemsToPickup) {
        for (Item check : itemsToPickup) {
            if (mod.getEntityTracker().itemDropped(check)) {
                return new PickupDroppedItemTask(new ItemTarget(check), true);
            }
        }
        return null;
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        mod.getBehaviour().push();
        // 不要对末影人使用力场。
        mod.getBehaviour().addForceFieldExclusion(entity -> entity instanceof EndermanEntity || entity instanceof EnderDragonEntity || entity instanceof EnderDragonPart);
        mod.getBehaviour().setPreferredStairs(true);
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        if (exitPortalTop == null) {
            exitPortalTop = locateExitPortalTop(mod);
        }

        // 如果掉落则收集：
        // - 钻石剑
        // - 钻石头盔
        // - 食物（列表）

        List<Item> toPickUp = new ArrayList<>(Arrays.asList(Items.DIAMOND_SWORD, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET));
        if (StorageHelper.calculateInventoryFoodScore() < 10) {
            toPickUp.addAll(Arrays.asList(
                    Items.BREAD, Items.COOKED_BEEF, Items.COOKED_CHICKEN, Items.COOKED_MUTTON, Items.COOKED_RABBIT, Items.COOKED_PORKCHOP
            ));
        }

        Task pickupDrops = getPickupTaskIfAny(mod, toPickUp.toArray(Item[]::new));
        if (pickupDrops != null) {
            setDebugState("在末地拾取掉落物。");
            return pickupDrops;
        }

        // 如果没有装备钻石盔甲且我们有任意一件，就装备它。
        for (Item armor : ItemHelper.DIAMOND_ARMORS) {
            try {
                if (mod.getItemStorage().hasItem(armor) && !StorageHelper.isArmorEquipped(armor)) {
                    setDebugState("装备 " + armor);
                    return new EquipArmorTask(armor);
                }
            } catch (NullPointerException e) {
                // 不应该发生。
                Debug.logError("不应发生的空指针异常。");
                e.printStackTrace();
            }
        }

        if (!isRailingOnDragon() && lookDownTimer.elapsed() && !mod.getControllerExtras().isBreakingBlock()) {
            if (mod.getPlayer().isOnGround()) {
                lookDownTimer.reset();
                mod.getClientBaritone().getLookBehavior().updateTarget(new Rotation(0f, 90f), true);
            }
        }

        // 如果有传送门，进入它。
        if (mod.getBlockScanner().anyFound(Blocks.END_PORTAL)) {
            setDebugState("进入传送门以完成游戏。");
            return new DoToClosestBlockTask(
                    blockPos -> new GetToBlockTask(blockPos.up(), false),
                    Blocks.END_PORTAL
            );
        }

        // 如果我们没有建筑材料（石头 + 圆石 + 末地石），获取末地石
        // 如果有水晶，自杀式爆炸摧毁它们。
        // 如果没有水晶，如果龙靠近就攻击龙。
        int MINIMUM_BUILDING_BLOCKS = 1;
        if (mod.getEntityTracker().entityFound(EndCrystalEntity.class) && mod.getItemStorage().getItemCount(Items.DIRT, Items.COBBLESTONE, Items.NETHERRACK, Items.END_STONE) < MINIMUM_BUILDING_BLOCKS || (collectBuildMaterialsTask.isActive() && !collectBuildMaterialsTask.isFinished())) {
            if (StorageHelper.miningRequirementMetInventory(MiningRequirement.WOOD)) {
                mod.getBehaviour().addProtectedItems(Items.END_STONE);
                setDebugState("收集建筑材料以搭建柱子到水晶");
                return collectBuildMaterialsTask;
            }
        } else {
            mod.getBehaviour().removeProtectedItems(Items.END_STONE);
        }

        // 爆炸最近的末地水晶
        if (mod.getEntityTracker().entityFound(EndCrystalEntity.class)) {
            setDebugState("自杀式攻击水晶");
            return new DoToClosestEntityTask(
                    (toDestroy) -> {
                        if (toDestroy.isInRange(mod.getPlayer(), 7)) {
                            mod.getControllerExtras().attack(toDestroy);
                        }
                        // 到水晶旁边，任意位置，我们只需要靠近。
                        return new GetToBlockTask(toDestroy.getBlockPos().add(1,0,0), false);
                    },
                    EndCrystalEntity.class
            );
        }

        // 攻击末影龙
        if (mod.getEntityTracker().entityFound(EnderDragonEntity.class)) {
            setDebugState("攻击末影龙");
            return punkTask;
        }
        setDebugState("找不到末影龙... 这可能是好消息或坏消息。");
        return null;
        //return new KillEntitiesTask(EnderDragonEntity.class);
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof KillEnderDragonTask;
    }

    @Override
    protected String toDebugString() {
        return "杀死末影龙";
    }

    /**
     * 检查是否正在攻击龙
     * @return 如果正在攻击龙返回true
     */
    private boolean isRailingOnDragon() {
        return punkTask.getMode() == Mode.RAILING;
    }

    /**
     * 定位出口传送门顶部
     * @param mod AltoClef实例
     * @return 返回出口传送门顶部位置
     */
    private BlockPos locateExitPortalTop(AltoClef mod) {
        if (!mod.getChunkTracker().isChunkLoaded(new BlockPos(0, 64, 0))) return null;
        int height = WorldHelper.getGroundHeight(0, 0, Blocks.BEDROCK);
        if (height != -1) return new BlockPos(0, height, 0);
        return null;
    }

    /**
     * 攻击模式枚举
     */
    private enum Mode {
        // 等待栖息
        WAITING_FOR_PERCH,
        // 攻击
        RAILING
    }

    /**
     * 攻击末影龙的内部任务类
     * 负责处理攻击末影龙的具体策略和行为
     */
    private class PunkEnderDragonTask extends Task {

        // 龙息伤害成本映射
        private final HashMap<BlockPos, Double> _breathCostMap = new HashMap<>();
        // 攻击持续时间计时器
        private final TimerGame _hitHoldTimer = new TimerGame(0.1);
        // 攻击重置计时器
        private final TimerGame _hitResetTimer = new TimerGame(0.4);
        // 随机徘徊位置变化超时计时器
        private final TimerGame _randomWanderChangeTimeout = new TimerGame(20);
        // 当前模式
        private Mode _mode = Mode.WAITING_FOR_PERCH;

        // 随机徘徊位置
        private BlockPos _randomWanderPos;
        // 是否正在攻击
        private boolean _wasHitting;
        // 是否已释放
        private boolean _wasReleased;

        private PunkEnderDragonTask() {
        }

        /**
         * 获取当前模式
         * @return 返回当前模式
         */
        public Mode getMode() {
            return _mode;
        }

        /**
         * 攻击龙
         * @param mod AltoClef实例
         */
        private void hit(AltoClef mod) {
            mod.getExtraBaritoneSettings().setInteractionPaused(true);
            if (!_wasHitting) {
                _wasHitting = true;
                _wasReleased = false;
                _hitHoldTimer.reset();
                _hitResetTimer.reset();
                Debug.logInternal("攻击");
                mod.getInputControls().tryPress(Input.CLICK_LEFT);
                //mod.getPlayer().swingHand(Hand.MAIN_HAND);
            }
            if (_hitHoldTimer.elapsed()) {
                if (!_wasReleased) {
                    Debug.logInternal("释放");
                    //mod.getControllerExtras().mouseClickOverride(0, false);
                    _wasReleased = true;
                }
            }
            if (_wasHitting && _hitResetTimer.elapsed() && mod.getPlayer().getAttackCooldownProgress(0) > 0.99) {
                _wasHitting = false;
                // 代码可能重复？
                //mod.getControllerExtras().mouseClickOverride(0, false);
                mod.getExtraBaritoneSettings().setInteractionPaused(false);
                _hitResetTimer.reset();
            }
        }

        /**
         * 停止攻击
         * @param mod AltoClef实例
         */
        private void stopHitting(AltoClef mod) {
            if (_wasHitting) {
                //MinecraftClient.getInstance().options.keyAttack.setPressed(false);
                if (!_wasReleased) {
                    //mod.getControllerExtras().mouseClickOverride(0, false);
                    mod.getExtraBaritoneSettings().setInteractionPaused(false);
                    _wasReleased = true;
                }
                _wasHitting = false;
            }
        }


        @Override
        protected void onStart() {
            AltoClef.getInstance().getClientBaritone().getCustomGoalProcess().onLostControl();
        }

        @Override
        protected Task onTick() {
            AltoClef mod = AltoClef.getInstance();

            if (!mod.getEntityTracker().entityFound(EnderDragonEntity.class)) {
                setDebugState("未找到龙。");
                return null;
            }
            List<EnderDragonEntity> dragons = mod.getEntityTracker().getTrackedEntities(EnderDragonEntity.class);
            if (!dragons.isEmpty()) {
                for (EnderDragonEntity dragon : dragons) {
                    Phase dragonPhase = dragon.getPhaseManager().getCurrent();
                    //Debug.logInternal("PHASE: " + dragonPhase);
                    boolean perchingOrGettingReady = dragonPhase.getType() == PhaseType.LANDING || dragonPhase.isSittingOrHovering();
                    switch (_mode) {
                        case RAILING -> {
                            if (!perchingOrGettingReady) {
                                Debug.logMessage("龙不再栖息。");
                                mod.getClientBaritone().getCustomGoalProcess().onLostControl();
                                _mode = Mode.WAITING_FOR_PERCH;
                                break;
                            }
                            //DamageSource.DRAGON_BREATH
                            Entity head = dragon.head;
                            // 攻击龙头
                            if (head.isInRange(mod.getPlayer(), 7.5) && dragon.ticksSinceDeath <= 1) {
                                // 装备武器
                                AbstractKillEntityTask.equipWeapon(mod);
                                // 朝向龙看
                                Vec3d targetLookPos = head.getPos().add(0, 3, 0);
                                Rotation targetRotation = RotationUtils.calcRotationFromVec3d(mod.getClientBaritone().getPlayerContext().playerHead(), targetLookPos, mod.getClientBaritone().getPlayerContext().playerRotations());
                                mod.getClientBaritone().getLookBehavior().updateTarget(targetRotation, true);
                                // 同时朝向龙看
                                OptionsVer.setAutoJump(false);
                                mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
                                hit(mod);
                            } else {
                                stopHitting(mod);
                            }
                            if (!mod.getClientBaritone().getCustomGoalProcess().isActive()) {
                                // 设置目标为龙头附近的柱子中的最近方块。
                                if (exitPortalTop != null) {
                                    int bottomYDelta = -3;
                                    BlockPos closest = null;
                                    double closestDist = Double.POSITIVE_INFINITY;
                                    for (int dx = -2; dx <= 2; ++dx) {
                                        for (int dz = -2; dz <= 2; ++dz) {
                                            // 我们这里有个圆形。
                                            if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                                            BlockPos toCheck = exitPortalTop.add(dx,bottomYDelta,dz);
                                            double distSq = BlockPosVer.getSquaredDistance(toCheck,head.getPos());
                                            if (distSq < closestDist) {
                                                closest = toCheck;
                                                closestDist = distSq;
                                            }
                                        }
                                    }
                                    if (closest != null) {
                                        mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(
                                                new GoalGetToBlock(closest)
                                        );
                                    }
                                }
                            }
                            setDebugState("攻击龙");
                        }
                        case WAITING_FOR_PERCH -> {
                            stopHitting(mod);
                            if (perchingOrGettingReady) {
                                // 我们栖息了！
                                mod.getClientBaritone().getCustomGoalProcess().onLostControl();
                                Debug.logMessage("检测到龙栖息。现在我要去攻击它。");
                                _mode = Mode.RAILING;
                                break;
                            }
                            // 无目的地奔跑，躲避龙的火焰
                            if (_randomWanderPos != null && WorldHelper.inRangeXZ(mod.getPlayer(), _randomWanderPos, 2)) {
                                _randomWanderPos = null;
                            }
                            if (_randomWanderPos != null && _randomWanderChangeTimeout.elapsed()) {
                                _randomWanderPos = null;
                                Debug.logMessage("超时后重置徘徊位置");
                            }
                            if (_randomWanderPos == null) {
                                _randomWanderPos = getRandomWanderPos(mod);
                                _randomWanderChangeTimeout.reset();
                                mod.getClientBaritone().getCustomGoalProcess().onLostControl();
                            }
                            if (!mod.getClientBaritone().getCustomGoalProcess().isActive()) {
                                mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(
                                        new GoalGetToBlock(_randomWanderPos)
                                );
                            }
                            setDebugState("等待栖息");
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onStop(Task interruptTask) {
            AltoClef mod = AltoClef.getInstance();

            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            //mod.getControllerExtras().mouseClickOverride(0, false);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
        }

        @Override
        protected boolean isEqual(Task other) {
            return other instanceof PunkEnderDragonTask;
        }

        @Override
        protected String toDebugString() {
            return "攻击龙";
        }

        /**
         * 获取随机徘徊位置
         * @param mod AltoClef实例
         * @return 返回随机徘徊位置
         */
        private BlockPos getRandomWanderPos(AltoClef mod) {
            double RADIUS_RANGE = 45;
            double MIN_RADIUS = 7;
            BlockPos pos = null;
            int allowed = 5000;

            while (pos == null) {
                if (allowed-- < 0) {
                    Debug.logWarning("在末地未能找到随机的坚实地面，这可能会导致问题。");
                    return null;
                }
                double radius = MIN_RADIUS + (RADIUS_RANGE - MIN_RADIUS) * Math.random();
                double angle = Math.PI * 2 * Math.random();
                int x = (int) (radius * Math.cos(angle)),
                        z = (int) (radius * Math.sin(angle));
                int y = WorldHelper.getGroundHeight(x, z);
                if (y == -1) continue;
                BlockPos check = new BlockPos(x, y, z);
                if (mod.getWorld().getBlockState(check).getBlock() == Blocks.END_STONE) {
                    // 我们找到了一个位置！
                    pos = check.up();
                }
            }
            return pos;
        }
    }
}
