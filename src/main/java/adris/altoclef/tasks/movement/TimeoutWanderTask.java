package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Optional;

// TODO 改进漫步
/**
 * 当您当前所在的位置由于某种原因不好且您只想离开时调用此任务。
 */
public class TimeoutWanderTask extends Task implements ITaskRequiresGrounded {
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker(); // 卡住检查器
    private final float distanceToWander; // 漫步距离
    private final MovementProgressChecker progressChecker = new MovementProgressChecker(); // 进度检查器
    private final boolean increaseRange; // 是否增加范围
    private final TimerGame timer = new TimerGame(60); // 计时器
    // 烦人的方块列表
    Block[] annoyingBlocks = new Block[]{
            Blocks.VINE, // 藤蔓
            Blocks.NETHER_SPROUTS, // 下界芽
            Blocks.CAVE_VINES, // 洞穴藤蔓
            Blocks.CAVE_VINES_PLANT, // 洞穴藤蔓植物
            Blocks.TWISTING_VINES, // 扭曲藤蔓
            Blocks.TWISTING_VINES_PLANT, // 扭曲藤蔓植物
            Blocks.WEEPING_VINES_PLANT, // 垂泪藤蔓植物
            Blocks.LADDER, // 梯子
            Blocks.BIG_DRIPLEAF, // 大型垂滴叶
            Blocks.BIG_DRIPLEAF_STEM, // 大型垂滴叶茎
            Blocks.SMALL_DRIPLEAF, // 小型垂滴叶
            Blocks.TALL_GRASS, // 高草
            Blocks.SHORT_GRASS, // 矮草
            Blocks.SWEET_BERRY_BUSH // 甜浆果丛
    };
    private Vec3d origin; // 起始位置
    //private DistanceProgressChecker _distanceProgressChecker = new DistanceProgressChecker(10, 0.1f);
    private boolean _forceExplore; // 是否强制探索
    private Task _unstuckTask = null; // 解卡任务
    private int failCounter; // 失败计数器
    private double _wanderDistanceExtension; // 漫步距离扩展

    public TimeoutWanderTask(float distanceToWander, boolean increaseRange) {
        this.distanceToWander = distanceToWander;
        this.increaseRange = increaseRange;
        _forceExplore = false;
    }

    public TimeoutWanderTask(float distanceToWander) {
        this(distanceToWander, false);
    }

    public TimeoutWanderTask() {
        this(Float.POSITIVE_INFINITY, false);
    }

    public TimeoutWanderTask(boolean forceExplore) {
        this();
        _forceExplore = forceExplore;
    }

    /**
     * 生成周围的8个方块位置
     * @param pos 中心位置
     * @return 周围的方块位置数组
     */
    private static BlockPos[] generateSides(BlockPos pos) {
        return new BlockPos[]{
                pos.add(1,0,0),   // 东
                pos.add(-1,0,0),  // 西
                pos.add(0,0,1),   // 南
                pos.add(0,0,-1),  // 北
                pos.add(1,0,-1),  // 东北
                pos.add(1,0,1),   // 东南
                pos.add(-1,0,-1), // 西北
                pos.add(-1,0,1)   // 西南
        };
    }

    /**
     * 检查指定位置的方块是否令人烦恼（可能导致卡住）
     * @param mod AltoClef实例
     * @param pos 位置
     * @return 是否为令人烦恼的方块
     */
    private boolean isAnnoying(AltoClef mod, BlockPos pos) {
        for (Block AnnoyingBlocks : annoyingBlocks) {
            return mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock || // 门
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock || // 栅栏
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock || // 栅栏门
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock; // 花
        }
        return false;
    }

    /**
     * 重置漫步
     */
    public void resetWander() {
        _wanderDistanceExtension = 0;
    }

    // 这在矿井和沼泽/丛林中经常发生
    /**
     * 检查玩家是否卡在烦人的方块中
     * 这在矿井和沼泽/丛林中经常发生
     * @param mod AltoClef实例
     * @return 卡住的方块位置，如果没有则返回null
     */
    private BlockPos stuckInBlock(AltoClef mod) {
        BlockPos p = mod.getPlayer().getBlockPos();
        // 检查玩家脚下和上方的方块
        if (isAnnoying(mod, p)) return p;
        if (isAnnoying(mod, p.up())) return p.up();
        // 检查周围方块
        BlockPos[] toCheck = generateSides(p);
        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        // 检查上方一层的周围方块
        BlockPos[] toCheckHigh = generateSides(p.up());
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        return null;
    }

    /**
     * 获取栅栏解卡任务
     * @return 解卡任务
     */
    private Task getFenceUnstuckTask() {
        return new SafeRandomShimmyTask(); // 安全随机摆动任务
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        timer.reset();
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        origin = mod.getPlayer().getPos();
        progressChecker.reset();
        stuckCheck.reset();
        failCounter = 0;
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // 如果是垃圾，尝试丢弃光标槽中的物品
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();


        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            progressChecker.reset();
        }
        if (WorldHelper.isInNetherPortal()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("从下界传送门出来");
                mod.getInputControls().hold(Input.SNEAK); // 按住潜行
                mod.getInputControls().hold(Input.MOVE_FORWARD); // 按住向前移动
                return null;
            } else {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        } else {
            if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        }
        if (_unstuckTask != null && _unstuckTask.isActive() && !_unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("从方块中解卡。");
            stuckCheck.reset();
            // 停止其他任务，我们只进行摆动
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return _unstuckTask;
        }
        if (!progressChecker.check(mod) || !stuckCheck.check(mod)) {
            List<Entity> closeEntities = mod.getEntityTracker().getCloseEntities();
            for (Entity CloseEntities : closeEntities) {
                if (CloseEntities instanceof MobEntity &&
                        CloseEntities.getPos().isInRange(mod.getPlayer().getPos(), 1)) {
                    setDebugState("杀死烦人的实体。");
                    return new KillEntitiesTask(CloseEntities.getClass());
                }
            }
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                failCounter++;
                _unstuckTask = getFenceUnstuckTask();
                return _unstuckTask;
            }
            stuckCheck.reset();
        }
        setDebugState("探索中。");
        switch (WorldHelper.getCurrentDimension()) {
            case END -> {
                if (timer.getDuration() >= 30) {
                    timer.reset();
                }
            }
            case OVERWORLD, NETHER -> {
                if (timer.getDuration() >= 30) {
                }
                if (timer.elapsed()) {
                    timer.reset();
                }
            }
        }
        if (!mod.getClientBaritone().getExploreProcess().isActive()) {
            mod.getClientBaritone().getExploreProcess().explore((int) origin.getX(), (int) origin.getZ());
        }
        if (!progressChecker.check(mod)) {
            progressChecker.reset();
            if (!_forceExplore) {
                failCounter++;
                Debug.logMessage("探索失败。");
            }
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getClientBaritone().getPathingBehavior().forceCancel();
        if (isFinished()) {
            if (increaseRange) {
                _wanderDistanceExtension += distanceToWander;
                Debug.logMessage("增加漫步范围");
            }
        }
    }

    @Override
    public boolean isFinished() {
        // 为什么我要添加这个？
        //if (_origin == null) return true;

        if (Float.isInfinite(distanceToWander)) return false;

        // 如果我们失败10次或更多，我们不妨再次尝试之前的任务。
        if (failCounter > 10) {
            return true;
        }

        ClientPlayerEntity player = AltoClef.getInstance().getPlayer();

        if (player != null && player.getPos() != null && (player.isOnGround() ||
                player.isTouchingWater())) {
            double sqDist = player.getPos().squaredDistanceTo(origin);
            double toWander = distanceToWander + _wanderDistanceExtension;
            return sqDist > toWander * toWander;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof TimeoutWanderTask task) {
            if (Float.isInfinite(task.distanceToWander) || Float.isInfinite(distanceToWander)) {
                return Float.isInfinite(task.distanceToWander) == Float.isInfinite(distanceToWander);
            }
            return Math.abs(task.distanceToWander - distanceToWander) < 0.5f;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "漫步 " + (distanceToWander + _wanderDistanceExtension) + " 个方块";
    }
}
