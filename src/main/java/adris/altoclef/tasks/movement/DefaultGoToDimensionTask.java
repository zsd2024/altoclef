package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalBucketTask;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 默认维度传送任务
 * <p>
 * 某些通用任务需要我们前往下界/主世界/末地。
 * <p>
 * 用户应该能够在设置中指定如何执行此操作
 * （例如，从头开始制作新传送门，或首先检查特定传送门区域，或使用高速公路等）
 */
public class DefaultGoToDimensionTask extends Task {

    // 目标维度
    private final Dimension _target;
    // 缓存的任务，用于在任务暂停/恢复时保持构建属性
    private final Task _cachedNetherBucketConstructionTask = new ConstructNetherPortalBucketTask();

    /**
     * 构造函数
     * @param target 目标维度
     */
    public DefaultGoToDimensionTask(Dimension target) {
        _target = target;
    }

    @Override
    protected void onStart() {
        // 任务开始时无需特殊处理
    }

    @Override
    protected Task onTick() {
        // 如果已经在目标维度，任务完成
        if (WorldHelper.getCurrentDimension() == _target) return null;

        // 根据目标维度和当前维度选择适当的传送策略
        switch (_target) {
            case OVERWORLD:
                switch (WorldHelper.getCurrentDimension()) {
                    case NETHER:
                        return goToOverworldFromNetherTask();
                    case END:
                        return goToOverworldFromEndTask();
                }
                break;
            case NETHER:
                switch (WorldHelper.getCurrentDimension()) {
                    case OVERWORLD:
                        return goToNetherFromOverworldTask();
                    case END:
                        // 首先前往主世界
                        return goToOverworldFromEndTask();
                }
                break;
            case END:
                switch (WorldHelper.getCurrentDimension()) {
                    case NETHER:
                        // 首先前往主世界
                        return goToOverworldFromNetherTask();
                    case OVERWORLD:
                        return goToEndTask();
                }
                break;
        }

        setDebugState(WorldHelper.getCurrentDimension() + " -> " + _target + " 尚未实现！");
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时无需特殊处理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof DefaultGoToDimensionTask task) {
            return task._target == _target;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "前往维度: " + _target + " (默认版本)";
    }

    @Override
    public boolean isFinished() {
        return WorldHelper.getCurrentDimension() == _target;
    }

    /**
     * 从下界前往主世界的任务
     * @return 适当的传送任务
     */
    private Task goToOverworldFromNetherTask() {
        AltoClef mod = AltoClef.getInstance();

        // 如果附近有下界传送门，直接使用
        if (netherPortalIsClose(mod)) {
            setDebugState("前往下界传送门");
            return new EnterNetherPortalTask(Dimension.NETHER);
        }

        // 否则尝试使用上次使用的传送门位置
        Optional<BlockPos> closest = mod.getMiscBlockTracker().getLastUsedNetherPortal(Dimension.NETHER);
        if (closest.isPresent()) {
            setDebugState("前往上次使用的下界传送门位置");
            return new GetToBlockTask(closest.get());
        }

        // 最后选择构建黑曜石传送门
        setDebugState("使用黑曜石构建下界传送门");
        return new ConstructNetherPortalObsidianTask();
    }

    /**
     * 从末地前往主世界的任务（未实现）
     * @return null（占位符）
     */
    private Task goToOverworldFromEndTask() {
        setDebugState("待办：前往中心传送门（位于0,0）。如果不存在，则击杀末影龙");
        return null;
    }

    /**
     * 从主世界前往下界的任务
     * @return 适当的传送任务
     */
    private Task goToNetherFromOverworldTask() {
        AltoClef mod = AltoClef.getInstance();

        // 如果附近有下界传送门，直接使用
        if (netherPortalIsClose(mod)) {
            setDebugState("前往下界传送门");
            return new EnterNetherPortalTask(Dimension.NETHER);
        }
        // 根据设置选择行为：构建传送门或前往基地
        return switch (mod.getModSettings().getOverworldToNetherBehaviour()) {
            case BUILD_PORTAL_VANILLA -> _cachedNetherBucketConstructionTask;
            case GO_TO_HOME_BASE -> new GetToBlockTask(mod.getModSettings().getHomeBasePosition());
        };
    }

    /**
     * 前往末地的任务（未实现）
     * @return null（占位符）
     */
    private Task goToEndTask() {
        // 请注意，前往末地需要先去下界
        setDebugState("待办：前往末地，与BeatMinecraft相同");
        return null;
    }

    /**
     * 检查附近是否有下界传送门
     * @param mod AltoClef实例
     * @return 如果2000格范围内有下界传送门则返回true
     */
    private boolean netherPortalIsClose(AltoClef mod) {
        if (mod.getBlockScanner().anyFound(Blocks.NETHER_PORTAL)) {
            Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock( Blocks.NETHER_PORTAL);
            return closest.isPresent() && closest.get().isWithinDistance(mod.getPlayer().getPos(), 2000);
        }
        return false;
    }

    /**
     * 主世界到下界的行为枚举
     */
    public enum OVERWORLD_TO_NETHER_BEHAVIOUR {
        BUILD_PORTAL_VANILLA,  // 使用原版方式构建传送门
        GO_TO_HOME_BASE        // 前往基地
    }
}
