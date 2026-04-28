package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.GotoTarget;
import adris.altoclef.tasks.movement.FollowPlayerTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 给玩家物品任务 - 收集指定物品并给予指定玩家
 */
public class GiveItemToPlayerTask extends Task {

    /** 目标玩家名称 */
    private final String playerName;
    /** 需要给予的物品目标数组 */
    private final ItemTarget[] targets;

    /** 资源收集任务 */
    private final CataloguedResourceTask resourceTask;
    /** 需要投掷的物品目标列表 */
    private final List<ItemTarget> throwTarget = new ArrayList<>();
    /** 目标位置 */
    private Vec3d targetPos;
    /** 是否正在投掷物品 */
    private boolean droppingItems;
    /** 是否已到达目标位置 */
    private boolean atGoal;
    /** 目标坐标（可选） */
    private GotoTarget cords = null;

    /** 投掷任务 */
    private Task throwTask;

    /**
     * 构造函数（使用玩家最后已知位置）
     * @param player 目标玩家名称
     * @param targets 需要给予的物品目标
     */
    public GiveItemToPlayerTask(String player, ItemTarget... targets) {
        playerName = player;
        this.targets = targets;
        resourceTask = TaskCatalogue.getSquashedItemTask(targets);
    }

    /**
     * 构造函数（指定目标坐标）
     * @param player 目标玩家名称
     * @param gotocords 目标坐标
     * @param targets 需要给予的物品目标
     */
    public GiveItemToPlayerTask(String player, GotoTarget gotocords, ItemTarget... targets) {
        playerName = player;
        this.targets = targets;
        this.cords = gotocords;
        resourceTask = TaskCatalogue.getSquashedItemTask(targets);
    }

    @Override
    protected void onStart() {
        // 初始化状态
        droppingItems = false;
        atGoal = false;
        throwTarget.clear();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果正在执行投掷任务，则返回该任务
        if (throwTask != null && throwTask.isActive() && !throwTask.isFinished()) {
            setDebugState("正在投掷物品");
            return throwTask;
        }

        // 如果未指定坐标，使用玩家最后已知位置
        if (cords == null) {
            Optional<Vec3d> lastPos = mod.getEntityTracker().getPlayerMostRecentPosition(playerName);
            if (lastPos.isEmpty()) {
                setDebugState("未找到/检测到玩家。在玩家进入渲染距离前不做任何操作。");
                return null;
            }
            targetPos = lastPos.get().add(0, 1f, 0);
        }


        // 如果尚未收集到所有目标物品，则执行资源收集任务
        if (!StorageHelper.itemTargetsMet(mod, targets)) {
            setDebugState("正在收集资源...");
            return resourceTask;
        }

        // 如果指定了坐标
        if (cords != null) {
            if (!atGoal) {
                atGoal = true;
                // 移动到指定坐标
                return new GetToBlockTask(new BlockPos(cords.getX(), cords.getY(), cords.getZ()), cords.getDimension());
            }
            Optional<Vec3d> lastPos = mod.getEntityTracker().getPlayerMostRecentPosition(playerName);
            if (lastPos.isEmpty()) {
                setDebugState("未找到/检测到玩家。在玩家进入渲染距离前不做任何操作。");
                return null;
            }
            targetPos = lastPos.get().add(0, 2f, 0);
        }

        // 如果正在投掷物品
        if (droppingItems) {
            // 对每个目标物品，拿起其堆叠并投掷
            for (ItemTarget target : throwTarget) {
                if (target.getTargetCount() <= 0) continue;

                // 查找包含该物品的槽位
                Optional<Slot> maybeSlot = mod.getItemStorage()
                        .getSlotsWithItemPlayerInventory(false, target.getMatches())
                        .stream()
                        .findFirst();

                if (maybeSlot.isEmpty()) continue;
                Slot slot = maybeSlot.get();

                // 每次操作时看向目标位置并设置调试状态
                setDebugState("正在投掷物品");
                LookHelper.lookAt(mod, targetPos);

                // 投掷整个物品堆叠
                mod.getSlotHandler().clickSlot(slot, 1, SlotActionType.THROW);
            }
            mod.log("已完成给予物品。");
            stop();
            return null;
        }

        // 如果已接近目标玩家
        if (targetPos.isInRange(mod.getPlayer().getPos(), 4)) {
            if (!mod.getEntityTracker().isPlayerLoaded(playerName)) {
                mod.logWarning("无法到达玩家\"" + playerName + "\"。我们移动到了上次看到他们的位置，但现在不知道他们在哪里。");
                stop();
                return null;
            }
            droppingItems = true;
            throwTarget.addAll(Arrays.asList(targets));
        }

        setDebugState("正在前往玩家...");
        return new FollowPlayerTask(playerName);
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 停止时无需特殊处理
    }

    @Override
    protected boolean isEqual(Task other) {
        // 比较两个任务是否相等（基于玩家名称和物品目标）
        if (other instanceof GiveItemToPlayerTask task) {
            if (!task.playerName.equals(playerName)) return false;
            return Arrays.equals(task.targets, targets);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "给予物品给 " + playerName;
    }
}