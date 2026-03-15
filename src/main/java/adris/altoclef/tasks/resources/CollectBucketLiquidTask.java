package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

import java.util.HashSet;
import java.util.function.Predicate;

/**
 * 收集液体桶任务
 * 用于收集水或岩浆等液体，装入桶中
 */
public class CollectBucketLiquidTask extends ResourceTask {

    private final HashSet<BlockPos> blacklist = new HashSet<>(); // 黑名单位置
    private final TimerGame tryImmediatePickupTimer = new TimerGame(3); // 尝试立即收集的计时器
    private final TimerGame pickedUpTimer = new TimerGame(0.5); // 收集后的计时器
    private final int count; // 目标数量

    private final Item target; // 目标物品
    private final Block toCollect; // 要收集的液体方块
    private final String liquidName; // 液体名称
    private final MovementProgressChecker progressChecker = new MovementProgressChecker(); // 移动进度检查器

    private boolean wasWandering = false; // 是否在徘徊

    public CollectBucketLiquidTask(String liquidName, Item filledBucket, int targetCount, Block toCollect) {
        super(filledBucket, targetCount);
        this.liquidName = liquidName;
        target = filledBucket;
        count = targetCount;
        this.toCollect = toCollect;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onResourceStart(AltoClef mod) {
        // 跟踪流体
        mod.getBehaviour().push();
        mod.getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);

        // 避免在液体上破坏或放置方块
        mod.getBehaviour().avoidBlockBreaking((pos) -> MinecraftClient.getInstance().world.getBlockState(pos).getBlock() == toCollect);
        mod.getBehaviour().avoidBlockPlacing((pos) -> MinecraftClient.getInstance().world.getBlockState(pos).getBlock() == toCollect);

        mod.getClientBaritoneSettings().avoidUpdatingFallingBlocks.value = true;
        //_blacklist.clear();

        progressChecker.reset();
    }


    @Override
    protected Task onTick() {
        Task result = super.onTick();
        // 重置"首次"超时/徘徊标志
        if (!thisOrChildAreTimedOut()) {
            wasWandering = false;
        }
        return result;
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            progressChecker.reset();
        }
        // 如果我们站在液体内部，尝试收集它
        if (tryImmediatePickupTimer.elapsed() && !mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
            Block standingInside = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos()).getBlock();
            if (standingInside == toCollect && WorldHelper.isSourceBlock(mod.getPlayer().getBlockPos(), false)) {
                setDebugState("尝试收集（我们在液体中）");
                mod.getInputControls().forceLook(0, 90);
                //mod.getClientBaritone().getLookBehavior().updateTarget(new Rotation(0, 90), true);
                //Debug.logMessage("Looking at " + _toCollect + ", picking up right away.");
                tryImmediatePickupTimer.reset();
                if (mod.getSlotHandler().forceEquipItem(Items.BUCKET)) {
                    mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                    mod.getExtraBaritoneSettings().setInteractionPaused(true);
                    pickedUpTimer.reset();
                    progressChecker.reset();
                }
                return null;
            }
        }

        if (!pickedUpTimer.elapsed()) {
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            progressChecker.reset();
            // 等待强制收集
            return null;
        }

        // 如果需要则获取桶
        int bucketsNeeded = count - mod.getItemStorage().getItemCount(Items.BUCKET) - mod.getItemStorage().getItemCount(target);
        if (bucketsNeeded > 0) {
            setDebugState("获取桶...");
            return TaskCatalogue.getItemTask(Items.BUCKET, bucketsNeeded);
        }

        Predicate<BlockPos> isSafeSourceLiquid = blockPos -> {
            if (blacklist.contains(blockPos)) return false;
            if (!WorldHelper.canReach(blockPos)) return false;
            if (!WorldHelper.canReach(blockPos.up())) return false; // 我们可能尝试到达上方的方块
            assert MinecraftClient.getInstance().world != null;

            Block above = mod.getWorld().getBlockState(blockPos.up()).getBlock();
            // 我们破坏上方的方块。如果是基岩，忽略
            if (above == Blocks.BEDROCK || above == Blocks.WATER) {
                return false;
            }

            // 检查周围方块是否不是水，这样就不会到处溢出
            for (Direction direction : Direction.values()) {
                if (direction.getAxis().isVertical()) continue;

                if (mod.getWorld().getBlockState(blockPos.up().offset(direction)).getBlock() == Blocks.WATER) {
                    return false;
                }
            }

            return WorldHelper.isSourceBlock(blockPos, false);
        };

        // 查找最近的水并右键点击它
        if (mod.getBlockScanner().anyFound(isSafeSourceLiquid, toCollect)) {
            // 我们想要最小化到液体的距离
            setDebugState("尝试收集...");
            //Debug.logMessage("TEST: " + RayTraceUtils.fluidHandling);

            return new DoToClosestBlockTask(blockPos -> {
                // 清除上方，如果是岩浆因为无法进入
                // 但如果我们就站在上方，则不是
                if (mod.getWorld().getBlockState(blockPos.up()).isSolid()) {
                    if (!progressChecker.check(mod)) {
                        mod.getClientBaritone().getPathingBehavior().cancelEverything();
                        mod.getClientBaritone().getPathingBehavior().forceCancel();
                        mod.getClientBaritone().getExploreProcess().onLostControl();
                        mod.getClientBaritone().getCustomGoalProcess().onLostControl();
                        Debug.logMessage("破坏失败，列入黑名单。");
                        mod.getBlockScanner().requestBlockUnreachable(blockPos);
                        blacklist.add(blockPos);
                    }
                    return new DestroyBlockTask(blockPos.up());
                }

                if (tries > 75) {
                    if (timeoutTimer.elapsed()) {
                        tries = 0;
                    }
                    mod.log("尝试徘徊 "+timeoutTimer.getDuration());
                    return new TimeoutWanderTask();
                }
                timeoutTimer.reset();

                // 我们可以到达方块
                if (LookHelper.getReach(blockPos).isPresent() &&
                        mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                    tries++;
                    return new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), blockPos, toCollect != Blocks.LAVA, new Vec3i(0, 1, 0));
                }
                // 靠近足够近
                // 向上，因为在下方时我们会尝试移动到液体旁边（对于岩浆，不是好主意）
                if (this.thisOrChildAreTimedOut() && !wasWandering) {
                    mod.getBlockScanner().requestBlockUnreachable(blockPos.up());
                    wasWandering = true;
                }
                return new GetCloseToBlockTask(blockPos.up());
            }, isSafeSourceLiquid, toCollect);
        }

        // 维度
        if (toCollect == Blocks.WATER && WorldHelper.getCurrentDimension() == Dimension.NETHER) {
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }

        // 没找到液体
        setDebugState("通过漫无目的地徘徊搜索液体");

        return new TimeoutWanderTask();
    }
    int tries = 0;
    TimerGame timeoutTimer = new TimerGame(2);

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
        //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
        mod.getExtraBaritoneSettings().setInteractionPaused(false);

        mod.getClientBaritoneSettings().avoidUpdatingFallingBlocks.value = false;
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CollectBucketLiquidTask task) {
            if (task.count != count) return false;
            return task.toCollect == toCollect;
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + count + " 个" + liquidName + "桶";
    }

    /**
     * 收集水桶任务
     */
    public static class CollectWaterBucketTask extends CollectBucketLiquidTask {
        public CollectWaterBucketTask(int targetCount) {
            super("water", Items.WATER_BUCKET, targetCount, Blocks.WATER);
        }
    }

    /**
     * 收集岩浆桶任务
     */
    public static class CollectLavaBucketTask extends CollectBucketLiquidTask {
        public CollectLavaBucketTask(int targetCount) {
            super("lava", Items.LAVA_BUCKET, targetCount, Blocks.LAVA);
        }
    }

}
