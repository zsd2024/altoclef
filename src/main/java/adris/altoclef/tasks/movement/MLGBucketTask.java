package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.control.InputControls;
import adris.altoclef.multiversion.DamageSourceVer;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.serialization.ItemDeserializer;
import adris.altoclef.util.serialization.ItemSerializer;
import baritone.Baritone;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 水桶缓冲任务 - 当玩家掉落时使用水桶进行缓冲（MLG - 灌溉水桶）
 */
public class MLGBucketTask extends Task {

    private static MLGClutchConfig _config;

    static {
        ConfigHelper.loadConfig("configs/mlg_clutch_settings.json", MLGClutchConfig::new, MLGClutchConfig.class, newConfig -> _config = newConfig);
    }

    private BlockPos placedPos; // 放置位置
    private BlockPos movingTorwards; // 移动方向

    /**
     * 检查指定位置是否为熔岩
     * @param pos 位置
     * @return 是否为熔岩
     */
    private static boolean isLava(BlockPos pos) {
        assert MinecraftClient.getInstance().world != null;
        return MinecraftClient.getInstance().world.getBlockState(pos).getBlock() == Blocks.LAVA;
    }

    /**
     * 熔岩是否能够保护免受掉落伤害
     * @param pos 位置
     * @return 熔岩是否能保护
     */
    private static boolean lavaWillProtect(BlockPos pos) {
        assert MinecraftClient.getInstance().world != null;
        BlockState state = MinecraftClient.getInstance().world.getBlockState(pos);
        if (state.getBlock() == Blocks.LAVA) {
            int level = state.getFluidState().getLevel();
            return level == 0 || level >= _config.lavaLevelOrGreaterWillCancelFallDamage;
        }
        return false;
    }

    /**
     * 检查指定位置是否为水
     * @param pos 位置
     * @return 是否为水
     */
    private static boolean isWater(BlockPos pos) {
        assert MinecraftClient.getInstance().world != null;
        return MinecraftClient.getInstance().world.getBlockState(pos).getBlock() == Blocks.WATER;
    }

    /**
     * 在掉落时我们能到达这个方块吗，还是重力会把我们拉得太远？
     */
    private static boolean canTravelToInAir(BlockPos pos) {
        Entity player = MinecraftClient.getInstance().player;
        assert player != null;
        double verticalDist = player.getPos().getY() - pos.getY() - 1;
        double verticalVelocity = -1 * player.getVelocity().y;
        double grav = EntityHelper.ENTITY_GRAVITY;
        double movementSpeedPerTick = _config.averageHorizontalMovementSpeedPerTick; // 已计算，但也有些保守
        // 1维抛物运动
        double ticksToTravelSq = (-verticalVelocity + Math.sqrt(verticalVelocity * verticalVelocity + 2 * grav * verticalDist)) / grav;
        double maxMoveDistanceSq = movementSpeedPerTick * movementSpeedPerTick * ticksToTravelSq * ticksToTravelSq;
        // 我们需要进入1个方块内，所以减去一个"半径"或类似的东西
        double horizontalDistance = WorldHelper.distanceXZ(player.getPos(), WorldHelper.toVec3d(pos)) - 0.8;
        if (horizontalDistance < 0)
            horizontalDistance = 0;
        return maxMoveDistanceSq > horizontalDistance * horizontalDistance;
    }

    /**
     * 检查掉落是否致命
     * @param pos 着陆位置
     * @return 是否致命
     */
    private static boolean isFallDeadly(BlockPos pos) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        double damage = calculateFallDamageToLandOn(pos);
        assert MinecraftClient.getInstance().world != null;
        Block b = MinecraftClient.getInstance().world.getBlockState(pos).getBlock();
        if (b == Blocks.HAY_BLOCK) {
            damage *= 0.2f;
        }
        assert player != null;
        double resultingHealth = player.getHealth() - (float) damage;
        return resultingHealth < _config.preferLavaWhenFallDropsHealthBelowThreshold;
    }

    /**
     * 计算着陆时的掉落伤害
     * @param pos 着陆位置
     * @return 掉落伤害
     */
    private static double calculateFallDamageToLandOn(BlockPos pos) {
        ClientWorld world = MinecraftClient.getInstance().world;
        PlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        double totalFallDistance = player.fallDistance + (player.getY() - pos.getY() - 1);
        // 从生物实体复制，某处，你懂的。
        double baseFallDamage = MathHelper.ceil(totalFallDistance - 3.0F);
        // 稍微保守一点，假设更多伤害
        assert world != null;
        return EntityHelper.calculateResultingPlayerDamage(player, DamageSourceVer.getFallDamageSource(world), baseFallDamage);
    }

    /**
     * 左右移动
     * @param delta 移动方向
     */
    private static void moveLeftRight(int delta) {
        InputControls controls = AltoClef.getInstance().getInputControls();

        if (delta == 0) {
            controls.release(Input.MOVE_LEFT);
            controls.release(Input.MOVE_RIGHT);
        } else if (delta > 0) {
            controls.release(Input.MOVE_LEFT);
            controls.hold(Input.MOVE_RIGHT);
        } else {
            controls.hold(Input.MOVE_LEFT);
            controls.release(Input.MOVE_RIGHT);
        }
    }

    /**
     * 前后移动
     * @param delta 移动方向
     */
    private static void moveForwardBack(int delta) {
        InputControls controls = AltoClef.getInstance().getInputControls();

        if (delta == 0) {
            controls.release(Input.MOVE_FORWARD);
            controls.release(Input.MOVE_BACK);
        } else if (delta > 0) {
            controls.hold(Input.MOVE_FORWARD);
            controls.release(Input.MOVE_BACK);
        } else {
            controls.release(Input.MOVE_FORWARD);
            controls.hold(Input.MOVE_BACK);
        }
    }

    /**
     * 内部Tick处理
     * @param mod AltoClef实例
     * @param oldMovingTorwards 旧的移动方向
     * @return 任务
     */
    private Task onTickInternal(AltoClef mod, BlockPos oldMovingTorwards) {
        Optional<BlockPos> willLandOn = getBlockWeWillLandOn(mod);
        Optional<BlockPos> bestClutchPos = getBestConeClutchBlock(mod, oldMovingTorwards);
        // 移动到我们最佳的"抓地"位置
        if (bestClutchPos.isPresent()) {
            movingTorwards = bestClutchPos.get().mutableCopy();
            if (!movingTorwards.equals(oldMovingTorwards)) {
                if (oldMovingTorwards == null)
                    Debug.logMessage("(新抓地目标: " + movingTorwards + ")");
                else
                    Debug.logMessage("(改变抓地目标: " + movingTorwards + ")");
            }
        } else if (oldMovingTorwards != null) {
            Debug.logMessage("(失去抓地位置！)");
        }
        if (willLandOn.isPresent()) {
            handleJumpForLand(mod, willLandOn.get());
            return placeMLGBucketTask(mod, willLandOn.get());
        } else {
            setDebugState("等待...");
            // 我们必须在进入"可攀爬"对象时立即触发跳跃
            mod.getInputControls().release(Input.JUMP);
            return null;
        }
    }

    /**
     * 放置MLG水桶任务
     * @param mod AltoClef实例
     * @param toPlaceOn 放置位置
     * @return 任务
     */
    private Task placeMLGBucketTask(AltoClef mod, BlockPos toPlaceOn) {
        if (!hasClutchItem(mod)) {
            setDebugState("没有抓地物品");
            return null;
        }
        // 如果射线检测击中非实体方块，则向下移动一格
        if (!WorldHelper.isSolidBlock(toPlaceOn)) {
            toPlaceOn = toPlaceOn.down();
        }
        BlockPos willLandIn = toPlaceOn.up();
        // 如果是水，我们没事。什么都不做。
        BlockState willLandInState = mod.getWorld().getBlockState(willLandIn);
        if (willLandInState.getBlock() == Blocks.WATER) {
            // 我们没事。
            setDebugState("等待掉入水中");
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
            return null;
        }

        IPlayerContext ctx = mod.getClientBaritone().getPlayerContext();
        Optional<Rotation> reachable = RotationUtils.reachableCenter(ctx.player(), toPlaceOn, ctx.playerController().getBlockReachDistance(), false);
        if (reachable.isPresent()) {
            setDebugState("执行MLG");
            LookHelper.lookAt(reachable.get());
            // 默认尝试水
            boolean hasClutch = (!mod.getWorld().getDimension().ultrawarm() && mod.getSlotHandler().forceEquipItem(Items.WATER_BUCKET));
            if (!hasClutch) {
                // 检查我们的"抓地"物品，看是否有合适的
                if (!_config.clutchItems.isEmpty()) {
                    for (Item tryEquip : _config.clutchItems) {
                        if (mod.getSlotHandler().forceEquipItem(tryEquip)) {
                            hasClutch = true;
                            break;
                        }
                    }
                }
            }
            // 也尝试捕捉高草...
            BlockPos[] toCheckLook = new BlockPos[]{toPlaceOn, toPlaceOn.up(), toPlaceOn.up(2)};
            if (hasClutch && Arrays.stream(toCheckLook).anyMatch(check -> mod.getClientBaritone().getPlayerContext().isLookingAt(check))) {
                Debug.logMessage("命中: " + willLandIn);
                placedPos = willLandIn;
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
            } else {
                setDebugState("没有正确看向！");
            }
        } else {
            setDebugState("等待到达目标方块...");
        }
        return null;
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 始终冲刺
        mod.getInputControls().hold(Input.SPRINT);
        // 检查玩家周围而不是正下方。
        // 我们可能裁剪方块或墙的边缘。
        BlockPos oldMovingTorwards = movingTorwards != null ? movingTorwards.mutableCopy() : null;
        movingTorwards = null;
        Task result = onTickInternal(mod, oldMovingTorwards);

        handleForwardVelocity(mod, !Objects.equals(oldMovingTorwards, movingTorwards));
        handleCancellingSidewaysVelocity(mod);

        return result;
    }

    /**
     * 处理前进速度
     * @param mod AltoClef实例
     * @param newForwardTarget 是否为新的前进目标
     */
    private void handleForwardVelocity(AltoClef mod, boolean newForwardTarget) {
        if (mod.getPlayer().isOnGround() || movingTorwards == null || WorldHelper.inRangeXZ(mod.getPlayer(), movingTorwards, 0.05f)) {
            moveForwardBack(0);
            return;
        }
        Rotation look = LookHelper.getLookRotation();
        look = new Rotation(look.getYaw(), 0);
        Vec3d forwardFacing = LookHelper.toVec3d(look).multiply(1, 0, 1).normalize();
        Vec3d delta = WorldHelper.toVec3d(movingTorwards).subtract(mod.getPlayer().getPos()).multiply(1, 0, 1);
        Vec3d velocity = mod.getPlayer().getVelocity().multiply(1, 0, 1);
        Vec3d pd = delta.subtract(velocity.multiply(3f));
        double forwardStrength = pd.dotProduct(forwardFacing);
        if (newForwardTarget) {
            LookHelper.lookAt(mod, movingTorwards);
        }
        Debug.logInternal("F:" + forwardStrength);
        moveForwardBack((int) Math.signum(forwardStrength));
    }

    @Override
    protected void onStart() {
        AltoClef.getInstance().getClientBaritone().getPathingBehavior().forceCancel();
        placedPos = null;
        // 掉落时按住shift。
        // 先向下看，可能有帮助
        AltoClef.getInstance().getPlayer().setPitch(90);
    }

    /**
     * 我们将降落在这个方块上，处理我们的跳跃。
     * <p>
     * 扭曲藤蔓要求我们只在藤蔓内部时按空格键
     */
    private void handleJumpForLand(AltoClef mod, BlockPos willLandOn) {
        BlockPos willLandIn = WorldHelper.isSolidBlock(willLandOn) ? willLandOn.up() : willLandOn;
        BlockState s = mod.getWorld().getBlockState(willLandIn);
        if (s.getBlock() == Blocks.LAVA) {
            // 始终为熔岩按住跳跃
            mod.getInputControls().hold(Input.JUMP);
            return;
        }
        Box blockBounds;
        try {
            blockBounds = s.getCollisionShape(mod.getWorld(), willLandIn).getBoundingBox();
        } catch (UnsupportedOperationException ex) {
            blockBounds = Box.of(WorldHelper.toVec3d(willLandIn), 1, 1, 1);
        }
        boolean inside = mod.getPlayer().getBoundingBox().intersects(blockBounds);
        if (inside)
            mod.getInputControls().hold(Input.JUMP);
        else
            mod.getInputControls().release(Input.JUMP);
    }

    /**
     * 获取我们将降落在哪个方块上
     * @param mod AltoClef实例
     * @return 将降落的方块位置（如果存在）
     */
    private Optional<BlockPos> getBlockWeWillLandOn(AltoClef mod) {
        Vec3d velCheck = mod.getPlayer().getVelocity();
        // 展平并略微夸大速度
        velCheck.multiply(10, 0, 10);
        Box b = mod.getPlayer().getBoundingBox().offset(velCheck);
        Vec3d c = b.getCenter();
        Vec3d[] coords = new Vec3d[]{
                c,
                new Vec3d(b.minX, c.y, b.minZ),
                new Vec3d(b.maxX, c.y, b.minZ),
                new Vec3d(b.minX, c.y, b.maxZ),
                new Vec3d(b.maxX, c.y, b.maxZ),
        };
        BlockHitResult result = null;
        double bestSqDist = Double.POSITIVE_INFINITY;
        for (Vec3d rayOrigin : coords) {
            RaycastContext rctx = castDown(rayOrigin);
            BlockHitResult hit = mod.getWorld().raycast(rctx);
            if (hit.getType() == HitResult.Type.BLOCK) {
                double curDis = hit.getPos().squaredDistanceTo(rayOrigin);
                if (curDis < bestSqDist) {
                    result = hit;
                    bestSqDist = curDis;
                }
            }
        }

        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        return Optional.ofNullable(result.getBlockPos());
    }

    /**
     * 降落到目标时，我们朝中心看并向前移动。
     * 但是，如果我们改变方向，我们会相对于视角方向侧向移动，
     * 这通常会搞砸我们。
     * <p>
     * 这将使机器人向左/右移动，这样我们就不再"滑向"侧面。
     */
    private void handleCancellingSidewaysVelocity(AltoClef mod) {
        if (movingTorwards == null) {
            moveLeftRight(0);
            return;
        }
        // 取消相对于方块的左/右速度
        Vec3d velocity = mod.getPlayer().getVelocity();
        Vec3d deltaTarget = WorldHelper.toVec3d(movingTorwards).subtract(mod.getPlayer().getPos());
        // 相对于delta的"右"速度
        Rotation look = LookHelper.getLookRotation();
        Vec3d forwardFacing = LookHelper.toVec3d(look).multiply(1, 0, 1).normalize();
        Vec3d rightVelocity = MathsHelper.projectOntoPlane(velocity, forwardFacing).multiply(1, 0, 1); // 展平
        // 还要考虑我们应向右移动多少距离
        Vec3d rightDelta = MathsHelper.projectOntoPlane(deltaTarget, forwardFacing).multiply(1, 0, 1);
        // 执行一个小的PD循环
        Vec3d pd = rightDelta.subtract(rightVelocity.multiply(2));
        // 我们横向移动得太快了
        Vec3d faceRight = forwardFacing.crossProduct(new Vec3d(0, 1, 0));
        boolean moveRight = pd.dotProduct(faceRight) > 0;
        if (moveRight) {
            moveLeftRight(1);
        } else {
            moveLeftRight(-1);
        }
    }

    /**
     * 获取最佳圆锥抓地方块
     * @param mod AltoClef实例
     * @param oldClutchTarget 旧抓地方块
     * @return 最佳抓地方块位置（如果存在）
     */
    private Optional<BlockPos> getBestConeClutchBlock(AltoClef mod, BlockPos oldClutchTarget) {
        double pitchHalfWidth = _config.epicClutchConePitchAngle;
        double dpitchStart = pitchHalfWidth / _config.epicClutchConePitchResolution;

        // 我们的优先级是：
        // - 安全降落（水）
        // - 最高方块
        // 如果我们有MLG
        // - 接近玩家

        ConeClutchContext cctx = new ConeClutchContext(mod);

        // 始终检查我们之前的最佳位置，以免失去它
        if (oldClutchTarget != null)
            cctx.checkBlock(mod, oldClutchTarget);

        // 执行圆锥扫描
        for (double pitch = dpitchStart; pitch <= pitchHalfWidth; pitch += pitchHalfWidth / _config.epicClutchConePitchResolution) {
            double pitchProgress = (pitch - dpitchStart) / (pitchHalfWidth - dpitchStart);
            double yawResolution = _config.epicClutchConeYawDivisionStart + pitchProgress * (_config.epicClutchConeYawDivisionEnd - _config.epicClutchConeYawDivisionStart); // 从开始到结束插值
            for (double yaw = 0; yaw < 360; yaw += 360.0 / yawResolution) {
                RaycastContext rctx = castCone(yaw, pitch);
                cctx.checkRay(mod, rctx);
            }
        }

        // 执行附近扫描
        //int nearbySweepSize =
        Vec3d center = mod.getPlayer().getPos();
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                RaycastContext ctx = castDown(center.add(dx, 0, dz));
                cctx.checkRay(mod, ctx);
            }
        }

        return Optional.ofNullable(cctx.bestBlock);
    }

    /**
     * 向下投射
     * @param origin 起始点
     * @return 射线投射上下文
     */
    private RaycastContext castDown(Vec3d origin) {
        Entity player = MinecraftClient.getInstance().player;
        assert player != null;
        return new RaycastContext(origin, origin.add(0, -1 * _config.castDownDistance, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, player);
    }

    /**
     * 圆锥投射
     * @param yaw 偏航角
     * @param pitch 俯仰角
     * @return 射线投射上下文
     */
    private RaycastContext castCone(double yaw, double pitch) {
        Entity player = MinecraftClient.getInstance().player;
        assert player != null;
        Vec3d origin = player.getPos();
        double dy = _config.epicClutchConeCastHeight;
        double dH = dy * Math.sin(Math.toRadians(pitch)); // 水平距离
        double yawRad = Math.toRadians(yaw);
        double dx = dH * Math.cos(yawRad);
        double dz = dH * Math.sin(yawRad);
        Vec3d end = origin.add(dx, -1 * dy, dz);
        return new RaycastContext(origin, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, player);
    }

    @Override
    protected void onStop(Task interruptTask) {
        Baritone baritone = AltoClef.getInstance().getClientBaritone();
        InputControls controls = AltoClef.getInstance().getInputControls();

        baritone.getPathingBehavior().forceCancel();
        movingTorwards = null;
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
        moveLeftRight(0);
        moveForwardBack(0);
        controls.release(Input.SPRINT);
        controls.release(Input.JUMP);
    }

    /**
     * 检查是否有抓地物品
     * @param mod AltoClef实例
     * @return 是否有抓地物品
     */
    private boolean hasClutchItem(AltoClef mod) {
        if (!mod.getWorld().getDimension().ultrawarm() && mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
            return true;
        }
        return _config.clutchItems.stream().anyMatch(item -> mod.getItemStorage().hasItem(item));
    }

    @Override
    public boolean isFinished() {
        ClientPlayerEntity player = AltoClef.getInstance().getPlayer();

        // 当玩家在游泳、接触水、在地面或攀爬时完成
        return player.isSwimming() || player.isTouchingWater() || player.isOnGround() || player.isClimbing();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof MLGBucketTask;
    }

    @Override
    protected String toDebugString() {
        String result = "史诗级游戏时刻";
        if (movingTorwards != null) {
            result += " (抓地在: " + movingTorwards + ")";
        }
        return result;
    }

    /**
     * 获取水放置位置
     * @return 水放置位置
     */
    public BlockPos getWaterPlacedPos() {
        return placedPos;
    }

    /**
     * MLG抓地配置类
     */
    private static class MLGClutchConfig {
        public double castDownDistance = 40; // 向下投射距离
        public double averageHorizontalMovementSpeedPerTick = 0.25; // 每tick玩家水平移动的距离。设置得太低，机器人会忽略可行的抓地。设置得太高，机器人会尝试抓地但无法到达。
        public double epicClutchConeCastHeight = 40; // "史诗级抓地"射线圆锥的高度
        public double epicClutchConePitchAngle = 25; // "史诗级抓地"射线圆锥的宽度（度）
        public int epicClutchConePitchResolution = 8; // 圆锥的俯仰角度的分割数量
        public int epicClutchConeYawDivisionStart = 6; // 圆锥抓地在中心开始的分割数量
        public int epicClutchConeYawDivisionEnd = 20; // 圆锥抓地在末尾的分割数量
        public int preferLavaWhenFallDropsHealthBelowThreshold = 3; // 如果掉落导致玩家血量低于此值，则认为是致命的。
        public int lavaLevelOrGreaterWillCancelFallDamage = 5; // 此等级的熔岩会在我们按空格键时取消掉落伤害。
        @JsonSerialize(using = ItemSerializer.class)
        @JsonDeserialize(using = ItemDeserializer.class)
        public List<Item> clutchItems = List.of(Items.HAY_BLOCK, Items.TWISTING_VINES); // 抓地物品列表
    }

    /**
     * 圆锥抓地上下文
     */
    class ConeClutchContext {
        private final boolean hasClutchItem;
        public BlockPos bestBlock = null; // 最佳方块
        private double highestY = Double.NEGATIVE_INFINITY; // 最高Y坐标
        private double closestXZ = Double.POSITIVE_INFINITY; // 最近XZ距离
        private boolean bestBlockIsSafe = false; // 最佳方块是否安全
        private boolean bestBlockIsDeadlyFall = false; // 最佳方块是否致命掉落
        private boolean bestBlockIsLava = false; // 最佳方块是否为熔岩

        public ConeClutchContext(AltoClef mod) {
            hasClutchItem = hasClutchItem(mod);
        }

        /**
         * 检查方块
         * @param mod AltoClef实例
         * @param check 待检查方块
         */
        public void checkBlock(AltoClef mod, BlockPos check) {
            // 已检查过
            if (Objects.equals(bestBlock, check))
                return;
            if (WorldHelper.isAir(check)) {
                Debug.logMessage("(MLG空气方块检查降落，方块破碎了。我们将尝试另一个): " + check);
                return;
            }
            boolean lava = isLava(check);
            boolean lavaWillProtect = lava && lavaWillProtect(check);
            boolean water = isWater(check);
            boolean isDeadlyFall = !hasClutchItem && isFallDeadly(check);
            // 始终优先考虑安全方块
            if (bestBlockIsSafe && !water)
                return;
            double height = check.getY();
            double distSqXZ = WorldHelper.distanceXZSquared(WorldHelper.toVec3d(check), mod.getPlayer().getPos());
            boolean highestSoFar = height > highestY;
            boolean closestSoFar = distSqXZ < closestXZ;
            // 我们找到了一个新的候选者
            if (
                    bestBlock == null || // 没有找到目标。
                            (water && !bestBlockIsSafe) || // 如果可能我们总是降落在水中
                            (lava && lavaWillProtect && bestBlockIsDeadlyFall && !hasClutchItem) || // 如果我们最好的选择是因掉落伤害而死亡，则降落在熔岩中
                            (!lava && !isDeadlyFall && ((closestSoFar && hasClutchItem) && highestSoFar || bestBlockIsLava)) // 如果不是熔岩且不致命，如果它比之前更高或我们最好的选择是熔岩，则降落在上面
            ) {
                if (canTravelToInAir((lava || water) ? check.down() : check)) {
                    if (highestSoFar) {
                        highestY = height;
                    }
                    if (closestSoFar) {
                        closestXZ = distSqXZ;
                    }
                    bestBlockIsSafe = water;
                    bestBlockIsDeadlyFall = isDeadlyFall;
                    bestBlockIsLava = lava;
                    bestBlock = check;
                }
            }
        }

        /**
         * 检查射线
         * @param mod AltoClef实例
         * @param rctx 射线上下文
         */
        public void checkRay(AltoClef mod, RaycastContext rctx) {
            BlockHitResult hit = mod.getWorld().raycast(rctx);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos check = hit.getBlockPos();
                // 目前，要求我们降落在这个方块上
                if (hit.getSide().getOffsetY() <= 0)
                    return;
                checkBlock(mod, check);
            }
        }
    }
}

    @Override
    protected String toDebugString() {
        String result = "Epic gaemer moment";
        if (movingTorwards != null) {
            result += " (CLUTCH AT: " + movingTorwards + ")";
        }
        return result;
    }

    public BlockPos getWaterPlacedPos() {
        return placedPos;
    }

    private static class MLGClutchConfig {
        public double castDownDistance = 40;
        public double averageHorizontalMovementSpeedPerTick = 0.25; // How "far" the player moves horizontally per tick. Set too low and the bot will ignore viable clutches. Set too high and the bot will go for clutches it can't reach.
        public double epicClutchConeCastHeight = 40; // How high the "epic clutch" ray cone is
        public double epicClutchConePitchAngle = 25; // How wide (degrees) the "epic clutch" ray cone is
        public int epicClutchConePitchResolution = 8; // How many divisions in each direction the cone's pitch has
        public int epicClutchConeYawDivisionStart = 6; // How many divisions to start the cone clutch at in the center
        public int epicClutchConeYawDivisionEnd = 20; // How many divisions to move the cone clutch at torwars the end
        public int preferLavaWhenFallDropsHealthBelowThreshold = 3; // If a fall results in our player's health going below this value, consider it deadly.
        public int lavaLevelOrGreaterWillCancelFallDamage = 5; // Lava at this level will cancel our fall damage if we hold space.
        @JsonSerialize(using = ItemSerializer.class)
        @JsonDeserialize(using = ItemDeserializer.class)
        public List<Item> clutchItems = List.of(Items.HAY_BLOCK, Items.TWISTING_VINES);
    }

    class ConeClutchContext {
        private final boolean hasClutchItem;
        public BlockPos bestBlock = null;
        private double highestY = Double.NEGATIVE_INFINITY;
        private double closestXZ = Double.POSITIVE_INFINITY;
        private boolean bestBlockIsSafe = false;
        private boolean bestBlockIsDeadlyFall = false;
        private boolean bestBlockIsLava = false;

        public ConeClutchContext(AltoClef mod) {
            hasClutchItem = hasClutchItem(mod);
        }

        public void checkBlock(AltoClef mod, BlockPos check) {
            // Already checked
            if (Objects.equals(bestBlock, check))
                return;
            if (WorldHelper.isAir(check)) {
                Debug.logMessage("(MLG Air block checked for landing, the block broke. We'll try another): " + check);
                return;
            }
            boolean lava = isLava(check);
            boolean lavaWillProtect = lava && lavaWillProtect(check);
            boolean water = isWater(check);
            boolean isDeadlyFall = !hasClutchItem && isFallDeadly(check);
            // Prioritize safe blocks ALWAYS
            if (bestBlockIsSafe && !water)
                return;
            double height = check.getY();
            double distSqXZ = WorldHelper.distanceXZSquared(WorldHelper.toVec3d(check), mod.getPlayer().getPos());
            boolean highestSoFar = height > highestY;
            boolean closestSoFar = distSqXZ < closestXZ;
            // We found a new contender
            if (
                    bestBlock == null || // No target was found.
                            (water && !bestBlockIsSafe) || // We ALWAYS land in water if we can
                            (lava && lavaWillProtect && bestBlockIsDeadlyFall && !hasClutchItem) || // Land in lava if our best alternative is death by fall damage
                            (!lava && !isDeadlyFall && ((closestSoFar && hasClutchItem) && highestSoFar || bestBlockIsLava)) // If it's not lava and is not deadly, land on it if it's higher than before OR if our best alternative is lava
            ) {
                if (canTravelToInAir((lava || water) ? check.down() : check)) {
                    if (highestSoFar) {
                        highestY = height;
                    }
                    if (closestSoFar) {
                        closestXZ = distSqXZ;
                    }
                    bestBlockIsSafe = water;
                    bestBlockIsDeadlyFall = isDeadlyFall;
                    bestBlockIsLava = lava;
                    bestBlock = check;
                }
            }
        }

        public void checkRay(AltoClef mod, RaycastContext rctx) {
            BlockHitResult hit = mod.getWorld().raycast(rctx);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos check = hit.getBlockPos();
                // For now, REQUIRE we land on this
                if (hit.getSide().getOffsetY() <= 0)
                    return;
                checkBlock(mod, check);
            }
        }
    }

}
