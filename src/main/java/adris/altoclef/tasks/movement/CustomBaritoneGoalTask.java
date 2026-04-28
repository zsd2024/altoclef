package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.control.InputControls;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import baritone.api.utils.input.Input;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;

/**
 * 将 Baritone 目标转换为任务的抽象基类。
 * 此类提供了通用的移动逻辑，包括路径规划、卡住检测和随机游走功能。
 */
public abstract class CustomBaritoneGoalTask extends Task implements ITaskRequiresGrounded {
    // 随机游走任务，用于在无法前进时尝试摆脱困境
    private final Task wanderTask = new TimeoutWanderTask(5, true);
    // 卡住检测器，用于检测玩家是否被困住
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
    // 是否启用随机游走功能
    private final boolean wander;
    // 移动进度检查器，用于监控任务执行进度
    protected MovementProgressChecker checker = new MovementProgressChecker();
    // 缓存的 Baritone 目标
    protected Goal cachedGoal = null;
    // 定义会阻碍移动的"烦人"方块列表
    Block[] annoyingBlocks = new Block[]{
            Blocks.VINE,                // 藤蔓
            Blocks.NETHER_SPROUTS,      // 下界苗
            Blocks.CAVE_VINES,          // 洞穴藤蔓
            Blocks.CAVE_VINES_PLANT,    // 洞穴藤蔓植株
            Blocks.TWISTING_VINES,      // 缠怨藤
            Blocks.TWISTING_VINES_PLANT,// 缠怨藤植株
            Blocks.WEEPING_VINES_PLANT, // 垂泪藤植株
            Blocks.LADDER,              // 梯子
            Blocks.BIG_DRIPLEAF,        // 大型垂滴叶
            Blocks.BIG_DRIPLEAF_STEM,   // 大型垂滴叶茎
            Blocks.SMALL_DRIPLEAF,      // 小型垂滴叶
            Blocks.TALL_GRASS,          // 高草丛
            Blocks.SHORT_GRASS,         // 矮草丛
            Blocks.SWEET_BERRY_BUSH     // 甜浆果丛
    };
    // 当前正在执行的解困任务
    private Task unstuckTask = null;

    // 在矿井和沼泽/丛林中经常发生这种情况

    /**
     * 构造函数
     * @param wander 是否启用随机游走功能
     */
    public CustomBaritoneGoalTask(boolean wander) {
        this.wander = wander;
    }

    /**
     * 默认构造函数，启用随机游走功能
     */
    public CustomBaritoneGoalTask() {
        this(true);
    }

    /**
     * 生成指定位置周围8个水平方向的位置数组
     * @param pos 中心位置
     * @return 周围8个位置的数组
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
     * 检查指定位置是否包含烦人的方块
     * @param mod AltoClef实例
     * @param pos 要检查的位置
     * @return 如果位置包含烦人的方块则返回true
     */
    private boolean isAnnoying(AltoClef mod, BlockPos pos) {
        for (Block AnnoyingBlocks : annoyingBlocks) {
            return mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock ||
                    mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock;
        }
        return false;
    }

    /**
     * 检测玩家是否被困在烦人的方块中
     * @param mod AltoClef实例
     * @return 如果玩家被困住，返回被困位置；否则返回null
     */
    private BlockPos stuckInBlock(AltoClef mod) {
        BlockPos p = mod.getPlayer().getBlockPos();
        // 检查玩家当前位置
        if (isAnnoying(mod, p)) return p;
        // 检查玩家上方位置
        if (isAnnoying(mod, p.up())) return p.up();
        // 检查玩家周围8个水平方向
        BlockPos[] toCheck = generateSides(p);
        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        // 检查玩家上方周围的8个水平方向
        BlockPos[] toCheckHigh = generateSides(p.up());
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        return null;
    }

    /**
     * 获取用于解困的安全随机移动任务
     * @return SafeRandomShimmyTask实例
     */
    private Task getFenceUnstuckTask() {
        return new SafeRandomShimmyTask();
    }

    @Override
    protected void onStart() {
        // 开始任务时强制取消任何现有的路径规划
        AltoClef.getInstance().getClientBaritone().getPathingBehavior().forceCancel();
        checker.reset();
        stuckCheck.reset();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();
        InputControls controls = mod.getInputControls();
        
        // 如果正在进行路径规划，重置进度检查器
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            checker.reset();
        }
        // 处理下界传送门内的特殊情况
        if (WorldHelper.isInNetherPortal()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("从下界传送门中退出");
                controls.hold(Input.SNEAK);
                controls.hold(Input.MOVE_FORWARD);
                return null;
            } else {
                controls.release(Input.SNEAK);
                controls.release(Input.MOVE_BACK);
                controls.release(Input.MOVE_FORWARD);
            }
        } else {
            // 正常情况下的输入控制
            if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                controls.release(Input.SNEAK);
                controls.release(Input.MOVE_BACK);
                controls.release(Input.MOVE_FORWARD);
            }
        }
        // 如果有解困任务正在执行且玩家仍然被困
        if (unstuckTask != null && unstuckTask.isActive() && !unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("从方块中解困。");
            stuckCheck.reset();
            // 停止其他任务，专注于解困
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return unstuckTask;
        }
        // 检查是否卡住
        if (!checker.check(mod) || !stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                unstuckTask = getFenceUnstuckTask();
                return unstuckTask;
            }
            stuckCheck.reset();
        }
        // 如果没有缓存目标，创建新目标
        if (cachedGoal == null) {
            cachedGoal = newGoal(mod);
        }

        // 处理随机游走逻辑
        if (wander) {
            if (isFinished()) {
                // 如果已达到目标，不要随机游走
                checker.reset();
            } else {
                if (wanderTask.isActive() && !wanderTask.isFinished()) {
                    setDebugState("随机游走中...");
                    checker.reset();
                    return wanderTask;
                }
                if (!checker.check(mod)) {
                    Debug.logMessage("无法在目标上取得进展，开始随机游走。");
                    onWander(mod);
                    return wanderTask;
                }
            }
        }
        // 设置并开始执行Baritone目标
        if (!mod.getClientBaritone().getCustomGoalProcess().isActive()
                && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
            mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(cachedGoal);
        }
        setDebugState("完成目标。");
        return null;
    }

    @Override
    public boolean isFinished() {
        if (cachedGoal == null) {
            cachedGoal = newGoal(AltoClef.getInstance());
        }
        return cachedGoal != null && cachedGoal.isInGoal(AltoClef.getInstance().getPlayer().getBlockPos());
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 停止任务时强制取消路径规划
        AltoClef.getInstance().getClientBaritone().getPathingBehavior().forceCancel();
    }

    /**
     * 创建新的Baritone目标（由子类实现）
     * @param mod AltoClef实例
     * @return 新的Baritone目标
     */
    protected abstract Goal newGoal(AltoClef mod);

    /**
     * 当需要随机游走时调用的钩子方法（由子类可选实现）
     * @param mod AltoClef实例
     */
    protected void onWander(AltoClef mod) {
    }
}
