package adris.altoclef.trackers;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerCollidedWithEntityEvent;
import adris.altoclef.mixins.PersistentProjectileEntityAccessor;
import adris.altoclef.trackers.blacklisting.EntityLocateBlacklist;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.ProjectileHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;

/**
 * 实体跟踪器 - 跟踪所有实体，以便我们能够搜索和获取它们
 */
@SuppressWarnings("rawtypes")
public class EntityTracker extends Tracker {

    // 物品掉落位置映射
    private final HashMap<Item, List<ItemEntity>> itemDropLocations = new HashMap<>();
    // 实体类型映射
    private final HashMap<Class, List<Entity>> entityMap = new HashMap<>();

    // 附近的实体列表
    private final List<Entity> closeEntities = new ArrayList<>();
    // 敌对生物列表
    private final List<LivingEntity> hostiles = new ArrayList<>();

    // 投射物列表
    private final List<CachedProjectile> projectiles = new ArrayList<>();

    // 玩家实体映射
    private final HashMap<String, PlayerEntity> playerMap = new HashMap<>();
    // 玩家最后坐标映射
    private final HashMap<String, Vec3d> playerLastCoordinates = new HashMap<>();

    // 实体定位黑名单
    private final EntityLocateBlacklist entityBlacklist = new EntityLocateBlacklist();

    // 玩家碰撞实体累积器
    private final HashMap<PlayerEntity, List<Entity>> entitiesCollidingWithPlayerAccumulator = new HashMap<>();
    // 玩家碰撞实体集合
    private final HashMap<PlayerEntity, HashSet<Entity>> entitiesCollidingWithPlayer = new HashMap<>();

    public EntityTracker(TrackerManager manager) {
        super(manager);

        // 监听玩家碰撞事件
        EventBus.subscribe(PlayerCollidedWithEntityEvent.class, evt -> registerPlayerCollision(evt.player, evt.other));
    }

    /**
     * 将可能有子类的类压缩成一个可区分的类类型。
     * 为了便于使用。
     *
     * @param type: 一个可能有'更简单'的类可以压缩的实体类
     * @return 给定实体类应该被读取/分类的方式。
     */
    private static Class squashType(Class type) {
        // 压缩类型以便于使用
        if (PlayerEntity.class.isAssignableFrom(type)) {
            return PlayerEntity.class;
        }
        return type;
    }

    /**
     * 注册玩家与实体的碰撞
     * @param player 玩家实体
     * @param entity 发生碰撞的实体
     */
    private void registerPlayerCollision(PlayerEntity player, Entity entity) {
        if (!entitiesCollidingWithPlayerAccumulator.containsKey(player)) {
            entitiesCollidingWithPlayerAccumulator.put(player, new ArrayList<>());
        }
        entitiesCollidingWithPlayerAccumulator.get(player).add(entity);
    }

    /**
     * 检查指定玩家是否与实体碰撞
     * @param player 玩家实体
     * @param entity 检查的实体
     * @return 是否碰撞
     */
    public boolean isCollidingWithPlayer(PlayerEntity player, Entity entity) {
        return entitiesCollidingWithPlayer.containsKey(player) && entitiesCollidingWithPlayer.get(player).contains(entity);
    }

    /**
     * 检查当前玩家是否与实体碰撞
     * @param entity 检查的实体
     * @return 是否碰撞
     */
    public boolean isCollidingWithPlayer(Entity entity) {
        return isCollidingWithPlayer(mod.getPlayer(), entity);
    }

    /**
     * 获取指定物品的最近掉落物
     * @param items 要查找的物品
     * @return 最近的物品实体
     */
    public Optional<ItemEntity> getClosestItemDrop(Item... items) {
        return getClosestItemDrop(mod.getPlayer().getPos(), items);
    }

    /**
     * 从指定位置获取指定物品的最近掉落物
     * @param position 检查位置
     * @param items 要查找的物品
     * @return 最近的物品实体
     */
    public Optional<ItemEntity> getClosestItemDrop(Vec3d position, Item... items) {
        return getClosestItemDrop(position, entity -> true, items);
    }

    /**
     * 从指定位置获取指定物品目标的最近掉落物
     * @param position 检查位置
     * @param items 要查找的物品目标
     * @return 最近的物品实体
     */
    public Optional<ItemEntity> getClosestItemDrop(Vec3d position, ItemTarget... items) {
        return getClosestItemDrop(position, entity -> true, items);
    }

    /**
     * 获取满足条件的指定物品的最近掉落物
     * @param acceptPredicate 接受条件
     * @param items 要查找的物品
     * @return 最近的物品实体
     */
    public Optional<ItemEntity> getClosestItemDrop(Predicate<ItemEntity> acceptPredicate, Item... items) {
        return getClosestItemDrop(mod.getPlayer().getPos(), acceptPredicate, items);
    }

    /**
     * 从指定位置获取满足条件的指定物品的最近掉落物
     * @param position 检查位置
     * @param acceptPredicate 接受条件
     * @param items 要查找的物品
     * @return 最近的物品实体
     */
    public Optional<ItemEntity> getClosestItemDrop(Vec3d position, Predicate<ItemEntity> acceptPredicate, Item... items) {
        ensureUpdated();
        ItemTarget[] tempTargetList = new ItemTarget[items.length];
        for (int i = 0; i < items.length; ++i) {
            tempTargetList[i] = new ItemTarget(items[i], 9999999);
        }
        return getClosestItemDrop(position, acceptPredicate, tempTargetList);
    }

    /**
     * 从指定位置获取满足条件的指定物品目标的最近掉落物
     * @param position 检查位置
     * @param acceptPredicate 接受条件
     * @param targets 要查找的物品目标
     * @return 最近的物品实体
     */
    public Optional<ItemEntity> getClosestItemDrop(Vec3d position, Predicate<ItemEntity> acceptPredicate, ItemTarget... targets) {
        ensureUpdated();
        if (targets.length == 0) {
            Debug.logError("您查询了零个物品的掉落位置... 很可能是一个输入错误。");
            return Optional.empty();
        }
        if (!itemDropped(targets)) {
            return Optional.empty();
        }

        ItemEntity closestEntity = null;
        float minCost = Float.POSITIVE_INFINITY;
        for (ItemTarget target : targets) {
            for (Item item : target.getMatches()) {
                if (!itemDropped(item)) continue;
                for (ItemEntity entity : itemDropLocations.get(item)) {
                    if (entityBlacklist.unreachable(entity)) continue;
                    if (!entity.getStack().getItem().equals(item)) continue;
                    if (!acceptPredicate.test(entity)) continue;

                    float cost = (float) BaritoneHelper.calculateGenericHeuristic(position, entity.getPos());
                    if (cost < minCost) {
                        minCost = cost;
                        closestEntity = entity;
                    }
                }
            }
        }
        return Optional.ofNullable(closestEntity);
    }

    /**
     * 获取指定类型的最近实体
     * @param entityTypes 要查找的实体类型
     * @return 最近的实体
     */
    public Optional<Entity> getClosestEntity(Class... entityTypes) {
        return getClosestEntity(mod.getPlayer().getPos(), entityTypes);
    }

    /**
     * 从指定位置获取指定类型的最近实体
     * @param position 检查位置
     * @param entityTypes 要查找的实体类型
     * @return 最近的实体
     */
    public Optional<Entity> getClosestEntity(Vec3d position, Class... entityTypes) {
        return this.getClosestEntity(position, (entity) -> true, entityTypes);
    }

    /**
     * 获取满足条件的指定类型的最近实体
     * @param acceptPredicate 接受条件
     * @param entityTypes 要查找的实体类型
     * @return 最近的实体
     */
    public Optional<Entity> getClosestEntity(Predicate<Entity> acceptPredicate, Class... entityTypes) {
        return getClosestEntity(mod.getPlayer().getPos(), acceptPredicate, entityTypes);
    }

    /**
     * 从指定位置获取满足条件的指定类型的最近实体
     * @param position 检查位置
     * @param acceptPredicate 接受条件
     * @param entityTypes 要查找的实体类型
     * @return 最近的实体
     */
    public Optional<Entity> getClosestEntity(Vec3d position, Predicate<Entity> acceptPredicate, Class... entityTypes) {
        Entity closestEntity = null;
        double minCost = Float.POSITIVE_INFINITY;
        for (Class toFind : entityTypes) {
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                if (entityMap.containsKey(toFind)) {
                    for (Entity entity : entityMap.get(toFind)) {
                        // 不接受已经不存在的实体
                        if (entityBlacklist.unreachable(entity)) continue;
                        if (!entity.isAlive()) continue;
                        if (!acceptPredicate.test(entity)) continue;
                        double cost = entity.squaredDistanceTo(position);
                        if (cost < minCost) {
                            minCost = cost;
                            closestEntity = entity;
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(closestEntity);
    }

    /**
     * 检查指定物品是否已掉落
     * @param items 要检查的物品
     * @return 是否已掉落
     */
    public boolean itemDropped(Item... items) {
        ensureUpdated();
        for (Item item : items) {
            if (itemDropLocations.containsKey(item)) {
                // 查找未被拉黑的物品
                for (ItemEntity entity : itemDropLocations.get(item)) {
                    if (!entityBlacklist.unreachable(entity)) return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查指定物品目标是否已掉落
     * @param targets 要检查的物品目标
     * @return 是否已掉落
     */
    public boolean itemDropped(ItemTarget... targets) {
        ensureUpdated();
        for (ItemTarget target : targets) {
            if (itemDropped(target.getMatches())) return true;
        }
        return false;
    }

    /**
     * 获取所有已掉落的物品列表
     * @return 已掉落的物品实体列表
     */
    public List<ItemEntity> getDroppedItems() {
        ensureUpdated();
        return itemDropLocations.values().stream().reduce(new ArrayList<>(), (result, drops) -> {
            result.addAll(drops);
            return result;
        });
    }

    /**
     * 检查是否找到了满足条件的指定类型实体
     * @param shouldAccept 接受条件
     * @param types 实体类型
     * @return 是否找到
     */
    public boolean entityFound(Predicate<Entity> shouldAccept, Class... types) {
        ensureUpdated();
        for (Class type : types) {
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                for (Entity entity : entityMap.getOrDefault(type, Collections.emptyList())) {
                    if (shouldAccept.test(entity))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查是否找到了指定类型的实体
     * @param types 实体类型
     * @return 是否找到
     */
    public boolean entityFound(Class... types) {
        return entityFound(check -> true, types);
    }

    /**
     * 获取追踪的指定类型实体列表
     * @param type 实体类型
     * @return 实体列表
     */
    public <T extends Entity> List<T> getTrackedEntities(Class<T> type) {
        ensureUpdated();
        if (!entityFound(type)) {
            return Collections.emptyList();
        }
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            //noinspection unchecked
            return (List<T>) entityMap.get(type);
        }
    }

    /**
     * 获取在我们的交互范围内的所有实体
     */
    public List<Entity> getCloseEntities() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return closeEntities;
        }
    }

    /**
     * 获取我们已缓存/存储信息的投射物列表。
     */
    public List<CachedProjectile> getProjectiles() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return projectiles;
        }
    }

    /**
     * 获取敌对生物列表
     * @return 敌对生物列表
     */
    public List<LivingEntity> getHostiles() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return hostiles;
        }
    }

    /**
     * 玩家是否已加载/在渲染距离内?
     *
     * @param name 多人游戏服务器上的用户名
     */
    public boolean isPlayerLoaded(String name) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return playerMap.containsKey(name);
        }
    }

    /**
     * 获取我们最后看到玩家的位置，如果我们看到过他们。
     *
     * @return 多人游戏服务器上的用户名。
     */
    public Optional<Vec3d> getPlayerMostRecentPosition(String name) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return Optional.ofNullable(playerLastCoordinates.getOrDefault(name, null));
        }
    }

    /**
     * 获取用户名对应的玩家实体，如果他们已加载/在渲染距离内。
     *
     * @param name 多人游戏服务器上的用户名。
     */
    public Optional<PlayerEntity> getPlayerEntity(String name) {
        if (isPlayerLoaded(name)) {
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                return Optional.of(playerMap.get(name));
            }
        }
        return Optional.empty();
    }

    /**
     * 通知实体跟踪器我们无法到达这个实体。
     */
    public void requestEntityUnreachable(Entity entity) {
        entityBlacklist.blackListItem(mod, entity, 3);
    }

    /**
     * 判断我们是否认为此实体不可到达。
     */
    public boolean isEntityReachable(Entity entity) {
        return !entityBlacklist.unreachable(entity);
    }

    @Override
    protected synchronized void updateState() {
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            itemDropLocations.clear();
            entityMap.clear();
            closeEntities.clear();
            projectiles.clear();
            hostiles.clear();
            playerMap.clear();
            if (MinecraftClient.getInstance().world == null) return;

            // 存储/注册此帧的所有累积玩家碰撞。
            entitiesCollidingWithPlayer.clear();
            for (Map.Entry<PlayerEntity, List<Entity>> collisions : entitiesCollidingWithPlayerAccumulator.entrySet()) {
                entitiesCollidingWithPlayer.put(collisions.getKey(), new HashSet<>());
                entitiesCollidingWithPlayer.get(collisions.getKey()).addAll(collisions.getValue());
            }
            entitiesCollidingWithPlayerAccumulator.clear();

            // 遍历所有实体并跟踪它们
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {

                // 根据类型分类。有些类型可能会被"压缩"或合并为一个。
                Class type = entity.getClass();
                type = squashType(type);

                //noinspection ConstantConditions
                if (entity == null || !entity.isAlive()) continue;

                // 不要分类我们自己的玩家。
                if (type == PlayerEntity.class && entity.equals(mod.getPlayer())) continue;

                if (!entityMap.containsKey(type)) {
                    entityMap.put(type, new ArrayList<>());
                }
                entityMap.get(type).add(entity);

                if (mod.getControllerExtras().inRange(entity)) {
                    closeEntities.add(entity);
                }

                if (entity instanceof ItemEntity ientity) {
                    Item droppedItem = ientity.getStack().getItem();

                    // 只关心已落地的物品实体
                    if (ientity.isOnGround() || ientity.isTouchingWater() || WorldHelper.isSolidBlock(ientity.getBlockPos().down(2)) || WorldHelper.isSolidBlock(ientity.getBlockPos().down(3))) {
                        if (!itemDropLocations.containsKey(droppedItem)) {
                            itemDropLocations.put(droppedItem, new ArrayList<>());
                        }
                        itemDropLocations.get(droppedItem).add(ientity);
                    }
                }
                if (entity instanceof MobEntity) {
                    if (EntityHelper.isAngryAtPlayer(mod, entity)) {

                        // 检查生物是否面向我们或是否足够近
                        boolean closeEnough = entity.isInRange(mod.getPlayer(), 26);

                        //Debug.logInternal("TARGET: " + hostile.is);
                        if (closeEnough) {
                            hostiles.add((LivingEntity) entity);
                        }
                    }
                } else if (entity instanceof ProjectileEntity projEntity) {
                    if (!mod.getBehaviour().shouldAvoidDodgingProjectile(entity)) {
                        CachedProjectile proj = new CachedProjectile();

                        boolean inGround = false;
                        // 获取投射物 "inGround" 变量
                        if (entity instanceof PersistentProjectileEntity) {
                            inGround = ((PersistentProjectileEntityAccessor) entity).isInGround();
                        }

                        // 忽略一些无害的投射物
                        if (projEntity instanceof FishingBobberEntity || projEntity instanceof EnderPearlEntity || projEntity instanceof ExperienceBottleEntity)
                            continue;

                        if (!inGround) {
                            proj.position = projEntity.getPos();
                            proj.velocity = projEntity.getVelocity();
                            proj.gravity = ProjectileHelper.hasGravity(projEntity) ? ProjectileHelper.ARROW_GRAVITY_ACCEL : 0;
                            proj.projectileType = projEntity.getClass();
                            projectiles.add(proj);
                        }
                    }
                } else if (entity instanceof PlayerEntity player) {
                    String name = player.getName().getString();
                    playerMap.put(name, player);
                    playerLastCoordinates.put(name, player.getPos());
                }
            }
        }
    }

    @Override
    protected void reset() {
        // 设置为脏状态会清除其他所有内容。
        entityBlacklist.clear();
    }
}
