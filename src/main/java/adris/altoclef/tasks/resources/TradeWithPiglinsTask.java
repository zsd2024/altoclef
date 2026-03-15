package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.AbstractDoToEntityTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.HashSet;
import java.util.Optional;

/**
 * 与猪灵交易任务
 * 用于与下界的猪灵进行以物易物，获取目标物品
 */
public class TradeWithPiglinsTask extends ResourceTask {

    // TODO: 设置? 自定义参数?
    private static final boolean AVOID_HOGLINS = true;
    private static final double HOGLIN_AVOID_TRADE_RADIUS = 64;
    // 如果我们离交易的猪灵太远，可能会导致它们卸载并失去交易机会
    private static final double TRADING_PIGLIN_TOO_FAR_AWAY = 64 + 8;
    private final int goldBuffer; // 金锭缓冲数量
    private final Task tradeTask = new PerformTradeWithPiglin();
    private Task goldTask = null;

    public TradeWithPiglinsTask(int goldBuffer, ItemTarget[] itemTargets) {
        super(itemTargets);
        this.goldBuffer = goldBuffer;
    }

    public TradeWithPiglinsTask(int goldBuffer, ItemTarget target) {
        super(target);
        this.goldBuffer = goldBuffer;
    }

    public TradeWithPiglinsTask(int goldBuffer, Item item, int targetCount) {
        super(item, targetCount);
        this.goldBuffer = goldBuffer;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 如果没有金锭则先收集金锭
        if (goldTask != null && goldTask.isActive() && !goldTask.isFinished()) {
            setDebugState("收集金锭");
            return goldTask;
        }
        if (!mod.getItemStorage().hasItem(Items.GOLD_INGOT)) {
            if (goldTask == null) goldTask = TaskCatalogue.getItemTask(Items.GOLD_INGOT, goldBuffer);
            return goldTask;
        }

        // 如果附近没有猪灵，则探索直到找到猪灵
        if (!mod.getEntityTracker().entityFound(PiglinEntity.class)) {
            setDebugState("徘徊寻找猪灵");
            return new TimeoutWanderTask(false);
        }

        // 如果我们有一个交易猪灵但距离太远，则靠近它

        // 寻找金锭并与猪灵交易
        setDebugState("与猪灵交易");
        return tradeTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务停止时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof TradeWithPiglinsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "与猪灵交易";
    }

    /**
     * 执行与猪灵的交易任务
     * 处理实际的交易逻辑
     */
    static class PerformTradeWithPiglin extends AbstractDoToEntityTask {

        private static final double PIGLIN_NEARBY_RADIUS = 10;
        private final TimerGame _barterTimeout = new TimerGame(2); // 交易超时计时器
        private final TimerGame _intervalTimeout = new TimerGame(10); // 间隔超时计时器
        private final HashSet<Entity> _blacklisted = new HashSet<>(); // 黑名单猪灵
        private Entity _currentlyBartering = null; // 当前正在交易的实体

        public PerformTradeWithPiglin() {
            super(3);
        }

        @Override
        protected void onStart() {
            super.onStart();
            AltoClef mod = AltoClef.getInstance();

            mod.getBehaviour().push();

            // 不要扔掉我们的金锭
            mod.getBehaviour().addProtectedItems(Items.GOLD_INGOT);

            // 除非我们已将它们列入黑名单，否则不要攻击猪灵
            mod.getBehaviour().addForceFieldExclusion(entity -> {
                if (entity instanceof PiglinEntity) {
                    return !_blacklisted.contains(entity);
                }
                return false;
            });
            //_blacklisted.clear();
        }

        @Override
        protected void onStop(Task interruptTask) {
            super.onStop(interruptTask);
            AltoClef.getInstance().getBehaviour().pop();
        }

        @Override
        protected boolean isSubEqual(AbstractDoToEntityTask other) {
            return other instanceof PerformTradeWithPiglin;
        }

        @Override
        protected Task onEntityInteract(AltoClef mod, Entity entity) {

            // 如果我们很久没有执行此操作，则可以重试交易
            if (_intervalTimeout.elapsed()) {
                // 我们很久没有交互了，继续正常交易
                _barterTimeout.reset();
                _intervalTimeout.reset();
            }

            // 我们正在交易，所以重置交易超时
            if (EntityHelper.isTradingPiglin(_currentlyBartering)) {
                _barterTimeout.reset();
            }

            // 我们正在与新实体交易
            if (!entity.equals(_currentlyBartering)) {
                _currentlyBartering = entity;
                _barterTimeout.reset();
            }

            if (_barterTimeout.elapsed()) {
                // 交易失败
                Debug.logMessage("与当前猪灵交易失败，将其列入黑名单。");
                _blacklisted.add(_currentlyBartering);
                _barterTimeout.reset();
                _currentlyBartering = null;
                return null;
            }

            if (AVOID_HOGLINS && _currentlyBartering != null && !EntityHelper.isTradingPiglin(_currentlyBartering)) {
                Optional<Entity> closestHoglin = mod.getEntityTracker().getClosestEntity(_currentlyBartering.getPos(), HoglinEntity.class);
                if (closestHoglin.isPresent() && closestHoglin.get().isInRange(entity, HOGLIN_AVOID_TRADE_RADIUS)) {
                    Debug.logMessage("因为出现了疣猪兽，中止进一步交易");
                    _blacklisted.add(_currentlyBartering);
                    _barterTimeout.reset();
                    _currentlyBartering = null;
                }
            }

            setDebugState("与猪灵交易");

            if (mod.getSlotHandler().forceEquipItem(Items.GOLD_INGOT)) {
                mod.getController().interactEntity(mod.getPlayer(), entity, Hand.MAIN_HAND);
                _intervalTimeout.reset();
            }
            return null;
        }

        @Override
        protected Optional<Entity> getEntityTarget(AltoClef mod) {
            // 忽略正在交易的猪灵
            Optional<Entity> found = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(),
                    entity -> {
                        if (_blacklisted.contains(entity)
                                || EntityHelper.isTradingPiglin(entity)
                                || (entity instanceof LivingEntity && ((LivingEntity) entity).isBaby())
                                || (_currentlyBartering != null && !entity.isInRange(_currentlyBartering, PIGLIN_NEARBY_RADIUS))) {
                            return false;
                        }

                        if (AVOID_HOGLINS) {
                            // 如果疣猪兽在附近，则避免交易
                            Optional<Entity> closestHoglin = mod.getEntityTracker().getClosestEntity(entity.getPos(), HoglinEntity.class);
                            return closestHoglin.isEmpty() || !closestHoglin.get().isInRange(entity, HOGLIN_AVOID_TRADE_RADIUS);
                        }
                        return true;
                    }, PiglinEntity.class
            );
            if (found.isEmpty()) {
                if (_currentlyBartering != null && (_blacklisted.contains(_currentlyBartering) || !_currentlyBartering.isAlive())) {
                    _currentlyBartering = null;
                }
                found = Optional.ofNullable(_currentlyBartering);
            }
            return found;
        }

        @Override
        protected String toDebugString() {
            return "与猪灵交易";
        }
    }

}
