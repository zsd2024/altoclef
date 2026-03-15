package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.entity.Entity;

import java.util.function.Predicate;

/**
 * 击杀并拾取任务
 * 用于击杀指定类型的生物并拾取掉落物品
 */
public class KillAndLootTask extends ResourceTask {

    // 要击杀的实体类型
    private final Class<?> _toKill;

    // 击杀任务
    private final Task _killTask;

    public KillAndLootTask(Class<?> toKill, Predicate<Entity> shouldKill, ItemTarget... itemTargets) {
        super(itemTargets.clone());
        _toKill = toKill;
        _killTask = new KillEntitiesTask(shouldKill, _toKill);
    }

    public KillAndLootTask(Class<?> toKill, ItemTarget... itemTargets) {
        super(itemTargets.clone());
        _toKill = toKill;
        _killTask = new KillEntitiesTask(_toKill);
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 检查是否找到了要击杀的实体
        if (!mod.getEntityTracker().entityFound(_toKill)) {
            if (isInWrongDimension(mod)) {
                setDebugState("前往正确的维度。");
                return getToCorrectDimensionTask(mod);
            }
            setDebugState("搜索生物...");
            return new TimeoutWanderTask();
        }
        // 我们找到了生物！
        return _killTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务停止时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof KillAndLootTask task) {
            return task._toKill.equals(_toKill);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "从 " + _toKill.toGenericString() + " 收集物品";
    }
}
