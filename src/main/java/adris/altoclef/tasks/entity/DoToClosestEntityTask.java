package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 最近实体操作任务 - 查找最近的实体并对其执行指定任务
 */
@SuppressWarnings("rawtypes")
public class DoToClosestEntityTask extends AbstractDoToClosestObjectTask<Entity> {

    /** 目标实体类型数组 */
    private final Class[] targetEntities;

    /** 获取起始位置的供应器 */
    private final Supplier<Vec3d> getOriginPos;

    /** 获取目标任务的函数 */
    private final Function<Entity, Task> getTargetTask;

    /** 判断是否应该与实体交互的谓词 */
    private final Predicate<Entity> shouldInteractWith;

    /**
     * 完整构造函数
     * @param getOriginSupplier 获取起始位置的供应器
     * @param getTargetTask 获取目标任务的函数
     * @param shouldInteractWith 判断是否应该与实体交互的谓词
     * @param entities 目标实体类型数组
     */
    public DoToClosestEntityTask(Supplier<Vec3d> getOriginSupplier, Function<Entity, Task> getTargetTask, Predicate<Entity> shouldInteractWith, Class... entities) {
        getOriginPos = getOriginSupplier;
        this.getTargetTask = getTargetTask;
        this.shouldInteractWith = shouldInteractWith;
        targetEntities = entities;
    }

    /**
     * 构造函数（默认与所有实体交互）
     * @param getOriginSupplier 获取起始位置的供应器
     * @param getTargetTask 获取目标任务的函数
     * @param entities 目标实体类型数组
     */
    public DoToClosestEntityTask(Supplier<Vec3d> getOriginSupplier, Function<Entity, Task> getTargetTask, Class... entities) {
        this(getOriginSupplier, getTargetTask, entity -> true, entities);
    }

    /**
     * 构造函数（使用玩家当前位置作为起始点）
     * @param getTargetTask 获取目标任务的函数
     * @param shouldInteractWith 判断是否应该与实体交互的谓词
     * @param entities 目标实体类型数组
     */
    public DoToClosestEntityTask(Function<Entity, Task> getTargetTask, Predicate<Entity> shouldInteractWith, Class... entities) {
        this(null, getTargetTask, shouldInteractWith, entities);
    }

    /**
     * 构造函数（使用玩家当前位置作为起始点，默认与所有实体交互）
     * @param getTargetTask 获取目标任务的函数
     * @param entities 目标实体类型数组
     */
    public DoToClosestEntityTask(Function<Entity, Task> getTargetTask, Class... entities) {
        this(null, getTargetTask, entity -> true, entities);
    }

    @Override
    protected Vec3d getPos(AltoClef mod, Entity obj) {
        // 返回实体的位置
        return obj.getPos();
    }

    @Override
    protected Optional<Entity> getClosestTo(AltoClef mod, Vec3d pos) {
        // 如果未找到目标实体类型，则返回空
        if (!mod.getEntityTracker().entityFound(targetEntities)) return Optional.empty();
        // 获取距离指定位置最近的实体
        return mod.getEntityTracker().getClosestEntity(pos, shouldInteractWith, targetEntities);
    }

    @Override
    protected Vec3d getOriginPos(AltoClef mod) {
        // 如果指定了起始位置供应器，则使用它；否则使用玩家当前位置
        if (getOriginPos != null) {
            return getOriginPos.get();
        }
        return mod.getPlayer().getPos();
    }

    @Override
    protected Task getGoalTask(Entity obj) {
        // 应用目标任务函数获取具体任务
        return getTargetTask.apply(obj);
    }

    @Override
    protected boolean isValid(AltoClef mod, Entity obj) {
        // 检查实体是否存活且可达
        return obj.isAlive() && mod.getEntityTracker().isEntityReachable(obj);
    }

    @Override
    protected void onStart() {
        // 启动时无需特殊处理
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 停止时无需特殊处理
    }

    @Override
    protected boolean isEqual(Task other) {
        // 比较两个任务是否相等（基于目标实体类型）
        if (other instanceof DoToClosestEntityTask task) {
            return Arrays.equals(task.targetEntities, targetEntities);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "对最近的实体执行操作...";
    }
}