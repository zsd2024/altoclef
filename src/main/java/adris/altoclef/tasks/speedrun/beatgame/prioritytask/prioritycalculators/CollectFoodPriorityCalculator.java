package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.function.Predicate;

import static adris.altoclef.tasks.resources.CollectFoodTask.*;

/**
 * 食物收集优先级计算器
 * CollectFoodTask.java的部分复制，可能需要想办法使用那里的方法而不是复制
 * 这个类是必要的，因为如果计算了某个东西的优先级，然后任务去往其他地方，可能会导致卡住
 */

public class CollectFoodPriorityCalculator extends ItemPriorityCalculator {

    private final AltoClef mod; // AltoClef实例
    private final double foodUnits; // 食物单位

    public CollectFoodPriorityCalculator(AltoClef mod ,double foodUnits) {
        super(Integer.MAX_VALUE,Integer.MAX_VALUE);
        this.mod = mod;
        this.foodUnits = foodUnits;
    }

    /**
     * 计算优先级
     * @param count 食物数量
     * @return 优先级值
     */
    public double calculatePriority(int count) {
        double distance = getDistance(mod);

        double multiplier = 1;
        double foodPotential = CollectFoodTask.calculateFoodPotential(mod);

        // 防止在没有任何食物的情况下进入下界
        if (Double.isInfinite(distance) && foodPotential < foodUnits) return 0.1d;

        Optional<BlockPos> hay = mod.getBlockScanner().getNearestBlock(Blocks.HAY_BLOCK);
        if ((hay.isPresent() && WorldHelper.inRangeXZ(hay.get(),mod.getPlayer().getBlockPos(),75))|| mod.getEntityTracker().itemDropped(Items.HAY_BLOCK)) {
            multiplier = 50;
        }

        if (foodPotential > foodUnits) {
            if (foodPotential > foodUnits+20) return Double.NEGATIVE_INFINITY;

            if (distance > 10 && hay.isEmpty()) return Double.NEGATIVE_INFINITY;

            return 17 / distance * (30 / (count / 2d))*multiplier;
        }

        if (foodPotential < 10) {
            multiplier = Math.max(11d / foodPotential,22);
        }
        return 33 / distance * 37 * multiplier;
    }

    /**
     * 获取食物来源距离
     * @param mod AltoClef实例
     * @return 最近食物来源的距离
     */
    private double getDistance(AltoClef mod) {
        PlayerEntity player = mod.getPlayer();

        // 从地面拾取食物
        for (Item item : ITEMS_TO_PICK_UP) {
            double dist  = this.pickupTaskOrNull(mod, item);
            if (dist != Double.NEGATIVE_INFINITY) {
                return dist;
            }
        }
        // 拾取地面上的生食/熟食
        for (CookableFoodTarget cookable : COOKABLE_FOODS) {
            double dist = this.pickupTaskOrNull(mod, cookable.getRaw(), 20);
            if (dist == Double.NEGATIVE_INFINITY) dist = this.pickupTaskOrNull(mod, cookable.getCooked(), 40);

            if (dist != Double.NEGATIVE_INFINITY) {
                return dist;
            }
        }

        // 干草块
        double hayTaskBlock = this.pickupBlockTaskOrNull(mod, Blocks.HAY_BLOCK, Items.HAY_BLOCK, 300);
        if (hayTaskBlock != Double.NEGATIVE_INFINITY) {
            return hayTaskBlock;
        }
        // 作物
        for (CropTarget target : CROPS) {
            // 如果作物在附近。不要重新种植，因为无所谓。
            double t = pickupBlockTaskOrNull(mod, target.cropBlock, target.cropItem, (blockPos -> {
                BlockState s = mod.getWorld().getBlockState(blockPos);
                Block b = s.getBlock();
                if (b instanceof CropBlock) {
                    boolean isWheat = !(b instanceof PotatoesBlock || b instanceof CarrotsBlock || b instanceof BeetrootsBlock);
                    if (isWheat) {
                        // 区块需要加载才能检查小麦成熟度。
                        if (!mod.getChunkTracker().isChunkLoaded(blockPos)) {
                            return false;
                        }
                        // 如果不是成熟/完全长成的小麦，则不选取。
                        CropBlock crop = (CropBlock) b;
                        return crop.isMature(s);
                    }
                }
                // 无法破坏。
                return WorldHelper.canBreak(blockPos);
                // 我们不是小麦，所以不要拒绝。
            }), 96);
            if (t != Double.NEGATIVE_INFINITY) {
                return t;
            }
        }
        // 熟食
        double bestScore = 0;
        Entity bestEntity = null;
        Predicate<Entity> notBaby = entity -> entity instanceof LivingEntity livingEntity && !livingEntity.isBaby();

        for (CookableFoodTarget cookable : COOKABLE_FOODS) {
            if (!mod.getEntityTracker().entityFound(cookable.mobToKill)) continue;
            Optional<Entity> nearest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), notBaby, cookable.mobToKill);
            if (nearest.isEmpty()) continue; // ?? 这有一次崩溃？
            int hungerPerformance = cookable.getCookedUnits();
            double sqDistance = nearest.get().squaredDistanceTo(mod.getPlayer());
            double score = (double) 100 * hungerPerformance / (sqDistance);
            if (cookable.isFish()) {
                score = 0;
            }
            if (score > bestScore) {
                bestScore = score;
                bestEntity = nearest.get();
            }
        }
        if (bestEntity != null) {
            return bestEntity.distanceTo(player);
        }

        // 甜浆果（从作物中分离出来，因为它们应该比其他东西有更低的优先级，因为它们很糟糕）
        double berryPickup = pickupBlockTaskOrNull(mod, Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES, 96);
        if (berryPickup != Double.NEGATIVE_INFINITY) {
            return berryPickup;
        }

        return Double.POSITIVE_INFINITY;
    }

    /**
     * 拾取方块任务或空
     * @param mod AltoClef实例
     * @param blockToCheck 要检查的方块
     * @param itemToGrab 要拾取的物品
     * @param maxRange 最大范围
     * @return 任务距离或空值
     */
    private double pickupBlockTaskOrNull(AltoClef mod, Block blockToCheck, Item itemToGrab, double maxRange) {
        return pickupBlockTaskOrNull(mod, blockToCheck, itemToGrab, toAccept -> true, maxRange);
    }

    /**
     * 拾取方块任务或空
     * @param mod AltoClef实例
     * @param blockToCheck 要检查的方块
     * @param itemToGrab 要拾取的物品
     * @param accept 接受条件
     * @param maxRange 最大范围
     * @return 任务距离或空值
     */
    private double pickupBlockTaskOrNull(AltoClef mod, Block blockToCheck, Item itemToGrab, Predicate<BlockPos> accept, double maxRange) {
        Predicate<BlockPos> acceptPlus = (blockPos) -> {
            if (!WorldHelper.canBreak(blockPos)) return false;
            return accept.test(blockPos);
        };
        Optional<BlockPos> nearestBlock = mod.getBlockScanner().getNearestBlock(mod.getPlayer().getPos(), acceptPlus, blockToCheck);

        if (nearestBlock.isPresent() && !nearestBlock.get().isWithinDistance(mod.getPlayer().getPos(), maxRange)) {
            nearestBlock = Optional.empty();
        }

        Optional<ItemEntity> nearestDrop = Optional.empty();
        if (mod.getEntityTracker().itemDropped(itemToGrab)) {
            nearestDrop = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), itemToGrab);
        }


        if (nearestDrop.isPresent()) {
            return nearestDrop.get().distanceTo(mod.getPlayer());
        }
        if (nearestBlock.isPresent()) {
            return Math.sqrt(mod.getPlayer().squaredDistanceTo(WorldHelper.toVec3d(nearestBlock.get())));
        }

        return Double.NEGATIVE_INFINITY;
    }

    /**
     * 拾取任务或空
     * @param mod AltoClef实例
     * @param itemToGrab 要拾取的物品
     * @return 任务距离或空值
     */
    private double pickupTaskOrNull(AltoClef mod, Item itemToGrab) {
        return pickupTaskOrNull(mod, itemToGrab, Double.POSITIVE_INFINITY);
    }

    /**
     * 拾取任务或空
     * @param mod AltoClef实例
     * @param itemToGrab 要拾取的物品
     * @param maxRange 最大范围
     * @return 任务距离或空值
     */
    private double pickupTaskOrNull(AltoClef mod, Item itemToGrab, double maxRange) {
        Optional<ItemEntity> nearestDrop = Optional.empty();
        if (mod.getEntityTracker().itemDropped(itemToGrab)) {
            nearestDrop = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), itemToGrab);
        }
        if (nearestDrop.isPresent()) {
            if (nearestDrop.get().isInRange(mod.getPlayer(), maxRange)) {
                if (mod.getItemStorage().getSlotsThatCanFitInPlayerInventory(nearestDrop.get().getStack(), false).isEmpty()) {
                    Optional<Slot> slot = StorageHelper.getGarbageSlot(mod);

                    // 如果为空我应该怎么办
                    if (slot.isPresent()) {
                        ItemStack stack = StorageHelper.getItemStackInSlot(slot.get());
                        if (ItemVer.isFood(stack.getItem())) {
                            // 计算优先级，如果地面上的物品优先级比我们要因为这个而扔掉的物品更低
                            // 不要拾取，否则我们会陷入无限循环
                            int inventoryCost = ItemVer.getFoodComponent(stack.getItem()).getHunger() * stack.getCount();

                            double hunger = 0;
                            if (ItemVer.isFood(itemToGrab)) {
                                hunger = ItemVer.getFoodComponent(itemToGrab).getHunger();
                            } else if (itemToGrab.equals(Items.WHEAT)) {
                                hunger += ItemVer.getFoodComponent(Items.BREAD).getHunger() / 3d;
                            } else {
                                mod.log("未知食物物品: " + itemToGrab);
                            }
                            int groundCost = (int) (hunger * nearestDrop.get().getStack().getCount());

                            if (inventoryCost > groundCost) return Double.NEGATIVE_INFINITY;
                        }
                    }
                }

                return nearestDrop.get().distanceTo(mod.getPlayer());
            }
        }
        return Double.NEGATIVE_INFINITY;
    }


}
