package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.BotBehaviour;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.trackers.BlockScanner;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;

/**
 * 使用桶和铸造法放置黑曜石任务 - 通过放置岩浆和水来生成黑曜石
 */
public class PlaceObsidianBucketTask extends Task {

    // 铸造框架的位置偏移数组，用于构建放置岩浆和水所需的结构
    public static final Vec3i[] CAST_FRAME = new Vec3i[]{
            new Vec3i(0, -1, 0),    // 下方中心
            new Vec3i(0, -1, -1),   // 下方后方
            new Vec3i(0, -1, 1),    // 下方前方
            new Vec3i(-1, -1, 0),   // 下方左侧
            new Vec3i(1, -1, 0),    // 下方右侧
            new Vec3i(0, 0, -1),    // 同层后方
            new Vec3i(0, 0, 1),     // 同层前方
            new Vec3i(-1, 0, 0),    // 同层左侧
            new Vec3i(1, 0, 0),     // 同层右侧
            new Vec3i(1, 1, 0)      // 上方右侧
    };
    // 移动进度检查器，用于检测任务是否卡住
    private final MovementProgressChecker _progressChecker = new MovementProgressChecker();
    // 目标放置位置
    private final BlockPos _pos;

    // 当前需要放置框架的目标位置
    private BlockPos _currentCastTarget;
    // 当前需要破坏的目标位置
    private BlockPos _currentDestroyTarget;

    public PlaceObsidianBucketTask(BlockPos pos) {
        _pos = pos;
    }

    @Override
    protected void onStart() {
        BotBehaviour botBehaviour = AltoClef.getInstance().getBehaviour();

        // 将行为压入行为栈
        botBehaviour.push();

        // 避免破坏铸造框架内的方块
        botBehaviour.avoidBlockBreaking(this::isBlockInCastFrame);

        // 避免在岩浆或水位置放置方块
        botBehaviour.avoidBlockPlacing(this::isBlockInCastWaterOrLava);

        // 重置进度检查器
        _progressChecker.reset();

        Debug.logInternal("开始执行onStart方法");
        Debug.logInternal("行为已压入栈");
        Debug.logInternal("避免破坏方块");
        Debug.logInternal("避免放置方块");
        Debug.logInternal("进度检查器已重置");
    }

    /**
 * 检查方块位置是否在铸造框架内
 * @param block 要检查的方块位置
 * @return 如果方块位置在铸造框架内则返回true
 */
private boolean isBlockInCastFrame(BlockPos block) {
        return Arrays.stream(PlaceObsidianBucketTask.CAST_FRAME)
                .map(_pos::add)
                .anyMatch(block::equals);
    }

    /**
 * 检查给定的方块位置是否是当前位置或其上方位置
 * @param blockPos 要检查的方块位置
 * @return 如果方块位置是当前位置或其上方位置则返回true，否则返回false
 */
    private boolean isBlockInCastWaterOrLava(BlockPos blockPos) {
        // 计算当前位置上方的位置
        BlockPos waterTarget = _pos.up();

        Debug.logInternal("blockPos: " + blockPos);
        Debug.logInternal("waterTarget: " + waterTarget);

        // 检查方块位置是否是当前位置或其上方位置
        return blockPos.equals(_pos) || blockPos.equals(waterTarget);
    }

    /**
     * 周期性执行的任务逻辑
     * 处理使用岩浆桶和水桶进行黑曜石生成的逻辑
     * @return 要执行的下一个任务
     */
    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果正在寻路则重置进度
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            _progressChecker.reset();
        }

        // 清除多余的水
        if (mod.getBlockScanner().isBlockAtPosition(_pos, Blocks.OBSIDIAN) && mod.getBlockScanner().isBlockAtPosition(_pos.up(), Blocks.WATER)) {
            return new ClearLiquidTask(_pos.up());
        }

        // 确保我们有一个水桶
        if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
            _progressChecker.reset();
            return TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
        }

        // 确保我们有一个岩浆桶
        if (!mod.getItemStorage().hasItem(Items.LAVA_BUCKET)) {
            // 唯一的例外是我们当前位置有岩浆
            if (!mod.getBlockScanner().isBlockAtPosition(_pos, Blocks.LAVA)) {
                _progressChecker.reset();
                return TaskCatalogue.getItemTask(Items.LAVA_BUCKET, 1);
            }
        }

        // 检查进度
        if (!_progressChecker.check(mod)) {
            mod.getClientBaritone().getPathingBehavior().forceCancel();
            mod.getBlockScanner().requestBlockUnreachable(_pos);
            _progressChecker.reset();
            return new TimeoutWanderTask(5);
        }

        // 如果尚未构建，则构建铸造框架
        if (_currentCastTarget != null) {
            if (WorldHelper.isSolidBlock(_currentCastTarget)) {
                _currentCastTarget = null;
            } else {
                return new PlaceBlockTask(_currentCastTarget,
                        Arrays.stream(ItemHelper.itemsToBlocks(mod.getModSettings().getThrowawayItems(mod))).filter((b)-> !Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.LEAVES)).toList().contains(b)).toArray(Block[]::new)
                );
            }
        }

        // 如果需要则破坏方块
        if (_currentDestroyTarget != null) {
            if (!WorldHelper.isSolidBlock(_currentDestroyTarget)) {
                _currentDestroyTarget = null;
            } else {
                return new DestroyBlockTask(_currentDestroyTarget);
            }
        }

        // 如果尚未构建，则构建铸造框架
        if (_currentCastTarget != null && WorldHelper.isSolidBlock(_currentCastTarget)) {
            // 当前铸造框架已构建完成
            _currentCastTarget = null;
        }
        for (Vec3i castPosRelative : CAST_FRAME) {
            BlockPos castPos = _pos.add(castPosRelative);
            if (!WorldHelper.isSolidBlock(castPos)) {
                _currentCastTarget = castPos;
                Debug.logInternal("正在构建铸造框架...");
                return null;
            }
        }

        // 放置岩浆
        if (mod.getWorld().getBlockState(_pos).getBlock() != Blocks.LAVA) {
            // 不要在我们自己的位置放置岩浆！
            // 会导致尴尬的死亡
            BlockPos targetPos = _pos.add(-1,1,0);
            if (!mod.getPlayer().getBlockPos().equals(targetPos) && mod.getItemStorage().hasItem(Items.LAVA_BUCKET)) {
                Debug.logInternal("放置岩浆前调整玩家位置...");
                return new GetToBlockTask(targetPos, false);
            }
            if (WorldHelper.isSolidBlock(_pos)) {
                Debug.logInternal("清理岩浆周围的空隙...");
                _currentDestroyTarget = _pos;
                return null;
            }
            // 同时清理上方两格，使放置更可靠
            if (WorldHelper.isSolidBlock(_pos.up())) {
                Debug.logInternal("清理岩浆周围的空隙...");
                _currentDestroyTarget = _pos.up();
                return null;
            }
            if (WorldHelper.isSolidBlock(_pos.up(2))) {
                Debug.logInternal("清理岩浆周围的空隙...");
                _currentDestroyTarget = _pos.up(2);
                return null;
            }
            Debug.logInternal("放置岩浆进行铸造...");
            return new InteractWithBlockTask(new ItemTarget(Items.LAVA_BUCKET, 1), Direction.WEST, _pos.add(1,0,0), false);
        }
        // 岩浆已放置，现在放置水
        BlockPos waterCheck = _pos.up();
        if (mod.getWorld().getBlockState(waterCheck).getBlock() != Blocks.WATER) {
            Debug.logInternal("放置水进行铸造...");
            // 调整位置以避免卡住的情况
            BlockPos targetPos = _pos.add(-1,1,0);
            if (!mod.getPlayer().getBlockPos().equals(targetPos) && mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                Debug.logInternal("放置水前调整玩家位置...");
                return new GetToBlockTask(targetPos, false);
            }
            if (WorldHelper.isSolidBlock(waterCheck)) {
                _currentDestroyTarget = waterCheck;
                return null;
            }
            if (WorldHelper.isSolidBlock(waterCheck.up())) {
                _currentDestroyTarget = waterCheck.up();
                return null;
            }
            return new InteractWithBlockTask(new ItemTarget(Items.WATER_BUCKET, 1), Direction.WEST, _pos.add(1,1,0), true);
        }
        return null;
    }

    /**
     * 任务被中断时调用此方法
     * @param interruptTask 导致中断的任务
     */
    @Override
    protected void onStop(Task interruptTask) {
        // 检查模组行为是否不为空
        if (AltoClef.getInstance().getBehaviour() != null) {
            // 从栈中弹出行为
            AltoClef.getInstance().getBehaviour().pop();
            Debug.logInternal("行为已弹出栈。");
        }
    }

    /**
     * 检查当前任务是否完成
     * 当指定位置的方块是黑曜石且其上方没有水方块时，任务被认为已完成
     * @return 任务完成返回true，否则返回false
     */
    @Override
    public boolean isFinished() {
        // 从模组获取BlockTracker实例
        BlockScanner blockTracker = AltoClef.getInstance().getBlockScanner();

        // 获取要检查的方块位置
        BlockPos pos = _pos;

        // 检查指定位置的方块是否是黑曜石
        boolean isObsidian = blockTracker.isBlockAtPosition(pos, Blocks.OBSIDIAN);
        Debug.logInternal("isObsidian: " + isObsidian);

        // 检查指定位置上方是否没有水方块
        boolean isNotWaterAbove = !blockTracker.isBlockAtPosition(pos.up(), Blocks.WATER);
        Debug.logInternal("isNotWaterAbove: " + isNotWaterAbove);

        // 当方块是黑曜石且上方没有水时，任务被认为已完成
        boolean isFinished = isObsidian && isNotWaterAbove;
        Debug.logInternal("isFinished: " + isFinished);

        return isFinished;
    }

    /**
     * 检查给定任务是否与此PlaceObsidianBucketTask相等
     * 当两个PlaceObsidianBucketTask的位置相等时，它们被认为是相等的
     * 重写父类的isEqual()方法
     * @param other 要比较的任务
     * @return 任务相等返回true，否则返回false
     */
    @Override
    protected boolean isEqual(Task other) {
        // 检查另一个任务是否是PlaceObsidianBucketTask的实例
        if (other instanceof PlaceObsidianBucketTask task) {
            // 检查位置是否相等
            boolean isEqual = task.getPos().equals(getPos());
            Debug.logInternal("isEqual: " + isEqual);
            return isEqual;
        }
        Debug.logInternal("isEqual: false");
        return false;
    }

    @Override
    protected String toDebugString() {
        return "正在 " + _pos + " 位置通过铸造法放置黑曜石";
    }

    /**
     * 获取对象的位置
     * @return 对象的位置
     */
    public BlockPos getPos() {
        Debug.logInternal("进入getPos()");

        return _pos;
    }
}
