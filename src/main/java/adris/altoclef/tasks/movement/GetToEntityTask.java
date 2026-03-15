package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalFollowEntity;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.utils.input.Input;
import net.minecraft.block.*;
import adris.altoclef.multiversion.versionedfields.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * 前往实体任务 - 前往指定实体
 */
public class GetToEntityTask extends Task implements ITaskRequiresGrounded {
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker(); // 卡住检查器
    private final MovementProgressChecker _progress = new MovementProgressChecker(); // 进度检查器
    private final TimeoutWanderTask _wanderTask = new TimeoutWanderTask(5); // 超时漫步任务
    private final Entity _entity; // 目标实体
    private final double _closeEnoughDistance; // 足够接近的距离
    // 烦人的方块列表（可能导致卡住）
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
    private Task _unstuckTask = null; // 解除卡住任务

    public GetToEntityTask(Entity entity, double closeEnoughDistance) {
        _entity = entity;
        _closeEnoughDistance = closeEnoughDistance;
    }

    public GetToEntityTask(Entity entity) {
        this(entity, 1);
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
        if (annoyingBlocks != null) {
            for (Block AnnoyingBlocks : annoyingBlocks) {
                return mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks ||
                        mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock || // 门
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock || // 栅栏
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock || // 栅栏门
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock; // 花
            }
        }
        return false;
    }

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
        AltoClef.getInstance().getClientBaritone().getPathingBehavior().forceCancel();
        _progress.reset(); // 重置进度检查器
        stuckCheck.reset(); // 重置卡住检查器
        _wanderTask.resetWander(); // 重置漫步任务
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            _progress.reset();
        }
        // 如果在下界传送门中
        if (WorldHelper.isInNetherPortal()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("从下界传送门出来");
                mod.getInputControls().hold(Input.SNEAK); // 按住潜行
                mod.getInputControls().hold(Input.MOVE_FORWARD); // 按住向前移动
                return null;
            } else {
                // 释放按键
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        } else {
            if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                // 释放按键
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        }
        // 如果有解卡任务且卡在方块中
        if (_unstuckTask != null && _unstuckTask.isActive() && !_unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("从方块中解卡。");
            stuckCheck.reset();
            // 停止其他任务，我们只进行摆动
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return _unstuckTask;
        }
        // 如果进度检查失败或卡住检查失败
        if (!_progress.check(mod) || !stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                _unstuckTask = getFenceUnstuckTask();
                return _unstuckTask;
            }
            stuckCheck.reset();
        }
        // 如果漫步任务正在运行但未完成
        if (_wanderTask.isActive() && !_wanderTask.isFinished()) {
            _progress.reset();
            setDebugState("未能到达目标，漫游一会儿。");
            return _wanderTask;
        }

        // 设置跟随实体的路径目标
        if (!mod.getClientBaritone().getCustomGoalProcess().isActive()) {
            mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(new GoalFollowEntity(_entity, _closeEnoughDistance));
        }

        // 如果玩家在实体范围内
        if (mod.getPlayer().isInRange(_entity, _closeEnoughDistance)) {
            _progress.reset();
        }

        // 如果进度检查失败
        if (!_progress.check(mod)) {
            return _wanderTask;
        }

        setDebugState("前往实体");
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getClientBaritone().getPathingBehavior().forceCancel();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToEntityTask task) {
            return task._entity.equals(_entity) && Math.abs(task._closeEnoughDistance - _closeEnoughDistance) < 0.1;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "接近实体 " + _entity.getType().getTranslationKey();
    }
}
