package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.eventbus.events.GameOverlayEvent;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.ArrayUtils;

/**
 * 放置床并设置重生点任务
 * 此任务负责在主世界中放置床并设置重生点，或使用已存在的床
 */
public class PlaceBedAndSetSpawnTask extends Task {

    /**
     * 区域扫描计时器（9秒）
     */
    private final TimerGame regionScanTimer = new TimerGame(9);
    /**
     * 床需要清理的区域大小（3x2x3）
     */
    private final Vec3i BED_CLEAR_SIZE = new Vec3i(3, 2, 3);
    /**
     * 床底部平台的位置偏移
     */
    private final Vec3i[] BED_BOTTOM_PLATFORM = new Vec3i[]{
            new Vec3i(0, -1, 0),
            new Vec3i(1, -1, 0),
            new Vec3i(2, -1, 0),
            new Vec3i(0, -1, -1),
            new Vec3i(1, -1, -1),
            new Vec3i(2, -1, -1),
            new Vec3i(0, -1, 1),
            new Vec3i(1, -1, 1),
            new Vec3i(2, -1, 1)
    };
    // 有点傻，但谁知道我们是否想改变它。
    /**
     * 放置床时玩家站立的位置偏移
     */
    private final Vec3i BED_PLACE_STAND_POS = new Vec3i(0, 0, 1);
    /**
     * 放置床的位置偏移
     */
    private final Vec3i BED_PLACE_POS = new Vec3i(1, 0, 1);
    /**
     * 床放置位置的偏移数组，用于检测已放置的床
     */
    private final Vec3i[] BED_PLACE_POS_OFFSET = new Vec3i[]{
            BED_PLACE_POS,
            BED_PLACE_POS.north(),
            BED_PLACE_POS.south(),
            BED_PLACE_POS.east(),
            BED_PLACE_POS.west(),
            BED_PLACE_POS.add(-1,0,1),
            BED_PLACE_POS.add(1,0,1),
            BED_PLACE_POS.add(-1,0,-1),
            BED_PLACE_POS.add(1,0,-1),
            BED_PLACE_POS.north(2),
            BED_PLACE_POS.south(2),
            BED_PLACE_POS.east(2),
            BED_PLACE_POS.west(2),
            BED_PLACE_POS.add(-2,0,1),
            BED_PLACE_POS.add(-2,0,2),
            BED_PLACE_POS.add(2,0,1),
            BED_PLACE_POS.add(2,0,2),
            BED_PLACE_POS.add(-2,0,-1),
            BED_PLACE_POS.add(-2,0,-2),
            BED_PLACE_POS.add(2,0,-1),
            BED_PLACE_POS.add(2,0,-2)
    };
    /**
     * 放置床的方向（向上）
     */
    private final Direction BED_PLACE_DIRECTION = Direction.UP;
    /**
     * 床交互超时计时器（5秒）
     */
    private final TimerGame bedInteractTimeout = new TimerGame(5);
    /**
     * 在床中计时器（1秒）
     */
    private final TimerGame inBedTimer = new TimerGame(1);
    /**
     * 移动进度检查器
     */
    private final MovementProgressChecker progressChecker = new MovementProgressChecker();
    /**
     * 是否留在床中
     */
    private boolean stayInBed;
    /**
     * 当前床区域位置
     */
    private BlockPos currentBedRegion;
    /**
     * 当前需要放置结构的位置
     */
    private BlockPos currentStructure;
    /**
     * 当前需要破坏的位置
     */
    private BlockPos currentBreak;
    /**
     * 重生点是否已设置
     */
    private boolean spawnSet;
    /**
     * 重生点设置消息检查订阅
     */
    private Subscription<ChatMessageEvent> respawnPointSetMessageCheck;
    /**
     * 重生失败消息检查订阅
     */
    private Subscription<GameOverlayEvent> respawnFailureMessageCheck;
    /**
     * 是否已尝试睡觉
     */
    private boolean sleepAttemptMade;
    /**
     * 之前是否在睡觉
     */
    private boolean wasSleeping;
    /**
     * 用于设置重生点的床位置
     */
    private BlockPos bedForSpawnPoint;

    /**
     * 构造函数
     */
    public PlaceBedAndSetSpawnTask() {

    }

    /**
     * 设置留在床中的标志
     *
     * @return 当前PlaceBedAndSetSpawnTask实例
     */
    public PlaceBedAndSetSpawnTask stayInBed() {
        // 记录方法调用
        Debug.logInternal("Stay in bed method called");

        // 将_stayInBed标志设置为true
        this.stayInBed = true;
        Debug.logInternal("Setting _stayInBed to true");

        // 返回当前实例
        return this;
    }

    /**
     * 任务开始时调用的方法
     * 初始化各种变量并设置模组行为
     */
    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        // 保存当前行为设置
        mod.getBehaviour().push();

        // 重置进度检查器
        progressChecker.reset();

        // 重置当前床区域
        currentBedRegion = null;

        // 避免在床附近放置方块
        mod.getBehaviour().avoidBlockPlacing(pos -> {
            if (currentBedRegion != null) {
                BlockPos start = currentBedRegion;
                BlockPos end = currentBedRegion.add(BED_CLEAR_SIZE);
                return start.getX() <= pos.getX() && pos.getX() < end.getX()
                        && start.getZ() <= pos.getZ() && pos.getZ() < end.getZ()
                        && start.getY() <= pos.getY() && pos.getY() < end.getY();
            }
            return false;
        });

        // 避免在床附近破坏方块
        mod.getBehaviour().avoidBlockBreaking(pos -> {
            if (currentBedRegion != null) {
                for (Vec3i baseOffs : BED_BOTTOM_PLATFORM) {
                    BlockPos base = currentBedRegion.add(baseOffs);
                    if (base.equals(pos)) return true;
                }
            }
            // 永远不要破坏床。如果存在床，我们会睡在上面。
            if (mod.getWorld() != null) {
                return mod.getWorld().getBlockState(pos).getBlock() instanceof BedBlock;
            }
            return false;
        });

        // 重置睡眠处理变量
        spawnSet = false;
        sleepAttemptMade = false;
        wasSleeping = false;

        // 订阅重生点设置消息事件
        respawnPointSetMessageCheck = EventBus.subscribe(ChatMessageEvent.class, evt -> {
            String msg = evt.toString();
            if (msg.contains("Respawn point set")) {
                spawnSet = true;
                inBedTimer.reset();
            }
        });

        // 订阅重生失败消息事件
        respawnFailureMessageCheck = EventBus.subscribe(GameOverlayEvent.class, evt -> {
            final String[] NEUTRAL_MESSAGES = new String[]{
                    "You can sleep only at night",
                    "You can only sleep at night",
                    "You may not rest now; there are monsters nearby"
            };
            for (String checkMessage : NEUTRAL_MESSAGES) {
                if (evt.message.contains(checkMessage)) {
                    if (!sleepAttemptMade) {
                        bedInteractTimeout.reset();
                    }
                    sleepAttemptMade = true;
                }
            }
        });

        // 调试日志
        Debug.logInternal("Started onStart() method");
        Debug.logInternal("Current bed region: " + currentBedRegion);
        Debug.logInternal("Spawn set: " + spawnSet);
    }

    /**
     * 重置睡眠状态
     */
    public void resetSleep() {
        spawnSet = false;
        sleepAttemptMade = false;
        wasSleeping = false;
    }

    @Override
    protected Task onTick() {
        // 概要：
        // 如果我们在附近找到一张床，就睡在上面。
        // 否则，放置床：
        //      如果没有床，先收集一张床。
        //      找到一个3x2x1的区域并清理它
        //      站在长边（3）的边缘
        //      放置在中间方块上，可靠地放置床。
        AltoClef mod = AltoClef.getInstance();

        if (!progressChecker.check(mod) && currentBedRegion != null) {
            progressChecker.reset();
            Debug.logMessage("正在搜索新的床区域。");
            currentBedRegion = null;
        }
        if (WorldHelper.isInNetherPortal()) {
            setDebugState("我们在下界传送门中。随机游荡");
            currentBedRegion = null;
            return new TimeoutWanderTask();
        }
        // 我们只能在主世界执行此操作。
        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            setDebugState("先去主世界。");
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof SleepingChatScreen) {
            progressChecker.reset();
            setDebugState("睡觉中...");
            wasSleeping = true;
            //Debug.logMessage("Closing sleeping thing");
            spawnSet = true;
            return null;
        }

        if (sleepAttemptMade) {
            if (bedInteractTimeout.elapsed()) {
                Debug.logMessage("未能获取\"重生点已设置\"消息或进入睡眠状态，假设此床已包含我们的重生点。");
                spawnSet = true;
                return null;
            }
        }
        if (mod.getBlockScanner().anyFound(blockPos -> (WorldHelper.canReach(blockPos) &&
                blockPos.isWithinDistance(mod.getPlayer().getPos(), 40) &&
                mod.getItemStorage().hasItem(ItemHelper.BED)) || (WorldHelper.canReach(blockPos) &&
                !mod.getItemStorage().hasItem(ItemHelper.BED)), ItemHelper.itemsToBlocks(ItemHelper.BED))) {
            // 睡在最近的床上
            setDebugState("去床上睡觉...");
            return new DoToClosestBlockTask(toSleepIn -> {
                boolean closeEnough = toSleepIn.isWithinDistance(mod.getPlayer().getPos(), 3);
                if (closeEnough) {
                    // 为什么是0.2？我累了。
                    Vec3d centerBed = new Vec3d(toSleepIn.getX() + 0.5, toSleepIn.getY() + 0.2, toSleepIn.getZ() + 0.5);
                    BlockHitResult hit = LookHelper.raycast(mod.getPlayer(), centerBed, 6);
                    // TODO: 有点丑，但我累了，为了第二次尝试速通，稍后再修复这个代码块
                    closeEnough = false;
                    if (hit.getType() != HitResult.Type.MISS) {
                        // 在这一点上，如果我们错过了，我们可能已经足够接近了。
                        BlockPos p = hit.getBlockPos();
                        if (ArrayUtils.contains(ItemHelper.itemsToBlocks(ItemHelper.BED), mod.getWorld().getBlockState(p).getBlock())) {
                            // 我们有一张床！
                            closeEnough = true;
                        }
                    }
                }
                bedForSpawnPoint = WorldHelper.getBedHead(toSleepIn);
                if (bedForSpawnPoint == null) {
                    bedForSpawnPoint = toSleepIn;
                }
                if (!closeEnough) {
                    try {
                        Direction face = mod.getWorld().getBlockState(toSleepIn).get(BedBlock.FACING);
                        Direction side = face.rotateYClockwise();
                        /*
                        BlockPos targetMove = toSleepIn.offset(side).offset(side); // 两次，只是为了确保...
                         */
                        return new GetToBlockTask(bedForSpawnPoint.add(side.getVector()));
                    } catch (IllegalArgumentException e) {
                        // 如果床未加载，就会发生这种情况。在这种情况下，先到达床的位置。
                    }
                } else {
                    inBedTimer.reset();
                }
                if (closeEnough) {
                    inBedTimer.reset();
                }
                // 跟踪我们的重生点位置
                progressChecker.reset();
                return new InteractWithBlockTask(bedForSpawnPoint);
            }, ItemHelper.itemsToBlocks(ItemHelper.BED));
        }

        if (mod.getPlayer().isTouchingWater() && mod.getItemStorage().hasItem(ItemHelper.BED)) {
            setDebugState("我们在水中。随机游荡");
            currentBedRegion = null;
            return new TimeoutWanderTask();
        }

        if (currentBedRegion != null) {
            for (Vec3i BedPlacePos : BED_PLACE_POS_OFFSET) {
                Block getBlock = mod.getWorld().getBlockState(currentBedRegion.add(BedPlacePos)).getBlock();
                if (getBlock instanceof BedBlock) {
                    mod.getBlockScanner().addBlock(getBlock, currentBedRegion.add(BedPlacePos));
                    break;
                }
            }
        }
        // 如果没有床，先获取一张。
        if (!mod.getItemStorage().hasItem(ItemHelper.BED)) {
            setDebugState("先获取一张床");
            return TaskCatalogue.getItemTask("bed", 1);
        }

        if (currentBedRegion == null) {
            if (regionScanTimer.elapsed()) {
                Debug.logMessage("重新扫描附近的床放置位置...");
                regionScanTimer.reset();
                currentBedRegion = this.locateBedRegion(mod, mod.getPlayer().getBlockPos());
            }
        }
        if (currentBedRegion == null) {
            setDebugState("寻找放置床的位置，随机游荡...");
            return new TimeoutWanderTask();
        }

        // 清理并制作床的基础

        for (Vec3i baseOffs : BED_BOTTOM_PLATFORM) {
            BlockPos toPlace = currentBedRegion.add(baseOffs);
            if (!WorldHelper.isSolidBlock(toPlace)) {
                currentStructure = toPlace;
                break;
            }
        }

        outer:
        for (int dx = 0; dx < BED_CLEAR_SIZE.getX(); ++dx) {
            for (int dz = 0; dz < BED_CLEAR_SIZE.getZ(); ++dz) {
                for (int dy = 0; dy < BED_CLEAR_SIZE.getY(); ++dy) {
                    BlockPos toClear = currentBedRegion.add(dx,dy,dz);
                    if (WorldHelper.isSolidBlock(toClear)) {
                        currentBreak = toClear;
                        break outer;
                    }
                }
            }
        }

        if (currentStructure != null) {
            if (WorldHelper.isSolidBlock(currentStructure)) {
                currentStructure = null;
            } else {
                setDebugState("放置床的结构");
                return new PlaceStructureBlockTask(currentStructure);
            }
        }
        if (currentBreak != null) {
            if (!WorldHelper.isSolidBlock(currentBreak)) {
                currentBreak = null;
            } else {
                setDebugState("清理床的区域");
                return new DestroyBlockTask(currentBreak);
            }
        }

        BlockPos toStand = currentBedRegion.add(BED_PLACE_STAND_POS);
        // 我们的床区域已准备好放置
        if (!mod.getPlayer().getBlockPos().equals(toStand)) {
            return new GetToBlockTask(toStand);
        }

        BlockPos toPlace = currentBedRegion.add(BED_PLACE_POS);
        if (mod.getWorld().getBlockState(toPlace.offset(BED_PLACE_DIRECTION)).getBlock() instanceof BedBlock) {
            setDebugState("等待重新扫描+找到我们刚刚放置的床。应该几乎是瞬间完成的。");
            progressChecker.reset();
            return null;
        }
        setDebugState("放置床...");

        setDebugState("填充传送门");
        if (!progressChecker.check(mod)) {
            mod.getClientBaritone().getPathingBehavior().cancelEverything();
            mod.getClientBaritone().getPathingBehavior().forceCancel();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            progressChecker.reset();
        }

        // 如果我们尝试放置但失败了，向后移动
        if (thisOrChildSatisfies(task -> {
            if (task instanceof InteractWithBlockTask intr)
                return intr.getClickStatus() == InteractWithBlockTask.ClickResponse.CLICK_ATTEMPTED;
            return false;
        })) {
            mod.getInputControls().tryPress(Input.MOVE_BACK);
        }
        return new InteractWithBlockTask(new ItemTarget("bed", 1), BED_PLACE_DIRECTION, toPlace.offset(BED_PLACE_DIRECTION.getOpposite()), false);
    }

    /**
     * 任务被中断时调用的覆盖方法
     *
     * @param interruptTask 中断此任务的任务
     */
    @Override
    protected void onStop(Task interruptTask) {
        // 恢复行为堆栈
        AltoClef.getInstance().getBehaviour().pop();

        // 取消订阅重生点设置消息
        EventBus.unsubscribe(respawnPointSetMessageCheck);

        // 取消订阅重生失败消息
        EventBus.unsubscribe(respawnFailureMessageCheck);

        // 调试日志
        Debug.logInternal("停止跟踪床");
        Debug.logInternal("行为已恢复");
        Debug.logInternal("已取消订阅重生点设置消息");
        Debug.logInternal("已取消订阅重生失败消息");
    }

    /**
     * 检查给定任务是否与此任务相等
     *
     * @param other 要比较的任务
     * @return 如果任务相等则返回true，否则返回false
     */
    @Override
    protected boolean isEqual(Task other) {
        // 检查其他任务是否是PlaceBedAndSetSpawnTask的实例
        boolean isSameTask = (other instanceof PlaceBedAndSetSpawnTask);

        if (!isSameTask) {
            // 如果任务类型不同，记录调试消息
            Debug.logInternal("任务类型不同");
        }

        return isSameTask;
    }

    /**
     * 返回此方法执行的操作的字符串表示
     * 操作描述为"在附近放置床+重置重生点"
     *
     * @return 操作的字符串表示
     */
    @Override
    protected String toDebugString() {
        return "在附近放置床+重置重生点";
    }

    /**
     * 检查重生点/睡眠条件是否已完成
     *
     * @return 条件是否已完成
     */
    @Override
    public boolean isFinished() {
        // 检查是否在主世界
        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            Debug.logInternal("除非我们在主世界，否则无法在床中设置重生点/睡觉！");
            return true;
        }

        // 检查玩家是否在睡觉
        boolean isSleeping = AltoClef.getInstance().getPlayer().isSleeping();

        // 检查计时器是否已过期
        boolean timerElapsed = inBedTimer.elapsed();

        // 检查重生点是否已设置，玩家是否不在睡觉，且计时器是否已过期
        boolean isFinished = spawnSet && !isSleeping && timerElapsed;

        // 记录调试值
        Debug.logInternal("isSleeping: " + isSleeping);
        Debug.logInternal("timerElapsed: " + timerElapsed);
        Debug.logInternal("isFinished: " + isFinished);

        return isFinished;
    }

    /**
     * 返回玩家上次睡觉的床的位置
     *
     * @return 床的BlockPos
     */
    public BlockPos getBedSleptPos() {
        // 记录正在获取床的睡觉位置的调试消息
        Debug.logInternal("获取床的睡觉位置");

        // 返回存储的床位置
        return bedForSpawnPoint;
    }

    /**
     * 检查重生点是否已设置
     *
     * @return 如果重生点已设置则返回true，否则返回false
     */
    public boolean isSpawnSet() {
        // 记录内部调试消息
        Debug.logInternal("检查重生点是否已设置");

        // 返回_spawnSet变量的值
        return spawnSet;
    }

    /**
     * 在指定范围内从给定原点定位最近的良好位置
     *
     * @param mod    模组实例
     * @param origin 原点位置
     * @return 最近的良好位置
     */
    private BlockPos locateBedRegion(AltoClef mod, BlockPos origin) {
        final int SCAN_RANGE = 10;

        BlockPos closestGood = null;
        double closestDist = Double.POSITIVE_INFINITY;

        for (int x = origin.getX() - SCAN_RANGE; x < origin.getX() + SCAN_RANGE; ++x) {
            for (int z = origin.getZ() - SCAN_RANGE; z < origin.getZ() + SCAN_RANGE; ++z) {
                outer:
                for (int y = origin.getY() - SCAN_RANGE; y < origin.getY() + SCAN_RANGE; ++y) {
                    BlockPos attemptPos = new BlockPos(x, y, z);
                    double distance = BlockPosVer.getSquaredDistance(attemptPos,mod.getPlayer().getPos());

                    Debug.logInternal("检查位置: " + attemptPos);

                    if (distance > closestDist) {
                        Debug.logInternal("跳过位置: " + attemptPos);
                        continue;
                    }

                    if (isGoodPosition(mod, attemptPos)) {
                        Debug.logInternal("找到良好位置: " + attemptPos);
                        closestGood = attemptPos;
                        closestDist = distance;
                    }
                }
            }
        }

        return closestGood;
    }

    /**
     * 检查给定位置是否是良好位置
     * 如果位置周围特定区域内的所有方块都可以放置或清理，则认为该位置是良好的
     *
     * @param mod 模组实例
     * @param pos 要检查的位置
     * @return 如果位置良好则返回true，否则返回false
     */
    private boolean isGoodPosition(AltoClef mod, BlockPos pos) {
        final BlockPos BED_CLEAR_SIZE = new BlockPos(2, 1, 2);

        // 遍历位置周围的区域
        for (int x = 0; x < BED_CLEAR_SIZE.getX(); ++x) {
            for (int y = 0; y < BED_CLEAR_SIZE.getY(); ++y) {
                for (int z = 0; z < BED_CLEAR_SIZE.getZ(); ++z) {
                    BlockPos checkPos = pos.add(x,y,z);
                    if (!isGoodToPlaceInsideOrClear(mod, checkPos)) {
                        Debug.logInternal("不是良好位置: " + checkPos);
                        return false;
                    }
                }
            }
        }

        Debug.logInternal("良好位置");
        return true;
    }

    /**
     * 检查给定位置是否适合放置或清理
     *
     * @param mod 模组实例
     * @param pos 要检查的位置
     * @return 如果位置适合放置或清理则返回true，否则返回false
     */
    private boolean isGoodToPlaceInsideOrClear(AltoClef mod, BlockPos pos) {
        // 定义要检查位置周围的偏移量
        final Vec3i[] CHECK = {
                new Vec3i(0, 0, 0),
                new Vec3i(-1, 0, 0),
                new Vec3i(1, 0, 0),
                new Vec3i(0, 1, 0),
                new Vec3i(0, -1, 0),
                new Vec3i(0, 0, 1),
                new Vec3i(0, 0, -1)
        };

        // 检查每个偏移量
        for (Vec3i offset : CHECK) {
            BlockPos newPos = pos.add(offset);
            if (!isGoodAsBorder(mod, newPos)) {
                Debug.logInternal("不适合作为边界: " + newPos);
                return false;
            }
        }

        Debug.logInternal("适合放置或清理");
        return true;
    }

    /**
     * 检查方块是否适合作为边界方块
     *
     * @param mod 模组实例
     * @param pos 方块的位置
     * @return 如果方块可以用作边界则返回true，否则返回false
     */
    private boolean isGoodAsBorder(AltoClef mod, BlockPos pos) {
        // 检查方块是否为实心
        boolean isSolid = WorldHelper.isSolidBlock(pos);
        Debug.logInternal("isSolid: " + isSolid);

        if (isSolid) {
            // 检查方块是否可以被破坏
            boolean canBreak = WorldHelper.canBreak(pos);
            Debug.logInternal("canBreak: " + canBreak);
            return canBreak;
        } else {
            // 检查方块是否为空气
            boolean isAir = WorldHelper.isAir(pos);
            Debug.logInternal("isAir: " + isAir);
            return isAir;
        }
    }
}
