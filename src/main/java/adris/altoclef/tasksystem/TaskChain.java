package adris.altoclef.tasksystem;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务链抽象基类 - 管理一组相关任务的执行
 * 提供任务链的基本生命周期管理和优先级控制
 */
public abstract class TaskChain {

    /** 缓存的任务链列表 */
    private final List<Task> cachedTaskChain = new ArrayList<>();

    /**
     * 构造函数，将任务链注册到任务执行器
     * @param runner 任务执行器
     */
    public TaskChain(TaskRunner runner) {
        runner.addTaskChain(this);
    }

    /**
     * 任务链执行主循环方法
     */
    public void tick() {
        cachedTaskChain.clear();
        onTick();
    }

    /**
     * 停止任务链
     */
    public void stop() {
        cachedTaskChain.clear();
        onStop();
    }

    /**
     * 任务链停止时调用的抽象方法
     */
    protected abstract void onStop();

    /**
     * 当任务链被其他任务链中断时调用的抽象方法
     * @param other 中断当前任务链的其他任务链
     */
    public abstract void onInterrupt(TaskChain other);

    /**
     * 任务链执行时调用的抽象方法
     */
    protected abstract void onTick();

    /**
     * 获取任务链优先级的抽象方法
     * @return 任务链的优先级值（越高优先级越高）
     */
    public abstract float getPriority();

    /**
     * 检查任务链是否处于活动状态的抽象方法
     * @return 如果任务链处于活动状态则返回true，否则返回false
     */
    public abstract boolean isActive();

    /**
     * 获取任务链名称的抽象方法
     * @return 任务链的名称
     */
    public abstract String getName();

    /**
     * 获取任务链中的所有任务
     * @return 任务列表
     */
    public List<Task> getTasks() {
        return cachedTaskChain;
    }

    /**
     * 向任务链中添加任务（包级私有方法）
     * @param task 要添加的任务
     */
    void addTaskToChain(Task task) {
        cachedTaskChain.add(task);
    }

    @Override
    public String toString() {
        return getName();
    }

}
