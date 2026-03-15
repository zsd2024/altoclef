package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.Items;

/**
 * 收集鸡蛋任务
 * 用于收集鸡蛋，通过在鸡附近等待来获取鸡下的蛋
 */
public class CollectEggsTask extends ResourceTask {

    private final int _count; // 目标鸡蛋数量

    private final DoToClosestEntityTask _waitNearChickens; // 在最近鸡附近等待的任务

    private AltoClef _mod;

    public CollectEggsTask(int targetCount) {
        super(Items.EGG, targetCount);
        _count = targetCount;
        _waitNearChickens = new DoToClosestEntityTask(chicken -> new GetToEntityTask(chicken, 5), ChickenEntity.class);
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        _mod = mod;
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 错误维度检查
        if (_waitNearChickens.wasWandering() && WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            setDebugState("前往正确维度。");
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }
        // 在鸡附近等待
        setDebugState("在鸡附近等待。是的。");
        return _waitNearChickens;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectEggsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个鸡蛋。";
    }
}
