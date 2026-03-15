package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Settings;
import adris.altoclef.multiversion.FoodComponentWrapper;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.slots.PlayerSlot;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Optional;

/**
 * 食物管理链 - 自动管理玩家的食物供应和自动进食
 * 根据玩家当前健康和饥饿状态，自动选择合适的食品并进食
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FoodChain extends SingleTaskChain {
    private static FoodChainConfig config; // 食物链配置
    private static boolean hasFood; // 标记玩家是否拥有食物

    static {
        ConfigHelper.loadConfig("configs/food_chain_settings.json", FoodChainConfig::new, FoodChainConfig.class, newConfig -> config = newConfig);
    }

    private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker(); // 龙息追踪器
    private boolean isTryingToEat = false; // 标记是否正在尝试进食
    private boolean requestFillup = false; // 请求填饱饥饿值
    private boolean needsFood = false; // 标记是否需要食物
    private Optional<Item> cachedPerfectFood = Optional.empty(); // 缓存的最佳食物
    private boolean shouldStop = false; // 标记是否应该停止进食

    public FoodChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        // 任务完成时无需特殊处理
    }

    /**
     * 开始进食
     * @param mod AltoClef实例
     * @param food 要进食的食物
     */
    private void startEat(AltoClef mod, Item food) {
        //Debug.logInternal("EATING " + toUse.getTranslationKey() + " : " + test);
        if (mod.getPlayer().isBlocking()) {
            mod.log("想要进食，尝试停止护盾...");
            mod.getInputControls().release(Input.CLICK_RIGHT);
            return;
        }

        isTryingToEat = true;
        requestFillup = true;
        mod.getSlotHandler().forceEquipItem(new Item[]{food}, true); //"true"因为这是食物
        mod.getInputControls().hold(Input.CLICK_RIGHT);
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
    }

    /**
     * 停止进食
     */
    private void stopEat() {
        if (isTryingToEat) {
            AltoClef altoClef = AltoClef.getInstance();

            if (altoClef.getItemStorage().hasItem(Items.SHIELD) || altoClef.getItemStorage().hasItemInOffhand(Items.SHIELD)) {
                if (StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT).getItem() != Items.SHIELD) {
                    altoClef.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    isTryingToEat = false;
                    requestFillup = false;
                }
            } else {
                isTryingToEat = false;
                requestFillup = false;
            }
            altoClef.getInputControls().release(Input.CLICK_RIGHT);
            altoClef.getExtraBaritoneSettings().setInteractionPaused(false);
        }
    }

    /**
     * 检查是否正在尝试进食
     * @return 如果正在尝试进食则返回true
     */
    public boolean isTryingToEat() {
        return isTryingToEat;
    }

@Override
    public float getPriority() {
        AltoClef mod = AltoClef.getInstance();

        if (WorldHelper.isInNetherPortal()) {
            stopEat();
            return Float.NEGATIVE_INFINITY;
        }
        // 不要在防御怪物时中断进食
        if (mod.getMobDefenseChain().isPuttingOutFire()
                || mod.getMobDefenseChain().isShielding()
                || mod.getPlayer().isBlocking()
                || mod.getMobDefenseChain().isDoingAcrobatics()
        ) {
            stopEat();
            return Float.NEGATIVE_INFINITY;
        }
        dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer()) {
            if (dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                stopEat();
                return Float.NEGATIVE_INFINITY;
            }
        }
        if (!mod.getModSettings().isAutoEat()) {
            stopEat();
            return Float.NEGATIVE_INFINITY;
        }

        // 如果我们正在逃离岩浆，则不要进食（意大利面条式代码依赖关系）
        if (mod.getPlayer().isInLava()) {
            stopEat();
            return Float.NEGATIVE_INFINITY;
        }

        /*
        - 进食条件：
        - 我们饿了并且有合适的食物
            - 我们健康值低，也许有点饿
            - 我们健康值很低，即使一点点饿
        - 我们有点饿，有完美匹配的食物
          */
        // 我们处于危险中，现在不要进食！
        if (!mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isFalling(mod) ||
                mod.getPlayer().isBlocking() || shouldStop) {
            stopEat();
            return Float.NEGATIVE_INFINITY;
        }
        Pair<Integer, Optional<Item>> calculation = calculateFood(mod);
        int cachedFoodScore = calculation.getLeft();
        cachedPerfectFood = calculation.getRight();
        hasFood = cachedFoodScore > 0;
        // 如果我们请求了填饱但已经饱了，就停止
        if (requestFillup && mod.getPlayer().getHungerManager().getFoodLevel() >= 20) {
            requestFillup = false;
        }
        // 如果不再有食物，我们就不能再吃
        if (!hasFood) {
            requestFillup = false;
        }

        //FIXME 应该检查是否正在战斗
        if (hasFood && (needsToEat() || requestFillup) && cachedPerfectFood.isPresent() &&
                !mod.getMLGBucketChain().isChorusFruiting() && !mod.getPlayer().isBlocking()/* &&
                !areEnemiesNearby(mod)*/) {

            Item toUse = cachedPerfectFood.get();

            // 确保我们没有面对容器
            if (!LookHelper.tryAvoidingInteractable(mod)) {
                return Float.NEGATIVE_INFINITY;
            }
            startEat(mod, toUse);
        } else {
            stopEat();
        }

        Settings settings = mod.getModSettings();

        if (needsFood || cachedFoodScore < settings.getMinimumFoodAllowed()) {
            needsFood = cachedFoodScore < settings.getFoodUnitsToCollect();

            // 只在食物不足时收集
            // 如果用户输入了无效设置，机器人会卡在这里
            if (cachedFoodScore < settings.getFoodUnitsToCollect()) {
                setTask(new CollectFoodTask(settings.getFoodUnitsToCollect()));
                return 55f;
            }
        }


        // 食物进食是异步处理的
        return Float.NEGATIVE_INFINITY;
    }

    /**
     * 检查附近是否有敌人
     * @param mod AltoClef实例
     * @return 如果附近有敌人则返回true
     */
    private boolean areEnemiesNearby(AltoClef mod) {
        for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
            if (entity instanceof HostileEntity hostile && hostile.distanceTo(mod.getPlayer()) < (isTryingToEat?14:7)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isActive() {
        // 我们始终在检查食物
        return true;
    }

    @Override
    public String getName() {
        return "食物";
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopEat();
    }

    /**
     * 检查玩家是否需要进食
     * @return 如果需要进食则返回true
     */
    public boolean needsToEat() {
        if (!hasFood() || shouldStop) {
            return false;
        }


        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        int foodLevel = player.getHungerManager().getFoodLevel();
        float health = player.getHealth();

        if (foodLevel >= 20) {
            // 我们不能吃
            return false;
        }

        if (health <= 10) {
            return true;
        }
        //Debug.logMessage("FOOD: " + foodLevel + " -- HEALTH: " + health);

        // 如果我们绝望/需要立即治愈
        if (player.isOnFire() || player.hasStatusEffect(StatusEffects.WITHER) || health < config.alwaysEatWhenWitherOrFireAndHealthBelow) {
            return true;
        } else if (foodLevel > config.alwaysEatWhenBelowHunger) {
            if (health < config.alwaysEatWhenBelowHealth) {
                return true;
            }
        } else {
            // 我们有一半饥饿值
            return true;
        }


        // 如果我们饥饿单位少且有完美匹配的食物
        if (foodLevel < config.alwaysEatWhenBelowHungerAndPerfectFit && cachedPerfectFood.isPresent()) {
            int need = 20 - foodLevel;
            Item best = cachedPerfectFood.get();

            int fills = (ItemVer.getFoodComponent(best) != null) ? ItemVer.getFoodComponent(best).getHunger() : -1;
            return fills == need;
        }

        return false;
    }

    /**
     * 计算食物评分，找到最佳食物
     * @param mod AltoClef实例
     * @return 食物总单位数和最佳食物的配对
     */
    private Pair<Integer, Optional<Item>> calculateFood(AltoClef mod) {
        Item bestFood = null;
        double bestFoodScore = Double.NEGATIVE_INFINITY;
        int foodTotal = 0;
        ClientPlayerEntity player = mod.getPlayer();
        float health = player != null ? player.getHealth() : 20;
        //float toHeal = player != null? 20 - player.getHealth() : 0;
        float hunger = player != null ? player.getHungerManager().getFoodLevel() : 20;
        float saturation = player != null ? player.getHungerManager().getSaturationLevel() : 20;
        // 获取最佳食物项 + 计算食物总量
        for (ItemStack stack : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
            if (ItemVer.isFood(stack)) {
                // 忽略受保护的物品
                if (!ItemHelper.canThrowAwayStack(mod, stack)) continue;

                // 忽略蜘蛛眼
                if (stack.getItem() == Items.SPIDER_EYE) {
                    continue;
                }

                FoodComponentWrapper food = ItemVer.getFoodComponent(stack.getItem());

                assert food != null;
                float hungerIfEaten = Math.min(hunger + food.getHunger(), 20);
                float saturationIfEaten = Math.min(hungerIfEaten, saturation + food.getSaturationModifier());
                float gainedSaturation = (saturationIfEaten - saturation);
                float gainedHunger = (hungerIfEaten - hunger);
                float hungerNotFilled = 20 - hungerIfEaten;

                float saturationWasted = food.getSaturationModifier() - gainedSaturation;
                float hungerWasted = food.getHunger() - gainedHunger;

                boolean prioritizeSaturation = health < config.prioritizeSaturationWhenBelowHealth;
                float saturationGoodScore = prioritizeSaturation ? gainedSaturation * config.foodPickPrioritizeSaturationSaturationMultiplier : gainedSaturation;
                float saturationLossPenalty = prioritizeSaturation ? 0 : saturationWasted * config.foodPickSaturationWastePenaltyMultiplier;
                float hungerLossPenalty = hungerWasted * config.foodPickHungerWastePenaltyMultiplier;
                float hungerNotFilledPenalty = hungerNotFilled * config.foodPickHungerNotFilledPenaltyMultiplier;

                float score = saturationGoodScore - saturationLossPenalty - hungerLossPenalty - hungerNotFilledPenalty;

                if (stack.getItem() == Items.ROTTEN_FLESH) {
                    score -= config.foodPickRottenFleshPenalty;
                }
                if (score > bestFoodScore) {
                    bestFoodScore = score;
                    bestFood = stack.getItem();
                }

                foodTotal += Objects.requireNonNull(ItemVer.getFoodComponent(stack.getItem())).getHunger() * stack.getCount();
            }
        }

        return new Pair<>(foodTotal, Optional.ofNullable(bestFood));
    }

    /**
     * 检查是否需要立即进食（紧急情况）
     * @return 如果需要立即进食则返回true
     */
    public boolean needsToEatCritical() {
        return false;
    }

    /**
     * 检查是否拥有食物
     * @return 如果拥有食物则返回true
     */
    public boolean hasFood() {
        return hasFood;
    }

    /**
     * 设置是否应该停止进食
     * @param shouldStopInput 是否应该停止
     */
    public void shouldStop(boolean shouldStopInput) {
        shouldStop = shouldStopInput;
    }

    /**
     * 检查是否应该停止
     * @return 如果应该停止则返回true
     */
    public boolean isShouldStop() {
        return shouldStop;
    }

    /**
     * 食物链配置类，存储进食相关的配置参数
     */
    static class FoodChainConfig {
        public int alwaysEatWhenWitherOrFireAndHealthBelow = 6; // 当受到凋零或着火且健康值低于此值时总是进食
        public int alwaysEatWhenBelowHunger = 10; // 当饥饿值低于此值时总是进食
        public int alwaysEatWhenBelowHealth = 14; // 当健康值低于此值时总是进食
        public int alwaysEatWhenBelowHungerAndPerfectFit = 20 - 5; // 当饥饿值低于此值且有完美匹配的食物时进食
        public int prioritizeSaturationWhenBelowHealth = 8; // 当健康值低于此值时优先考虑饱和度
        public float foodPickPrioritizeSaturationSaturationMultiplier = 8; // 食物选择时优先考虑饱和度的倍数
        public float foodPickSaturationWastePenaltyMultiplier = 1; // 食物选择时浪费饱和度的惩罚倍数
        public float foodPickHungerWastePenaltyMultiplier = 2; // 食物选择时浪费饥饿值的惩罚倍数
        public float foodPickHungerNotFilledPenaltyMultiplier = 1; // 食物选择时饥饿值未填满的惩罚倍数
        public float foodPickRottenFleshPenalty = 100; // 食物选择时腐肉的惩罚值
        public float runDontEatMaxHealth = 3; // 逃跑时不进食的最大健康值
        public int runDontEatMaxHunger = 3; // 逃跑时不进食的最大饥饿值
        public int canTankHitsAndEatArmor = 15; // 可以承受攻击并进食的护甲值
        public int canTankHitsAndEatMaxHunger = 3; // 可以承受攻击并进食的最大饥饿值
    }
}
