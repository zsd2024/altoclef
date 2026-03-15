package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.container.SmeltInSmokerTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.slots.SmokerSlot;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 收集食物任务
 * 用于收集各种食物，包括击杀动物、收集作物、拾取现成食物等
 */
public class CollectFoodTask extends Task {


    // 表示优先级从高到低的可烹饪食物列表
    public static final CookableFoodTarget[] COOKABLE_FOODS = new CookableFoodTarget[]{
            new CookableFoodTarget("beef", CowEntity.class),
            new CookableFoodTarget("porkchop", PigEntity.class),
            new CookableFoodTarget("chicken", ChickenEntity.class),
            new CookableFoodTarget("mutton", SheepEntity.class),
            new CookableFoodTarget("rabbit", RabbitEntity.class)
    };

    // 可拾取的食物物品列表
    public static final Item[] ITEMS_TO_PICK_UP = new Item[]{
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.GOLDEN_APPLE,
            Items.GOLDEN_CARROT,
            Items.BREAD,
            Items.BAKED_POTATO
    };

    // 作物目标列表
    public static final CropTarget[] CROPS = new CropTarget[]{
            new CropTarget(Items.WHEAT, Blocks.WHEAT),
            new CropTarget(Items.CARROT, Blocks.CARROTS)
    };

    private final double unitsNeeded; // 需要的食物单位数量
    private final TimerGame checkNewOptionsTimer = new TimerGame(10); // 检查新选项的计时器
    private final SmeltInSmokerTask smeltTask = null; // 烟熏炉熔炼任务
    private Task currentResourceTask = null; // 当前资源任务

    public CollectFoodTask(double unitsNeeded) {
        this.unitsNeeded = unitsNeeded;
    }

    /**
     * 计算物品的食物潜力值
     * @param food 食物物品堆栈
     * @return 食物潜力值
     */
    private static double getFoodPotential(ItemStack food) {
        if (food == null) return 0;
        int count = food.getCount();
        if (count <= 0) return 0;
        for (CookableFoodTarget cookable : COOKABLE_FOODS) {
            if (food.getItem() == cookable.getRaw()) {
                assert ItemVer.getFoodComponent(cookable.getCooked()) != null;
                return count * ItemVer.getFoodComponent(cookable.getCooked()).getHunger();
            }
        }

        //面包逻辑
        assert ItemVer.getFoodComponent( Items.BREAD) != null;

        if (food.getItem().equals(Items.HAY_BLOCK)) {
            return 3* ItemVer.getFoodComponent(Items.BREAD).getHunger()*count;
        }
        if (food.getItem().equals(Items.WHEAT)) {
            return (double) (ItemVer.getFoodComponent(Items.BREAD).getHunger() * count) /3;
        }

        // 我们只是一个普通物品
        if (ItemVer.isFood(food.getItem())) {
            assert ItemVer.getFoodComponent(food.getItem()) != null;
            return count * ItemVer.getFoodComponent(food.getItem()).getHunger();
        }
        return 0;
    }

    /**
     * 计算如果将所有原材料转换为食物后的食物单位数
     * @param mod AltoClef实例
     * @return 食物潜力值
     */
    @SuppressWarnings("RedundantCast")
    public static double calculateFoodPotential(AltoClef mod) {
        double potentialFood = 0;
        for (ItemStack food : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
            potentialFood += getFoodPotential(food);
        }
        int potentialBread = (int) (mod.getItemStorage().getItemCount(Items.WHEAT) / 3) + mod.getItemStorage().getItemCount(Items.HAY_BLOCK) * 3;
        potentialFood += Objects.requireNonNull(ItemVer.getFoodComponent( Items.BREAD)).getHunger() * potentialBread;
        // 检查熔炼
        ScreenHandler screen = mod.getPlayer().currentScreenHandler;
        if (screen instanceof SmokerScreenHandler) {
            potentialFood += getFoodPotential(StorageHelper.getItemStackInSlot(SmokerSlot.INPUT_SLOT_MATERIALS));
            potentialFood += getFoodPotential(StorageHelper.getItemStackInSlot(SmokerSlot.OUTPUT_SLOT));
        }
        return potentialFood;
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        mod.getBehaviour().push();
        // 保护所有食物
        mod.getBehaviour().addProtectedItems(ITEMS_TO_PICK_UP);

        // 允许我们消耗食物
        /*
        for (CookableFoodTarget food : COOKABLE_FOODS)
            mod.getBehaviour().addProtectedItems(food.getRaw(), food.getCooked());
            mod.getBehaviour().addProtectedItems(crop.cropItem);
        }
         */
        mod.getBehaviour().addProtectedItems(Items.HAY_BLOCK, Items.SWEET_BERRIES);
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        blackListChickenJockeys(mod);

        List<BlockPos> haysPos = mod.getBlockScanner().getKnownLocations(Blocks.HAY_BLOCK);
        for (BlockPos HaysPos : haysPos) {
            BlockPos haysUpPos = HaysPos.up();
            if (mod.getWorld().getBlockState(haysUpPos).getBlock() == Blocks.CARVED_PUMPKIN) {
                Debug.logMessage("Blacklisting pillage hay bales.");
                mod.getBlockScanner().requestBlockUnreachable(HaysPos, 0);
            }
        }
        // 如果我们之前正在熔炼，继续熔炼
        if (smeltTask != null && smeltTask.isActive() && !smeltTask.isFinished()) {
            // TODO: 如果我们没有烹饪材料，取消
            setDebugState("烹饪中...");
            return smeltTask;
        }

        if (checkNewOptionsTimer.elapsed()) {
            // 尝试新的资源任务
            checkNewOptionsTimer.reset();
            currentResourceTask = null;
        }

        if (currentResourceTask != null && currentResourceTask.isActive() && !currentResourceTask.isFinished() && !currentResourceTask.thisOrChildAreTimedOut()) {
            return currentResourceTask;
        }

        // 计算潜力
        double potentialFood = calculateFoodPotential(mod);
        if (potentialFood >= unitsNeeded) {
            // 转换我们的原材料
            // 计划:
            // - 如果我们有干草/小麦，将其制成面包
            // - 如果我们有生食，将它们全部熔炼

            // 转换干草+小麦 -> 面包
            if (mod.getItemStorage().getItemCount(Items.WHEAT) >= 3) {
                setDebugState("制作面包");
                Item[] w = new Item[]{Items.WHEAT};
                Item[] o = null;
                // jank
                currentResourceTask = new CraftInTableTask(new RecipeTarget(Items.BREAD, 99999999, CraftingRecipe.newShapedRecipe("bread", new Item[][]{w, w, w, o, o, o, o, o, o}, 1)), false, false);
                return currentResourceTask;
            }
            if (mod.getItemStorage().getItemCount(Items.HAY_BLOCK) >= 1) {
                setDebugState("制作小麦");
                Item[] o = null;
                currentResourceTask = new CraftInInventoryTask(new RecipeTarget(Items.WHEAT, 99999999, CraftingRecipe.newShapedRecipe("wheat", new Item[][]{new Item[]{Items.HAY_BLOCK}, o, o, o}, 9)), false, false);
                return currentResourceTask;
            }
            // 转换生食 -> 熟食

            /*for (CookableFoodTarget cookable : COOKABLE_FOODS) {
                int rawCount = mod.getItemStorage().getItemCount(cookable.getRaw());
                if (rawCount > 0) {
                    //Debug.logMessage("STARTING COOK OF " + cookable.getRaw().getTranslationKey());
                    int toSmelt = rawCount + mod.getItemStorage().getItemCount(cookable.getCooked());
                    smeltTask = new SmeltInSmokerTask(new SmeltTarget(new ItemTarget(cookable.cookedFood, toSmelt), new ItemTarget(cookable.rawFood, rawCount)));
                    smeltTask.ignoreMaterials();
                    return smeltTask;
                }
            }*/
        } else {
            // 从地面上拾取食物
            for (Item item : ITEMS_TO_PICK_UP) {
                Task t = this.pickupTaskOrNull(mod, item);
                if (t != null) {
                    setDebugState("拾取食物: " + item.getTranslationKey());
                    currentResourceTask = t;
                    return currentResourceTask;
                }
            }
            // 拾取地面上的生/熟食
            for (CookableFoodTarget cookable : COOKABLE_FOODS) {
                Task t = this.pickupTaskOrNull(mod, cookable.getRaw(), 20);
                if (t == null) t = this.pickupTaskOrNull(mod, cookable.getCooked(), 40);
                if (t != null) {
                    setDebugState("拾取可烹饪食物");
                    currentResourceTask = t;
                    return currentResourceTask;
                }
            }
            // 干草块
            Task hayTaskBlock = this.pickupBlockTaskOrNull(mod, Blocks.HAY_BLOCK, Items.HAY_BLOCK, 300);
            if (hayTaskBlock != null) {
                setDebugState("收集干草");
                currentResourceTask = hayTaskBlock;
                return currentResourceTask;
            }
            // 作物
            for (CropTarget target : CROPS) {
                // 如果附近有作物。不重新种植，因为我们不关心
                Task t = pickupBlockTaskOrNull(mod, target.cropBlock, target.cropItem, (blockPos -> {
                    BlockState s = mod.getWorld().getBlockState(blockPos);
                    Block b = s.getBlock();
                    if (b instanceof CropBlock) {
                        boolean isWheat = !(b instanceof PotatoesBlock || b instanceof CarrotsBlock || b instanceof BeetrootsBlock);
                        if (isWheat) {
                            // 检查小麦成熟度需要加载区块
                            if (!mod.getChunkTracker().isChunkLoaded(blockPos)) {
                                return false;
                            }
                            // 如果不是成熟/完全生长的小麦，则修剪
                            CropBlock crop = (CropBlock) b;
                            return crop.isMature(s);
                        }
                    }
                    // 不可破坏
                    return WorldHelper.canBreak(blockPos);
                    // 我们不是小麦所以不要拒绝
                }), 96);
                if (t != null) {
                    setDebugState("收获 " + target.cropItem.getTranslationKey());
                    currentResourceTask = t;
                    return currentResourceTask;
                }
            }
            // 熟食
            double bestScore = 0;
            Entity bestEntity = null;
            Item bestRawFood = null;
            Predicate<Entity> notBaby = entity -> entity instanceof LivingEntity livingEntity && !livingEntity.isBaby();

            for (CookableFoodTarget cookable : COOKABLE_FOODS) {
                if (!mod.getEntityTracker().entityFound(cookable.mobToKill)) continue;
                Optional<Entity> nearest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(),notBaby ,cookable.mobToKill);
                if (nearest.isEmpty()) continue; // ?? 这次崩溃了一次？
                int hungerPerformance = cookable.getCookedUnits();
                double sqDistance = nearest.get().squaredDistanceTo(mod.getPlayer());
                double score = (double) 100 * hungerPerformance / (sqDistance);
                if (cookable.isFish()) {
                    score = 0;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestEntity = nearest.get();
                    bestRawFood = cookable.getRaw();
                }
            }
            if (bestEntity != null) {
                setDebugState("击杀 " + bestEntity.getType().getTranslationKey());
                currentResourceTask = killTaskOrNull(bestEntity, notBaby, bestRawFood);
                return currentResourceTask;
            }

            // 甜浆果（与作物分开因为它们的优先级应该比其他所有东西都低，因为它们很烂）
            Task berryPickup = pickupBlockTaskOrNull(mod, Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES, 96);
            if (berryPickup != null) {
                setDebugState("获取甜浆果（没有更好的食物了）");
                currentResourceTask = berryPickup;
                return currentResourceTask;
            }
        }

        // 寻找食物
        setDebugState("搜索中...");
        return new TimeoutWanderTask();
    }

    /**
     * 将鸡骑士列入黑名单（鸡骑着敌对生物）
     * @param mod AltoClef实例
     */
    static void blackListChickenJockeys(AltoClef mod) {
        if (mod.getEntityTracker().entityFound(ChickenEntity.class)) {
            Optional<Entity> chickens = mod.getEntityTracker().getClosestEntity(ChickenEntity.class);
            if (chickens.isPresent()) {
                Iterable<Entity> entities = mod.getWorld().getEntities();
                for (Entity entity : entities) {
                    if (entity instanceof HostileEntity || entity instanceof SlimeEntity) {
                        if (chickens.get().hasPassenger(entity)) {
                            if (mod.getEntityTracker().isEntityReachable(entity)) {
                                Debug.logMessage("将鸡骑士列入黑名单。");
                                mod.getEntityTracker().requestEntityUnreachable(chickens.get());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    public boolean isFinished() {
        return StorageHelper.calculateInventoryFoodScore() >= unitsNeeded;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof CollectFoodTask task) {
            return task.unitsNeeded == unitsNeeded;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "收集 " + unitsNeeded + " 单位食物。";
    }

    /**
     * 返回一个挖掘方块并拾取其产出的的任务
     * 如果任务无法合理运行则返回null
     */
    private Task pickupBlockTaskOrNull(AltoClef mod, Block blockToCheck, Item itemToGrab, Predicate<BlockPos> accept, double maxRange) {
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
            return pickupTaskOrNull(mod,itemToGrab);
        }
        if (nearestBlock.isPresent()) {
            return new DoToClosestBlockTask(DestroyBlockTask::new, acceptPlus, blockToCheck);
        }

        return null;
    }

    private Task pickupBlockTaskOrNull(AltoClef mod, Block blockToCheck, Item itemToGrab, double maxRange) {
        return pickupBlockTaskOrNull(mod, blockToCheck, itemToGrab, toAccept -> true, maxRange);
    }

    private Task killTaskOrNull(Entity entity, Predicate<Entity> entityPredicate, Item itemToGrab) {
        return new KillAndLootTask(entity.getClass(), entityPredicate, new ItemTarget(itemToGrab, 1));
    }

    /**
     * 返回一个拾取掉落物品的任务
     * 如果任务无法合理运行则返回null
     */
    private Task pickupTaskOrNull(AltoClef mod, Item itemToGrab, double maxRange) {
        Optional<ItemEntity> nearestDrop = Optional.empty();
        if (mod.getEntityTracker().itemDropped(itemToGrab)) {
            nearestDrop = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), itemToGrab);
        }
        if (nearestDrop.isPresent()) {
            if (nearestDrop.get().isInRange(mod.getPlayer(), maxRange)) {
                if (mod.getItemStorage().getSlotsThatCanFitInPlayerInventory(nearestDrop.get().getStack(), false).isEmpty()) {
                    Optional<Slot> slot = StorageHelper.getGarbageSlot(mod);

                    // 如果它是空的我该怎么办
                    if (slot.isPresent()) {
                        ItemStack stack = StorageHelper.getItemStackInSlot(slot.get());
                        if (ItemVer.isFood(stack.getItem())) {
                            // 计算优先级，如果地面上的物品优先级低于我们因它而要扔掉的物品
                            // 不要拾取它，否则我们会陷入无限循环
                            int inventoryCost = ItemVer.getFoodComponent(stack.getItem()).getHunger() * stack.getCount();

                            double hunger = 0;
                            if (ItemVer.isFood(itemToGrab)) {
                                hunger = ItemVer.getFoodComponent(itemToGrab).getHunger();
                            } else if (itemToGrab.equals(Items.WHEAT)) {
                                hunger += ItemVer.getFoodComponent(Items.BREAD).getHunger()/3d;
                            } else {
                                mod.log("未知食物物品: "+itemToGrab);
                            }
                            int groundCost = (int) (hunger * nearestDrop.get().getStack().getCount());

                            if (inventoryCost > groundCost) return null;
                        }
                    }
                }
                return new PickupDroppedItemTask(new ItemTarget(itemToGrab), true);
            }
        }
        return null;
    }

    private Task pickupTaskOrNull(AltoClef mod, Item itemToGrab) {
        return pickupTaskOrNull(mod, itemToGrab, Double.POSITIVE_INFINITY);
    }

    /**
     * 可烹饪食物目标类
     * 定义了可以用来制作熟食的生食
     */
    @SuppressWarnings("rawtypes")
    public static class CookableFoodTarget {
        public String rawFood; // 生食名称
        public String cookedFood; // 熟食名称
        public Class mobToKill; // 需要击杀的生物类型

        public CookableFoodTarget(String rawFood, String cookedFood, Class mobToKill) {
            this.rawFood = rawFood;
            this.cookedFood = cookedFood;
            this.mobToKill = mobToKill;
        }

        public CookableFoodTarget(String rawFood, Class mobToKill) {
            this(rawFood, "cooked_" + rawFood, mobToKill);
        }

        public Item getRaw() {
            return Objects.requireNonNull(TaskCatalogue.getItemMatches(rawFood))[0];
        }

        public Item getCooked() {
            return Objects.requireNonNull(TaskCatalogue.getItemMatches(cookedFood))[0];
        }

        public int getCookedUnits() {
            assert ItemVer.getFoodComponent(getCooked()) != null;
            return ItemVer.getFoodComponent(getCooked()).getHunger();
        }

        public boolean isFish() {
            return false;
        }
    }

    /**
     * 可烹饪鱼类食物目标类
     * 继承自CookableFoodTarget，专门用于鱼类
     */
    @SuppressWarnings("rawtypes")
    private static class CookableFoodTargetFish extends CookableFoodTarget {

        public CookableFoodTargetFish(String rawFood, Class mobToKill) {
            super(rawFood, mobToKill);
        }

        @Override
        public boolean isFish() {
            return true;
        }
    }

    /**
     * 作物目标类
     * 定义了可收获的作物
     */
    public static class CropTarget {
        public Item cropItem; // 作物物品
        public Block cropBlock; // 作物方块

        public CropTarget(Item cropItem, Block cropBlock) {
            this.cropItem = cropItem;
            this.cropBlock = cropBlock;
        }
    }
}
