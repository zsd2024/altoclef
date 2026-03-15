package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.item.Item;

/**
 * 获取建筑材料任务
 * 用于收集建筑用的材料（通常是可丢弃的方块）
 */
public class GetBuildingMaterialsTask extends Task {
    private final int _count;

    public GetBuildingMaterialsTask(int count) {
        _count = count;
    }

    @Override
    protected void onStart() {
        // 任务开始时的初始化工作
    }

    @Override
    protected Task onTick() {
        // 获取可丢弃的物品列表作为建筑材料
        Item[] throwaways = AltoClef.getInstance().getModSettings().getThrowawayItems(true);
        // 挖掘并收集这些可丢弃物品
        return new MineAndCollectTask(new ItemTarget[]{new ItemTarget(throwaways, _count)}, MiningRequirement.WOOD);
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时的清理工作
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetBuildingMaterialsTask task) {
            return task._count == _count;
        }
        return false;
    }

    @Override
    public boolean isFinished() {
        // 检查已有的建筑材料数量是否达到目标
        return StorageHelper.getBuildingMaterialCount() >= _count;
    }

    @Override
    protected String toDebugString() {
        return "收集 " + _count + " 个建筑材料。";
    }
}
