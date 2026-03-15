package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.item.Items;

/**
 * 收集燃料任务
 * 用于收集燃料（目前主要是煤炭），在不同维度中采取不同策略
 * TODO: 让这个任务收集不仅仅是煤炭。如果煤炭太远或无法获取木制工具，应该智能选择替代来源。
 */
public class CollectFuelTask extends Task {

    private final double targetFuel; // 目标燃料量

    public CollectFuelTask(double targetFuel) {
        this.targetFuel = targetFuel;
    }

    @Override
    protected void onStart() {
        // 任务开始时的初始化
    }

    @Override
    protected Task onTick() {

        switch (WorldHelper.getCurrentDimension()) {
            case OVERWORLD -> {
                // 目前只收集煤炭
                setDebugState("收集煤炭。");
                return TaskCatalogue.getItemTask(Items.COAL, (int) Math.ceil(targetFuel / 8));
            }
            case END -> {
                setDebugState("前往主世界，因为这里无法找到更多燃料。");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
            case NETHER -> {
                setDebugState("前往主世界，因为我们可以使用木头但木头会让机器人困惑。目前是个bug。");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
        }
        setDebugState("无效维度: " + WorldHelper.getCurrentDimension());
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof CollectFuelTask task) {
            return Math.abs(task.targetFuel - targetFuel) < 0.01;
        }
        return false;
    }

    @Override
    public boolean isFinished() {
        return AltoClef.getInstance().getItemStorage().getItemCountInventoryOnly(Items.COAL) >= targetFuel;
    }

    @Override
    protected String toDebugString() {
        return "收集燃料: x" + targetFuel;
    }
}
