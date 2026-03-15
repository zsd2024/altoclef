package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.movement.RunAwayFromPositionTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.*;
import adris.altoclef.multiversion.versionedfields.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 破坏指定位置的方块
 */
public class DestroyBlockTask extends Task implements ITaskRequiresGrounded {
    // 卡住检查器
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
    // 移动检查器
    private final MovementProgressChecker _moveChecker = new MovementProgressChecker();
    // 目标位置
    private final BlockPos pos;
    // 麻烦的方块类型数组（如藤蔓、梯子等）
    Block[] annoyingBlocks = new Block[]{
            Blocks.VINE,                  // 藤蔓
            Blocks.NETHER_SPROUTS,        // 下界苗
            Blocks.CAVE_VINES,            // 洞穴藤蔓
            Blocks.CAVE_VINES_PLANT,      // 洞穴藤蔓植物
            Blocks.TWISTING_VINES,        // 缠怨藤
            Blocks.TWISTING_VINES_PLANT,  // 缠怨藤植物
            Blocks.WEEPING_VINES_PLANT,   // 垂泪藤植物
            Blocks.LADDER,                // 梯子
            Blocks.BIG_DRIPLEAF,          // 大型垂滴叶
            Blocks.BIG_DRIPLEAF_STEM,     // 大型垂滴叶茎
            Blocks.SMALL_DRIPLEAF,        // 小型垂滴叶
            Blocks.TALL_GRASS,            // 高草
            Blocks.SHORT_GRASS,           // 短草
            Blocks.SWEET_BERRY_BUSH       // 甜浆果丛
    };
    // 解困任务
    private Task unstuckTask = null;
    // 是否正在挖掘
    private boolean isMining;

    public DestroyBlockTask(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * 生成代表给定BlockPos周围侧面的BlockPos数组
     *
     * @param pos 要生成侧面的BlockPos对象
     * @return 代表给定BlockPos侧面的BlockPos数组
     */
    private static BlockPos[] generateSides(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        Debug.logInternal("x = " + x);
        Debug.logInternal("y = " + y);
        Debug.logInternal("z = " + z);

        return new BlockPos[]{
                new BlockPos(x + 1, y, z),
                new BlockPos(x - 1, y, z),
                new BlockPos(x, y, z + 1),
                new BlockPos(x, y, z - 1),
                new BlockPos(x + 1, y, z - 1),
                new BlockPos(x + 1, y, z + 1),
                new BlockPos(x - 1, y, z - 1),
                new BlockPos(x - 1, y, z + 1)
        };
    }

    /**
     * 检查方块是否是麻烦的方块
     *
     * @param mod AltoClef模组实例
     * @param pos 方块的位置
     * @return 如果方块是麻烦的方块则返回true，否则返回false
     */
    private boolean isAnnoying(AltoClef mod, BlockPos pos) {
        for (Block annoyingBlock : annoyingBlocks) {
            boolean isAnnoying = mod.getWorld().getBlockState(pos).getBlock() == annoyingBlock
                    || mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock      // 门方块
                    || mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock     // 栅栏方块
                    || mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock // 栅栏门方块
                    || mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock;   // 花朵方块
            if (isAnnoying) {
                Debug.logInternal("位置 " + pos + " 处的方块是麻烦的方块。");
                return true;
            }
        }
        Debug.logInternal("位置 " + pos + " 处的方块不是麻烦的方块。");
        return false;
    }

    /**
     * 返回玩家被卡住的方块位置
     * 如果没有麻烦的方块位置，则返回null
     *
     * @param mod AltoClef模组实例
     * @return 被卡住的方块位置，如果没有找到则返回null
     */
    private BlockPos stuckInBlock(AltoClef mod) {
        BlockPos playerPos = mod.getPlayer().getBlockPos();
        BlockPos[] toCheck = generateSides(playerPos);
        BlockPos[] toCheckHigh = generateSides(playerPos.up());

        // 检查玩家位置是否是麻烦的方块
        if (isAnnoying(mod, playerPos)) {
            Debug.logInternal("玩家位置是麻烦的方块: " + playerPos);
            return playerPos;
        }

        // 检查玩家上方位置是否是麻烦的方块
        if (isAnnoying(mod, playerPos.up())) {
            Debug.logInternal("玩家上方位置是麻烦的方块: " + playerPos.up());
            return playerPos.up();
        }

        // 检查每个侧面的方块位置
        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check)) {
                Debug.logInternal("方块位置是麻烦的方块: " + check);
                return check;
            }
        }

        // 检查每个高处的方块位置
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check)) {
                Debug.logInternal("方块位置（上方）是麻烦的方块: " + check);
                return check;
            }
        }

        Debug.logInternal("未找到麻烦的方块位置。");
        return null;
    }

    /**
     * 获取摆脱栅栏卡住的任务
     *
     * @return 摆脱栅栏卡住的任务
     */
    private Task getFenceUnstuckTask() {
        Debug.logInternal("进入getFenceUnstuckTask");

        Task task = createSafeRandomShimmyTask();

        Debug.logInternal("退出getFenceUnstuckTask");

        return task;
    }

    /**
     * 创建一个新的SafeRandomShimmyTask实例
     *
     * @return 创建的SafeRandomShimmyTask
     */
    private Task createSafeRandomShimmyTask() {
        Task task = new SafeRandomShimmyTask();
        Debug.logInternal("创建了SafeRandomShimmyTask: " + task);
        return task;
    }

    /**
     * 模组启动时调用此方法
     * 取消任何正在进行的寻路行为，重置移动检查器和卡住检查器
     * 如果光标堆栈不为空，尝试将其移动到玩家背包中的合适槽位
     * 如果物品可以丢弃，则将其丢弃到未定义槽位或垃圾槽位
     * 如果光标堆栈为空，则关闭界面
     */
    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        // 取消任何正在进行的寻路行为
        mod.getClientBaritone().getPathingBehavior().forceCancel();

        // 重置移动检查器和卡住检查器
        _moveChecker.reset();
        stuckCheck.reset();

        // 获取光标槽位中的物品堆叠
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        Debug.logInternal("光标堆叠: " + cursorStack);

        // 如果光标堆叠不为空，尝试将其移动到玩家背包中的合适槽位
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            Debug.logInternal("移动到槽位: " + moveTo);

            // 如果有槽位可以容纳物品，则点击该槽位来移动物品
            moveTo.ifPresent(slot -> {
                mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                Debug.logInternal("点击了槽位: " + slot);
            });

            // 如果物品可以丢弃，点击未定义槽位来丢弃物品
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                Debug.logInternal("点击了未定义槽位");
            }

            // 获取垃圾槽位并点击它来移动物品
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            Debug.logInternal("垃圾槽位: " + garbage);

            garbage.ifPresent(slot -> {
                mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                Debug.logInternal("点击了槽位: " + slot);
            });

            // 点击未定义槽位来丢弃物品
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            Debug.logInternal("点击了未定义槽位");
        } else {
            // 如果光标堆叠为空，则关闭界面
            StorageHelper.closeScreen();
            Debug.logInternal("关闭了界面");
        }
    }

    /**
     * 周期性调用此方法来执行各种任务
     *
     * @return 要执行的下一个任务
     */
    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 检查指定位置是否有白色羊毛
        if (mod.getWorld().getBlockState(pos).getBlock() == Blocks.WHITE_WOOL) {
            // 遍历世界中的所有实体
            Iterable<Entity> entities = mod.getWorld().getEntities();
            for (Entity entity : entities) {
                // 检查实体是否是掠夺者且在距离位置144格以内
                if (entity instanceof PillagerEntity && pos.isWithinDistance(entity.getPos(), 144)) {
                    Debug.logMessage("屏蔽掠夺者羊毛。");
                    // 请求将该位置的方块标记为不可到达
                    mod.getBlockScanner().requestBlockUnreachable(pos, 0);
                }
            }
        }

        // 如果Baritone当前正在寻路，则重置移动检查器
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            _moveChecker.reset();
        }

        // 检查玩家是否在下界传送门中
        if (WorldHelper.isInNetherPortal()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("正在从下界传送门中退出");
                // 按住潜行和向前移动输入以退出下界传送门
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                return null;
            } else {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        } else if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.MOVE_BACK);
            mod.getInputControls().release(Input.MOVE_FORWARD);
        }

        // 检查是否有活跃的解困任务且玩家卡在方块中
        if (unstuckTask != null && unstuckTask.isActive() && !unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("正在摆脱方块卡住。");
            stuckCheck.reset();
            // 释放Baritone的自定义目标进程和探索进程的控制权
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return unstuckTask;
        }

        // 检查移动检查器或卡住检查器是否失败
        if (!_moveChecker.check(mod) || !stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                unstuckTask = getFenceUnstuckTask();
                return unstuckTask;
            }
            stuckCheck.reset();
        }

        // 检查移动检查器是否失败
        if (!_moveChecker.check(mod)) {
            _moveChecker.reset();
            // 请求将该位置的方块标记为不可到达
            mod.getBlockScanner().requestBlockUnreachable(pos);
        }

        // 检查位置上方的方块是否不是固体，玩家在位置上方，
        // 且玩家在距离0.89格以内
        if (!WorldHelper.isSolidBlock(pos.up()) && mod.getPlayer().getPos().y > pos.getY() && pos.isWithinDistance(mod.getPlayer().isOnGround() ? mod.getPlayer().getPos() : mod.getPlayer().getPos().add(0, -1, 0), 0.89)) {
            if (WorldHelper.dangerousToBreakIfRightAbove(pos)) {
                setDebugState("从上方破坏是危险的，远离并重试。");
                return new RunAwayFromPositionTask(3, pos.getY(), pos);
            }
        }

        Optional<Rotation> reach = LookHelper.getReach(pos);
        if (reach.isPresent() && (mod.getPlayer().isTouchingWater() || mod.getPlayer().isOnGround()) && !mod.getFoodChain().needsToEat() && !WorldHelper.isInNetherPortal() && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
            setDebugState("方块在范围内，正在挖掘...");
            stuckCheck.reset();
            isMining = true;
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.MOVE_BACK);
            mod.getInputControls().release(Input.MOVE_FORWARD);
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getBuilderProcess().onLostControl();
            if (!LookHelper.isLookingAt(mod, reach.get())) {
                LookHelper.lookAt(reach.get());
            }
            // 工具装备在`PlayerInteractionFixChain`中处理。呃。
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
        } else {
            setDebugState("正在前往方块...");
            if (isMining && mod.getPlayer().isTouchingWater()) {
                setDebugState("我们在水中... 按住破坏按钮");
                isMining = false;
                mod.getBlockScanner().requestBlockUnreachable(pos);
                mod.getInputControls().hold(Input.CLICK_LEFT);
            } else {
                isMining = false;
            }
            boolean isCloseToMoveBack = pos.isWithinDistance(mod.getPlayer().getPos(), 2);
            if (isCloseToMoveBack) {
                if (!mod.getClientBaritone().getPathingBehavior().isPathing() && !mod.getPlayer().isTouchingWater() &&
                        !mod.getFoodChain().needsToEat()) {
                    mod.getInputControls().hold(Input.MOVE_BACK);
                    mod.getInputControls().hold(Input.SNEAK);
                } else {
                    mod.getInputControls().release(Input.MOVE_BACK);
                    mod.getInputControls().release(Input.SNEAK);
                }
            }
            if (!mod.getClientBaritone().getCustomGoalProcess().isActive()) {
                mod.getClientBaritone().getBuilderProcess().onLostControl();
                // 如果上方方块是雪，则使用GoalBlock，否则使用GoalNear
                mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(mod.getWorld().getBlockState(pos.up()).getBlock() ==
                        Blocks.SNOW ? new GoalBlock(pos) : new GoalNear(pos, 1));
            }
        }
        return null;
    }

    /**
     * 任务被中断或停止时调用此方法
     * 取消Baritone寻路并释放某些输入控制
     *
     * @param interruptTask 中断当前任务的任务
     */
    @Override
    protected void onStop(Task interruptTask) {
        AltoClef mod = AltoClef.getInstance();

        // 取消Baritone寻路
        mod.getClientBaritone().getPathingBehavior().forceCancel();

        // 如果不在游戏中，则返回
        if (!AltoClef.inGame()) {
            return;
        }

        // 释放输入控制
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
        mod.getInputControls().release(Input.SNEAK);
        mod.getInputControls().release(Input.MOVE_BACK);
        mod.getInputControls().release(Input.MOVE_FORWARD);

        Debug.logInternal("onStop方法被调用");
        Debug.logInternal("Baritone寻路已取消");
        if (!AltoClef.inGame()) {
            Debug.logInternal("不在游戏中");
        }
        Debug.logInternal("左键点击输入强制状态设置为false");
        Debug.logInternal("释放了潜行输入控制");
        Debug.logInternal("释放了后退输入控制");
        Debug.logInternal("释放了前进输入控制");
    }

    /**
     * 检查给定位置的方块是否是空气
     *
     * @return 如果方块是空气则返回true，否则返回false
     */
    @Override
    public boolean isFinished() {
        BlockState blockState = AltoClef.getInstance().getWorld().getBlockState(pos);
        boolean isAir = blockState.isAir();
        Debug.logInternal("位置 " + pos + " 处的方块是空气: " + isAir);
        return isAir;
    }

    /**
     * 检查此任务是否与另一个任务相等
     *
     * @param other 要比较的其他任务
     * @return 如果任务相等则返回True，否则返回false
     */
    @Override
    protected boolean isEqual(Task other) {
        boolean isSame = false;

        // 检查其他任务是否是DestroyBlockTask的实例
        if (other instanceof DestroyBlockTask destroyBlockTask) {

            // 检查任务的位置是否相等
            if (destroyBlockTask.pos.equals(pos)) {
                isSame = true;
            }
        }

        Debug.logInternal("isEqual结果: " + isSame);

        return isSame;
    }

    /**
     * 生成表示方块破坏位置的调试字符串
     *
     * @return 调试字符串
     */
    @Override
    protected String toDebugString() {
        return "在 " + pos.toShortString() + " 处破坏方块";
    }
}
