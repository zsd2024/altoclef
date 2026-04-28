package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.FoodComponentWrapper;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import baritone.pathing.movement.MovementHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 逃离熔岩任务
 * 此任务使玩家自动逃离熔岩区域，包括跳跃、疾跑和放置方块来逃生
 */
public class EscapeFromLavaTask extends CustomBaritoneGoalTask {

    // 逃离强度（影响路径规划的权重）
    private final float strength;
    // 计时器
    private int ticks = 0;
    // 避免在危险位置放置方块的谓词
    private final Predicate<BlockPos> avoidPlacingRiskyBlock;

    /**
     * 构造函数
     * @param mod AltoClef实例
     * @param strength 逃离强度
     */
    public EscapeFromLavaTask(AltoClef mod,float strength) {
        this.strength = strength;
        avoidPlacingRiskyBlock = (blockPos -> mod.getPlayer().getBoundingBox().intersects(new Box(blockPos))
                && (mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().down()).getBlock() == Blocks.LAVA || mod.getPlayer().isInLava()));
    }

    /**
     * 默认构造函数，使用默认强度100
     * @param mod AltoClef实例
     */
    public EscapeFromLavaTask(AltoClef mod) {
        this(mod, 100);
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        // 保存当前行为设置
        mod.getBehaviour().push();
        // 失去对探索和自定义目标进程的控制
        mod.getClientBaritone().getExploreProcess().onLostControl();
        mod.getClientBaritone().getCustomGoalProcess().onLostControl();
        // 允许在熔岩中游泳（实际上是快速通过）
        mod.getBehaviour().allowSwimThroughLava(true);
        // 鼓励放置所有方块！
        mod.getBehaviour().setBlockPlacePenalty(0);
        mod.getBehaviour().setBlockBreakAdditionalPenalty(0); // 通常为2
        // 永远不要随机游走
        checker = new MovementProgressChecker((int) Float.POSITIVE_INFINITY);

        // 避免在熔岩下方放置方块
        mod.getExtraBaritoneSettings().avoidBlockPlace(avoidPlacingRiskyBlock);
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

       // if (mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().up()).getBlock().equals(Blocks.LAVA)) {
            // 在熔岩中跳跃和疾跑，这样更快
            mod.getInputControls().hold(Input.JUMP);
            mod.getInputControls().hold(Input.SPRINT);
            /* setDebugState("escaping submerged lava");
            return null;
        }*/

        // 计算最佳食物并尝试食用
        Optional<Item> food = calculateFood(mod);
        if (food.isPresent() && mod.getPlayer().getHungerManager().getFoodLevel() < 20) {
            // 可能被放置方块中断，但我们应该尽可能尝试进食
            if (mod.getPlayer().isBlocking()) {
                mod.log("想要进食，尝试停止盾牌格挡...");
                mod.getInputControls().release(Input.CLICK_RIGHT);
            } else {
                mod.getSlotHandler().forceEquipItem(new Item[]{food.get()}, true);
                mod.getInputControls().hold(Input.CLICK_RIGHT);
            }
        }

        // 在熔岩中疾跑+跳跃，这样更快
        if (mod.getPlayer().isInLava() || mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().down()).getBlock() == Blocks.LAVA) {

            setDebugState("逃离熔岩");

            BlockPos steppingPos = mod.getPlayer().getSteppingPos();
            // 如果周围有任何方向不是熔岩，使用常规路径规划
            if (!mod.getWorld().getBlockState(steppingPos.east()).getBlock().equals(Blocks.LAVA) ||
                    !mod.getWorld().getBlockState(steppingPos.west()).getBlock().equals(Blocks.LAVA) ||
                    !mod.getWorld().getBlockState(steppingPos.south()).getBlock().equals(Blocks.LAVA) ||
                    !mod.getWorld().getBlockState(steppingPos.north()).getBlock().equals(Blocks.LAVA) ||
                    !mod.getWorld().getBlockState(steppingPos.east().north()).getBlock().equals(Blocks.LAVA) ||
                    !mod.getWorld().getBlockState(steppingPos.east().south()).getBlock().equals(Blocks.LAVA) ||
                    !mod.getWorld().getBlockState(steppingPos.west().north()).getBlock().equals(Blocks.LAVA) ||
                    !mod.getWorld().getBlockState(steppingPos.west().south()).getBlock().equals(Blocks.LAVA)) {

                return super.onTick();
            }

            // 尝试放置方块来逃生
            if (mod.getPlayer().isBlocking()) {
                mod.log("想要放置方块，尝试停止盾牌格挡...");
                mod.getInputControls().release(Input.CLICK_RIGHT);
            }

            // 射线检测寻找合适的位置放置方块
            for (float pitch = 25; pitch < 90; pitch += 1f) {
                for (float yaw = -180; yaw < 180; yaw += 1f) {
                    HitResult result = raycast(mod, 4, pitch, yaw);
                    if (result.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHitResult = (BlockHitResult) result;
                        BlockPos pos = blockHitResult.getBlockPos();

                        // 只考虑低于或等于玩家脚下的位置
                        if (pos.getY() > mod.getPlayer().getSteppingPos().getY()) continue;

                        Direction facing = blockHitResult.getSide();

                        // 不能向上放置
                        if (facing == Direction.UP) continue;
                        LookHelper.lookAt(new Rotation(yaw,pitch));

                        // 优先使用地狱岩，否则使用可丢弃物品
                        if (mod.getItemStorage().hasItem(Items.NETHERRACK)) {
                            mod.getSlotHandler().forceEquipItem(Items.NETHERRACK);
                        } else {
                            mod.getSlotHandler().forceEquipItem(mod.getClientBaritoneSettings().acceptableThrowawayItems.value.toArray(new Item[0]));
                        }
                        mod.log(pos+"");
                        mod.log(facing+"");

                        // 尝试放置方块
                        mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                        return null;
                    }
                }
            }

        }

        return super.onTick();
    }

    // 类似于FoodChain
    // TODO 添加配置选项
    /**
     * 计算最佳食物
     * @param mod AltoClef实例
     * @return 最佳食物项（如果有）
     */
    private Optional<Item> calculateFood(AltoClef mod) {
        Item bestFood = null;
        double bestFoodScore = Double.NEGATIVE_INFINITY;
        ClientPlayerEntity player = mod.getPlayer();

        float hunger = player != null ? player.getHungerManager().getFoodLevel() : 20;
        float saturation = player != null ? player.getHungerManager().getSaturationLevel() : 20;
        // 获取最佳食物项并计算食物总分
        for (ItemStack stack : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
            if (ItemVer.isFood(stack)) {
                // 不管那么多了，我们在熔岩里，吃任何能吃的
                // 忽略受保护的物品
               // if (!ItemHelper.canThrowAwayStack(mod, stack)) continue;

                // 忽略蜘蛛眼
                if (stack.getItem() == Items.SPIDER_EYE) {
                    continue;
                }

                float score = getScore(stack, hunger, saturation);
                if (score > bestFoodScore) {
                    bestFoodScore = score;
                    bestFood = stack.getItem();
                }
            }
        }

        return Optional.ofNullable(bestFood);
    }

    /**
     * 计算食物评分
     * @param stack 食物物品堆栈
     * @param hunger 当前饥饿值
     * @param saturation 当前饱和度
     * @return 食物评分
     */
    private static float getScore(ItemStack stack, float hunger, float saturation) {
        FoodComponentWrapper food = ItemVer.getFoodComponent(stack.getItem());

        assert food != null;
        float hungerIfEaten = Math.min(hunger + food.getHunger(), 20);
        float saturationIfEaten = Math.min(hungerIfEaten, saturation + food.getSaturationModifier());
        float gainedSaturation = (saturationIfEaten - saturation);

        float hungerNotFilled = 20 - hungerIfEaten;
        float saturationGoodScore = gainedSaturation * 10;
        float hungerNotFilledPenalty = hungerNotFilled * 2;

        float score = saturationGoodScore - hungerNotFilledPenalty;

        if (stack.getItem() == Items.ROTTEN_FLESH) {
            score = 0;
        }
        return score;
    }

    /**
     * 射线检测
     * @param mod AltoClef实例
     * @param maxDistance 最大检测距离
     * @param pitch 俯仰角
     * @param yaw 偏航角
     * @return 射线检测结果
     */
    public HitResult raycast(AltoClef mod,double maxDistance, float pitch, float yaw) {
        Vec3d cameraPos = mod.getPlayer().getCameraPosVec(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true));
        Vec3d rotationVector = getRotationVector(pitch,yaw);

        Vec3d vec3d3 = cameraPos.add(rotationVector.x * maxDistance, rotationVector.y * maxDistance, rotationVector.z * maxDistance);
        return mod.getPlayer().getWorld()
                .raycast(
                        new RaycastContext(
                                cameraPos, vec3d3, RaycastContext.ShapeType.OUTLINE,
                                false ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, mod.getPlayer()
                        )
                );
    }
    
    /**
     * 根据俯仰角和偏航角获取旋转向量
     * @param pitch 俯仰角
     * @param yaw 偏航角
     * @return 旋转向量
     */
    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef mod = AltoClef.getInstance();

        // 恢复之前的行为设置
        mod.getBehaviour().pop();
        mod.getInputControls().release(Input.JUMP);
        mod.getInputControls().release(Input.SPRINT);
        mod.getInputControls().release(Input.CLICK_RIGHT);

        // 移除自定义的放置避免器
        synchronized (mod.getExtraBaritoneSettings().getPlaceMutex()) {
            mod.getExtraBaritoneSettings().getPlaceAvoiders().remove(avoidPlacingRiskyBlock);
        }
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        return new EscapeFromLavaGoal();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof EscapeFromLavaTask;
    }

    @Override
    public boolean isFinished() {
        ClientPlayerEntity player = AltoClef.getInstance().getPlayer();

        return !player.isInLava() && !player.isOnFire();
    }

    @Override
    protected String toDebugString() {
        return "逃离熔岩";
    }

    /**
     * 逃离熔岩的目标实现
     */
    private class EscapeFromLavaGoal implements Goal {

        /**
         * 检查指定位置是否为熔岩
         * @param x X坐标
         * @param y Y坐标
         * @param z Z坐标
         * @return 如果是熔岩则返回true
         */
        private static boolean isLava(int x, int y, int z) {
            if (MinecraftClient.getInstance().world == null) return false;
            return MovementHelper.isLava(MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z)));
        }

        /**
         * 检查指定位置周围是否邻接熔岩
         * @param x X坐标
         * @param y Y坐标
         * @param z Z坐标
         * @return 如果邻接熔岩则返回true
         */
        private static boolean isLavaAdjacent(int x, int y, int z) {
            return isLava(x + 1, y, z) || isLava(x - 1, y, z) || isLava(x, y, z + 1) || isLava(x, y, z - 1)
                    || isLava(x + 1, y, z - 1) || isLava(x + 1, y, z + 1) || isLava(x - 1, y, z - 1)
                    || isLava(x - 1, y, z + 1);
        }

        /**
         * 检查指定位置是否为水
         * @param x X坐标
         * @param y Y坐标
         * @param z Z坐标
         * @return 如果是水则返回true
         */
        private static boolean isWater(int x, int y, int z) {
            if (MinecraftClient.getInstance().world == null) return false;
            return MovementHelper.isWater(MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z)));
        }

        @Override
        public boolean isInGoal(int x, int y, int z) {
            // 目标位置不能是熔岩且不能邻接熔岩
            return !isLava(x, y, z) && !isLavaAdjacent(x, y, z);
        }

        @Override
        public double heuristic(int x, int y, int z) {
            // 熔岩位置的启发式值很高（不希望进入）
            if (isLava(x, y, z)) {
                return strength;
            } else if (isLavaAdjacent(x, y, z)) {
                return strength * 0.5f;
            }
            // 水位置的启发式值很低（优先选择）
            if (isWater(x, y, z)) {
                return -100;
            }
            return 0;
        }
    }
}
