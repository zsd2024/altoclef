package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.baritone.GoalAnd;
import adris.altoclef.util.baritone.GoalBlockSide;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalTwoBlocks;
import baritone.api.process.ICustomGoalProcess;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.*;
import adris.altoclef.multiversion.versionedfields.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.Objects;
import java.util.Optional;

/**
 * 在方块上的特定（或任意）面上进行左键或右键单击。
 */
public class InteractWithBlockTask extends Task {
    // 移动进度检查器
    private final MovementProgressChecker moveChecker = new MovementProgressChecker();
    // 卡住检查器
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
    // 要使用的物品
    private final ItemTarget toUse;
    // 方向
    private final Direction direction;
    // 目标方块位置
    private final BlockPos target;
    // 是否进入方块
    private final boolean walkInto;
    // 交互偏移量
    private final Vec3i interactOffset;
    // 交互输入
    private final Input interactInput;
    // 是否Shift点击
    private final boolean shiftClick;
    // 点击计时器
    private final TimerGame clickTimer = new TimerGame(5);
    // 徘徊任务
    private final TimeoutWanderTask wanderTask = new TimeoutWanderTask(5, true);
    // 烦人的方块列表（可能阻碍移动的方块）
    Block[] annoyingBlocks = new Block[]{
            Blocks.VINE, // 藤蔓
            Blocks.NETHER_SPROUTS, // 下界芽
            Blocks.CAVE_VINES, // 洞穴藤蔓
            Blocks.CAVE_VINES_PLANT, // 洞穴藤蔓植物
            Blocks.TWISTING_VINES, // 缠绕藤蔓
            Blocks.TWISTING_VINES_PLANT, // 缠绕藤蔓植物
            Blocks.WEEPING_VINES_PLANT, // 垂泪藤蔓植物
            Blocks.LADDER, // 梯子
            Blocks.BIG_DRIPLEAF, // 大型垂滴叶
            Blocks.BIG_DRIPLEAF_STEM, // 大型垂滴叶茎
            Blocks.SMALL_DRIPLEAF, // 小型垂滴叶
            Blocks.TALL_GRASS, // 高草
            Blocks.SHORT_GRASS, // 矮草
            Blocks.SWEET_BERRY_BUSH // 甜浆果丛
    };
    // 解困任务
    private Task unstuckTask = null;
    // 缓存的点击状态
    private ClickResponse cachedClickStatus = ClickResponse.CANT_REACH;
    // 等待点击的ticks
    private int waitingForClickTicks = 0;

    /**
     * 构造函数，指定要使用的物品、方向、目标方块、交互输入、是否进入方块、交互偏移量和是否Shift点击
     * @param toUse 要使用的物品目标
     * @param direction 方向
     * @param target 目标方块位置
     * @param interactInput 交互输入
     * @param walkInto 是否进入方块
     * @param interactOffset 交互偏移量
     * @param shiftClick 是否Shift点击
     */
    public InteractWithBlockTask(ItemTarget toUse, Direction direction, BlockPos target, Input interactInput, boolean walkInto, Vec3i interactOffset, boolean shiftClick) {
        this.toUse = toUse;
        this.direction = direction;
        this.target = target;
        this.interactInput = interactInput;
        this.walkInto = walkInto;
        this.interactOffset = interactOffset;
        this.shiftClick = shiftClick;
    }

    /**
     * 构造函数，指定要使用的物品、方向、目标方块、交互输入、是否进入方块和是否Shift点击
     * @param toUse 要使用的物品目标
     * @param direction 方向
     * @param target 目标方块位置
     * @param interactInput 交互输入
     * @param walkInto 是否进入方块
     * @param shiftClick 是否Shift点击
     */
    public InteractWithBlockTask(ItemTarget toUse, Direction direction, BlockPos target, Input interactInput, boolean walkInto, boolean shiftClick) {
        this(toUse, direction, target, interactInput, walkInto, Vec3i.ZERO, shiftClick);
    }

    /**
     * 构造函数，指定要使用的物品、方向、目标方块和是否进入方块
     * @param toUse 要使用的物品目标
     * @param direction 方向
     * @param target 目标方块位置
     * @param walkInto 是否进入方块
     */
    public InteractWithBlockTask(ItemTarget toUse, Direction direction, BlockPos target, boolean walkInto) {
        this(toUse, direction, target, Input.CLICK_RIGHT, walkInto, true);
    }

    /**
     * 构造函数，指定要使用的物品、目标方块、是否进入方块和交互偏移量
     * @param toUse 要使用的物品目标
     * @param target 目标方块位置
     * @param walkInto 是否进入方块
     * @param interactOffset 交互偏移量
     */
    public InteractWithBlockTask(ItemTarget toUse, BlockPos target, boolean walkInto, Vec3i interactOffset) {
        // null表示任意面都可以
        this(toUse, null, target, Input.CLICK_RIGHT, walkInto, interactOffset, true);
    }

    /**
     * 构造函数，指定要使用的物品、目标方块和是否进入方块
     * @param toUse 要使用的物品目标
     * @param target 目标方块位置
     * @param walkInto 是否进入方块
     */
    public InteractWithBlockTask(ItemTarget toUse, BlockPos target, boolean walkInto) {
        this(toUse, target, walkInto, Vec3i.ZERO);
    }

    /**
     * 构造函数，指定要使用的物品和目标方块
     * @param toUse 要使用的物品目标
     * @param target 目标方块位置
     */
    public InteractWithBlockTask(ItemTarget toUse, BlockPos target) {
        this(toUse, target, false);
    }

    /**
     * 构造函数，指定要使用的物品、方向、目标方块、交互输入、是否进入方块、交互偏移量和是否Shift点击
     * @param toUse 要使用的物品
     * @param direction 方向
     * @param target 目标方块位置
     * @param interactInput 交互输入
     * @param walkInto 是否进入方块
     * @param interactOffset 交互偏移量
     * @param shiftClick 是否Shift点击
     */
    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target, Input interactInput, boolean walkInto, Vec3i interactOffset, boolean shiftClick) {
        this(new ItemTarget(toUse, 1), direction, target, interactInput, walkInto, interactOffset, shiftClick);
    }

    /**
     * 构造函数，指定要使用的物品、方向、目标方块、交互输入、是否进入方块和是否Shift点击
     * @param toUse 要使用的物品
     * @param direction 方向
     * @param target 目标方块位置
     * @param interactInput 交互输入
     * @param walkInto 是否进入方块
     * @param shiftClick 是否Shift点击
     */
    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target, Input interactInput, boolean walkInto, boolean shiftClick) {
        this(new ItemTarget(toUse, 1), direction, target, interactInput, walkInto, shiftClick);
    }

    /**
     * 构造函数，指定要使用的物品、方向、目标方块和是否进入方块
     * @param toUse 要使用的物品
     * @param direction 方向
     * @param target 目标方块位置
     * @param walkInto 是否进入方块
     */
    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target, boolean walkInto) {
        this(new ItemTarget(toUse, 1), direction, target, walkInto);
    }

    /**
     * 构造函数，指定要使用的物品、方向和目标方块
     * @param toUse 要使用的物品
     * @param direction 方向
     * @param target 目标方块位置
     */
    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target) {
        this(new ItemTarget(toUse, 1), direction, target, Input.CLICK_RIGHT, false, false);
    }

    /**
     * 构造函数，指定要使用的物品、目标方块、是否进入方块和交互偏移量
     * @param toUse 要使用的物品
     * @param target 目标方块位置
     * @param walkInto 是否进入方块
     * @param interactOffset 交互偏移量
     */
    public InteractWithBlockTask(Item toUse, BlockPos target, boolean walkInto, Vec3i interactOffset) {
        this(new ItemTarget(toUse, 1), target, walkInto, interactOffset);
    }

    /**
     * 构造函数，指定要使用的物品、方向、目标方块和交互偏移量
     * @param toUse 要使用的物品
     * @param direction 方向
     * @param target 目标方块位置
     * @param interactOffset 交互偏移量
     */
    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target, Vec3i interactOffset) {
        this(new ItemTarget(toUse, 1), direction, target, Input.CLICK_RIGHT, false, interactOffset, false);
    }

    /**
     * 构造函数，指定要使用的物品、目标方块和交互偏移量
     * @param toUse 要使用的物品
     * @param target 目标方块位置
     * @param interactOffset 交互偏移量
     */
    public InteractWithBlockTask(Item toUse, BlockPos target, Vec3i interactOffset) {
        this(new ItemTarget(toUse, 1), null, target, Input.CLICK_RIGHT, false, interactOffset, false);
    }

    /**
     * 构造函数，指定要使用的物品、目标方块和是否进入方块
     * @param toUse 要使用的物品
     * @param target 目标方块位置
     * @param walkInto 是否进入方块
     */
    public InteractWithBlockTask(Item toUse, BlockPos target, boolean walkInto) {
        this(new ItemTarget(toUse, 1), target, walkInto);
    }

    /**
     * 构造函数，指定要使用的物品和目标方块
     * @param toUse 要使用的物品
     * @param target 目标方块位置
     */
    public InteractWithBlockTask(Item toUse, BlockPos target) {
        this(new ItemTarget(toUse, 1), target);
    }

    /**
     * 构造函数，指定目标方块和是否Shift点击
     * @param target 目标方块位置
     * @param shiftClick 是否Shift点击
     */
    public InteractWithBlockTask(BlockPos target, boolean shiftClick) {
        this(ItemTarget.EMPTY, null, target, Input.CLICK_RIGHT, false, shiftClick);
    }

    /**
     * 构造函数，指定目标方块
     * @param target 目标方块位置
     */
    public InteractWithBlockTask(BlockPos target) {
        this(ItemTarget.EMPTY, null, target, Input.CLICK_RIGHT, false, false);
    }

    /**
     * 生成方块周围所有侧面的位置
     * @param pos 中心方块位置
     * @return 返回周围侧面的方块位置数组
     */
    private static BlockPos[] generateSides(BlockPos pos) {
        return new BlockPos[]{
                pos.add(1,0,0),  // 东面
                pos.add(-1,0,0), // 西面
                pos.add(0,0,1),  // 南面
                pos.add(0,0,-1), // 北面
                pos.add(1,0,-1), // 东北面
                pos.add(1,0,1),  // 东南面
                pos.add(-1,0,-1),// 西北面
                pos.add(-1,0,1)  // 西南面
        };
    }

    /**
     * 创建用于交互的目标
     * @param target 目标方块位置
     * @param reachDistance 到达距离
     * @param interactSide 交互面
     * @param interactOffset 交互偏移量
     * @param walkInto 是否进入方块
     * @return 返回交互目标
     */
    private static Goal createGoalForInteract(BlockPos target, int reachDistance, Direction interactSide, Vec3i interactOffset, boolean walkInto) {

        boolean sideMatters = interactSide != null;
        if (sideMatters) {
            Vec3i offs = interactSide.getVector();
            if (offs.getY() == -1) {
                // 如果我们在下方，将自己放置在方块下方两格。
                offs = offs.down();
            }
            target = target.add(offs);
        }

        if (walkInto) {
            return new GoalTwoBlocks(target);
        } else {
            if (sideMatters) {
                // 确保我们在方块的正确一面。
                Goal sideGoal = new GoalBlockSide(target, interactSide, 1);
                return new GoalAnd(sideGoal, new GoalNear(target.add(interactOffset), reachDistance));
            } else {
                // TODO: 更简洁的方法来选择从哪一面接近。这仅用于岩浆相关的内容。
                return new GoalTwoBlocks(target.up());
                //return new GoalNear(target.add(interactOffset), reachDistance);
            }
        }
    }

    /**
     * 检查方块是否令人烦恼（可能阻碍移动）
     * @param mod AltoClef实例
     * @param pos 方块位置
     * @return 如果方块令人烦恼返回true
     */
    private boolean isAnnoying(AltoClef mod, BlockPos pos) {
        if (annoyingBlocks != null) {
            for (Block AnnoyingBlocks : annoyingBlocks) {
                return mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks ||
                        mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock ||          // 门
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock ||         // 栅栏
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock ||     // 栅栏门
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock;          // 花
            }
        }
        return false;
    }

    // 这在矿井和沼泽/丛林中经常发生
    /**
     * 检查是否卡在方块中
     * @param mod AltoClef实例
     * @return 返回卡住的方块位置，如果没有则返回null
     */
    private BlockPos stuckInBlock(AltoClef mod) {
        BlockPos p = mod.getPlayer().getBlockPos();
        if (isAnnoying(mod, p)) return p;
        if (isAnnoying(mod, p.up())) return p.up();
        BlockPos[] toCheck = generateSides(p);
        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        BlockPos[] toCheckHigh = generateSides(p.up());
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        return null;
    }

    /**
     * 获取栅栏解困任务
     * @return 返回解困任务
     */
    private Task getFenceUnstuckTask() {
        return new SafeRandomShimmyTask();
    }

    @Override
    protected void onStart() {
        AltoClef.getInstance().getClientBaritone().getPathingBehavior().forceCancel();

        moveChecker.reset();
        stuckCheck.reset();
        wanderTask.resetWander();
        clickTimer.reset();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            moveChecker.reset();
        }
        if (WorldHelper.isInNetherPortal()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("从下界传送门出来");
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
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
        if (unstuckTask != null && unstuckTask.isActive() && !unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("从方块中解困。");
            stuckCheck.reset();
            // 停止其他任务，我们只需要摇摆移动
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return unstuckTask;
        }
        if (!moveChecker.check(mod) || !stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                unstuckTask = getFenceUnstuckTask();
                return unstuckTask;
            }
            stuckCheck.reset();
        }

        cachedClickStatus = ClickResponse.CANT_REACH;

        // 首先获取要使用的物品
        if (!ItemTarget.nullOrEmpty(toUse) && !StorageHelper.itemTargetsMet(mod, toUse)) {
            moveChecker.reset();
            clickTimer.reset();
            return TaskCatalogue.getItemTask(toUse);
        }

        // 徘徊并检查
        if (wanderTask.isActive() && !wanderTask.isFinished()) {
            moveChecker.reset();
            clickTimer.reset();
            return wanderTask;
        }
        if (!moveChecker.check(mod)) {
            Debug.logMessage("失败，加入黑名单并徘徊。");
            mod.getBlockScanner().requestBlockUnreachable(target);
            return wanderTask;
        }

        int reachDistance = 0;
        Goal moveGoal = createGoalForInteract(target, reachDistance, direction, interactOffset, walkInto);
        ICustomGoalProcess proc = mod.getClientBaritone().getCustomGoalProcess();

        cachedClickStatus = rightClick(mod);
        switch (Objects.requireNonNull(cachedClickStatus)) {
            case CANT_REACH -> {
                setDebugState("前往目标");
                // 前往目标
                if (!proc.isActive()) {
                    proc.setGoalAndPath(moveGoal);
                }
                clickTimer.reset();
            }
            case WAIT_FOR_CLICK -> {
                setDebugState("等待点击");
                if (proc.isActive()) {
                    proc.onLostControl();
                }
                clickTimer.reset();

                // 尝试通过按shift键解困
                waitingForClickTicks++;
                if (waitingForClickTicks % 25 == 0 && shiftClick) {
                    mod.getInputControls().hold(Input.SNEAK);
                    mod.log("尝试按shift键");
                }

                if (waitingForClickTicks > 10*20) {
                    mod.log("尝试徘徊");
                    waitingForClickTicks = 0;
                    return wanderTask;
                }
            }
            case CLICK_ATTEMPTED -> {
                setDebugState("点击中。");
                if (proc.isActive()) {
                    proc.onLostControl();
                }
                if (clickTimer.elapsed()) {
                    // 我们尝试点击但失败了。
                    clickTimer.reset();
                    return wanderTask;
                }
            }
        }

        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef mod = AltoClef.getInstance();

        mod.getClientBaritone().getPathingBehavior().forceCancel();
        mod.getInputControls().release(Input.SNEAK);
    }

    @Override
    public boolean isFinished() {
        return false;
        //return _trying && !proc(mod).isActive();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof InteractWithBlockTask task) {
            if ((task.direction == null) != (direction == null)) return false;
            if (task.direction != null && !task.direction.equals(direction)) return false;
            if ((task.toUse == null) != (toUse == null)) return false;
            if (task.toUse != null && !task.toUse.equals(toUse)) return false;
            if (!task.target.equals(target)) return false;
            if (!task.interactInput.equals(interactInput)) return false;
            return task.walkInto == walkInto;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "使用 " + toUse + " 与 " + target + " 方向 " + direction + " 进行交互";
    }

    /**
     * 获取点击状态
     * @return 返回点击响应状态
     */
    public ClickResponse getClickStatus() {
        return cachedClickStatus;
    }

    /**
     * 执行右键点击操作
     * @param mod AltoClef实例
     * @return 返回点击响应
     */
    private ClickResponse rightClick(AltoClef mod) {

        // 如果baritone不能交互则不进行交互。
        if (mod.getExtraBaritoneSettings().isInteractionPaused() || mod.getFoodChain().needsToEat() ||
                mod.getPlayer().isBlocking())
            return ClickResponse.WAIT_FOR_CLICK;

        // 屏幕打开时无法交互。
        if (!StorageHelper.isPlayerInventoryOpen()) {
            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
            if (!cursorStack.isEmpty()) {
                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                if (moveTo.isPresent()) {
                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                    return ClickResponse.WAIT_FOR_CLICK;
                }
                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                    return ClickResponse.WAIT_FOR_CLICK;
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                // 如果光标槽是垃圾，尝试丢弃
                if (garbage.isPresent()) {
                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                    return ClickResponse.WAIT_FOR_CLICK;
                }
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                return ClickResponse.WAIT_FOR_CLICK;
            } else {
                StorageHelper.closeScreen();
            }
        }

        Optional<Rotation> reachable = getCurrentReach();
        if (reachable.isPresent()) {
            if (LookHelper.isLookingAt(mod, target)) {
                if (toUse != null) {
                    mod.getSlotHandler().forceEquipItem(toUse, false);
                } else {
                    mod.getSlotHandler().forceDeequipRightClickableItem();
                }
                mod.getInputControls().tryPress(interactInput);
                if (mod.getInputControls().isHeldDown(interactInput)) {
                    if (shiftClick) {
                        mod.getInputControls().hold(Input.SNEAK);
                    }
                    return ClickResponse.CLICK_ATTEMPTED;
                }
                //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(_interactInput, true);
            } else {
                LookHelper.lookAt(reachable.get());
            }
            return ClickResponse.WAIT_FOR_CLICK;
        }
        if (shiftClick) {
            mod.getInputControls().release(Input.SNEAK);
        }
        return ClickResponse.CANT_REACH;
    }

    /**
     * 获取当前可到达的角度
     * @return 返回可到达的旋转角度
     */
    public Optional<Rotation> getCurrentReach() {
        return LookHelper.getReach(target, direction);
    }

    /**
     * 点击响应枚举
     */
    public enum ClickResponse {
        // 无法到达
        CANT_REACH,
        // 等待点击
        WAIT_FOR_CLICK,
        // 点击已尝试
        CLICK_ATTEMPTED
    }
}
