package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.input.Input;
import baritone.pathing.movement.MovementHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * 在附近任意位置放置方块的任务
 * <p>
 * 也被称为"熊策略"任务。
 */
public class PlaceBlockNearbyTask extends Task {

    // 要放置的方块数组
    private final Block[] toPlace;

    // 移动进度检查器
    private final MovementProgressChecker progressChecker = new MovementProgressChecker();
    // 超时徘徊任务
    private final TimeoutWanderTask wander = new TimeoutWanderTask(5);

    // 随机视角计时器
    private final TimerGame _randomlookTimer = new TimerGame(0.25);
    // 判断位置是否可以放置方块的谓词
    private final Predicate<BlockPos> _canPlaceHere;
    // 刚刚放置方块的位置
    private BlockPos justPlaced;
    // 尝试放置方块的位置
    private BlockPos tryPlace;
    // 用于处理方块放置事件的订阅
    private Subscription<BlockPlaceEvent> _onBlockPlaced;

    /**
     * 构造函数
     * @param canPlaceHere 用于判断位置是否可以放置方块的谓词
     * @param toPlace 要放置的方块
     */
    public PlaceBlockNearbyTask(Predicate<BlockPos> canPlaceHere, Block... toPlace) {
        this.toPlace = toPlace;
        _canPlaceHere = canPlaceHere;
    }

    /**
     * 构造函数（默认任何地方都可以放置）
     * @param toPlace 要放置的方块
     */
    public PlaceBlockNearbyTask(Block... toPlace) {
        this(blockPos -> true, toPlace);
    }

    @Override
    protected void onStart() {
        progressChecker.reset();
        AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);

        // 订阅方块放置事件，以检测何时放置了方块
        _onBlockPlaced = EventBus.subscribe(BlockPlaceEvent.class, evt -> {
            if (ArrayUtils.contains(toPlace, evt.blockState.getBlock())) {
                stopPlacing();
            }
        });
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            progressChecker.reset();
        }
        // 方法：
        // - 如果正看着可放置方块
        //      立即放置
        // 找一个放置点
        // - 优先选择平坦区域（开放空间，下方有方块）离玩家最近的位置
        // -

        // 首先关闭界面
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
           /* Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            if (moveTo.isPresent()) {
                mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                return null;
            }
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                return null;
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // 如果光标槽是垃圾，则尝试丢弃
            if (garbage.isPresent()) {
                mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                return null;
            }
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);*/
        } else {
            StorageHelper.closeScreen();
        }

        // 尝试在我们当前看的位置放置
        BlockPos current = getCurrentlyLookingBlockPlace(mod);
        if (current != null && _canPlaceHere.test(current)) {
            setDebugState("因为可以放置所以放置...");
            if (mod.getSlotHandler().forceEquipItem(ItemHelper.blocksToItems(toPlace))) {
                if (place(mod, current)) {
                    return null;
                }
            }
        }

        // 在可能的情况下徘徊
        if (wander.isActive() && !wander.isFinished()) {
            setDebugState("徘徊中，稍后再次尝试放置。");
            progressChecker.reset();
            return wander;
        }
        // 失败检查
        if (!progressChecker.check(mod)) {
            Debug.logMessage("放置失败，徘徊并重试。");
            LookHelper.randomOrientation();
            if (tryPlace != null) {
                mod.getBlockScanner().requestBlockUnreachable(tryPlace);
                tryPlace = null;
            }
            return wander;
        }

        // 尝试在特定位置放置
        if (tryPlace == null || !WorldHelper.canReach(tryPlace)) {
            tryPlace = locateClosePlacePos(mod);
        }
        if (tryPlace != null) {
            setDebugState("尝试在 " + tryPlace + " 放置");
            justPlaced = tryPlace;
            return new PlaceBlockTask(tryPlace, toPlace);
        }

        // 向随机方向看，可能获得随机放置机会
        if (_randomlookTimer.elapsed()) {
            _randomlookTimer.reset();
            LookHelper.randomOrientation();
        }

        setDebugState("徘徊直到随机放置或找到好的放置位置。");
        return new TimeoutWanderTask();
    }

    @Override
    protected void onStop(Task interruptTask) {
        stopPlacing();
        EventBus.unsubscribe(_onBlockPlaced);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof PlaceBlockNearbyTask task) {
            // 比较要放置的方块数组是否相等
            return Arrays.equals(task.toPlace, toPlace);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "在附近放置 " + Arrays.toString(toPlace);
    }

    @Override
    public boolean isFinished() {
        // 检查是否已经放置了方块且放置的方块是我们想要的类型
        return justPlaced != null && ArrayUtils.contains(toPlace, AltoClef.getInstance().getWorld().getBlockState(justPlaced).getBlock());
    }

    /**
     * 获取刚刚放置的方块位置
     * @return 已放置方块的位置
     */
    public BlockPos getPlaced() {
        return justPlaced;
    }

    /**
     * 获取当前视线方向上可以放置方块的位置
     * @param mod AltoClef主模块实例
     * @return 可放置方块的位置，如果无法放置则返回null
     */
    private BlockPos getCurrentlyLookingBlockPlace(AltoClef mod) {
        HitResult hit = MinecraftClient.getInstance().crosshairTarget;
        if (hit instanceof BlockHitResult bhit) {
            BlockPos bpos = bhit.getBlockPos();//.subtract(bhit.getSide().getVector());
            //Debug.logMessage("TEMP: A: " + bpos);
            IPlayerContext ctx = mod.getClientBaritone().getPlayerContext();
            if (MovementHelper.canPlaceAgainst(ctx, bpos)) {
                BlockPos placePos = bhit.getBlockPos().add(bhit.getSide().getVector());
                // 不要在玩家内部放置。
                if (WorldHelper.isInsidePlayer(placePos)) {
                    return null;
                }
                //Debug.logMessage("TEMP: B (actual): " + placePos);
                if (WorldHelper.canPlace(placePos)) {
                    return placePos;
                }
            }
        }
        return null;
    }

    /**
     * 检查是否已装备了要放置的方块
     * @return 是否已装备目标方块
     */
    private boolean blockEquipped() {
        return StorageHelper.isEquipped(ItemHelper.blocksToItems(toPlace));
    }

/**
     * 在指定位置放置方块
     * @param mod AltoClef主模块实例
     * @param targetPlace 目标放置位置
     * @return 放置是否成功
     */
    private boolean place(AltoClef mod, BlockPos targetPlace) {
        if (!mod.getExtraBaritoneSettings().isInteractionPaused() && blockEquipped()) {
            // 潜行点击，以确保容器安全。
            mod.getInputControls().hold(Input.SNEAK);

            //mod.getInputControls().tryPress(Input.CLICK_RIGHT);
            // 这在服务器上似乎有效...
            // TODO: 帮助函数
            HitResult mouseOver = MinecraftClient.getInstance().crosshairTarget;
            if (mouseOver == null || mouseOver.getType() != HitResult.Type.BLOCK) {
                return false;
            }
            Hand hand = Hand.MAIN_HAND;
            assert MinecraftClient.getInstance().interactionManager != null;
            if (MinecraftClient.getInstance().interactionManager.interactBlock(mod.getPlayer(),hand, (BlockHitResult) mouseOver) == ActionResult.SUCCESS &&
                    mod.getPlayer().isSneaking()) {
                mod.getPlayer().swingHand(hand);
                justPlaced = targetPlace;
                Debug.logMessage("已按下");
                return true;
            }

            //mod.getControllerExtras().mouseClickOverride(1, false);
            // 呃，这些有时会导致问题，所以这是一种临时修复方法。
            AltoClef.getInstance().getClientBaritone().getBuilderProcess().onLostControl();
            return true;
        }
return false;
    }

    private void stopPlacing() {
        AltoClef.getInstance().getInputControls().release(Input.SNEAK);
        //mod.getControllerExtras().mouseClickOverride(1, false);
        // 呃，这些有时会导致问题，所以这是一种临时修复方法。
        AltoClef.getInstance().getClientBaritone().getBuilderProcess().onLostControl();
    }

    /**
     * 寻找最近的可放置位置
     * @param mod AltoClef主模块实例
     * @return 最佳的放置位置，如果没有找到则返回null
     */
    private BlockPos locateClosePlacePos(AltoClef mod) {
        int range = 7;
        BlockPos best = null;
        double smallestScore = Double.POSITIVE_INFINITY;
        BlockPos start = mod.getPlayer().getBlockPos().add(-range,-range,-range);
        BlockPos end = mod.getPlayer().getBlockPos().add(range,range,range);
        for (BlockPos blockPos : WorldHelper.scanRegion(start, end)) {
            boolean solid = WorldHelper.isSolidBlock(blockPos);
            boolean inside = WorldHelper.isInsidePlayer(blockPos);
            // 我们不能破坏这个方块。
            if (solid && !WorldHelper.canBreak(blockPos)) {
                continue;
            }
            // 用户定义的不能在此放置。
            if (!_canPlaceHere.test(blockPos)) {
                continue;
            }
            // 我们不能在此放置。
            if (!WorldHelper.canReach(blockPos) || !WorldHelper.canPlace(blockPos)) {
                continue;
            }
            boolean hasBelow = WorldHelper.isSolidBlock(blockPos.down());
            double distSq = BlockPosVer.getSquaredDistance(blockPos,mod.getPlayer().getPos());

            // 计算放置位置的评分，距离越近、下方有方块、不是实体内部的得分越高
            double score = distSq + (solid ? 4 : 0) + (hasBelow ? 0 : 10) + (inside ? 3 : 0);

            if (score < smallestScore) {
                best = blockPos;
                smallestScore = score;
            }
        }

        return best;
    }
}
