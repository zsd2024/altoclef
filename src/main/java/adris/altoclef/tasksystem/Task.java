package adris.altoclef.tasksystem;

import adris.altoclef.Debug;
import adris.altoclef.tasks.movement.TimeoutWanderTask;

import java.util.function.Predicate;

/**
 * 任务基类 - 所有任务的抽象基类
 * 提供任务的基本生命周期管理和子任务处理功能
 */
public abstract class Task {

    /** 上一次的调试状态字符串 */
    private String oldDebugState = "";
    /** 当前的调试状态字符串 */
    private String debugState = "";

    /** 当前正在执行的子任务 */
    private Task sub = null;

    /** 标记是否是首次执行 */
    private boolean first = true;

    /** 标记任务是否已停止 */
    private boolean stopped = false;

    /** 标记任务是否处于活动状态 */
    private boolean active = false;

    /**
     * 任务执行主循环方法
     * @param parentChain 父任务链
     */
    public void tick(TaskChain parentChain) {
        parentChain.addTaskToChain(this);
        if (first) {
            Debug.logInternal("任务开始: " + this);
            active = true;
            onStart();
            first = false;
            stopped = false;
        }
        if (stopped) return;

        Task newSub = onTick();
        // 调试状态打印
        if (!oldDebugState.equals(debugState)) {
            Debug.logInternal(toString());
            oldDebugState = debugState;
        }
        // 我们有一个子任务
        if (newSub != null) {
            if (!newSub.isEqual(sub)) {
                if (canBeInterrupted(sub, newSub)) {
                    // 我们的子任务是新的
                    if (sub != null) {
                        // 我们之前的子任务必须被中断
                        sub.stop(newSub);
                    }

                    sub = newSub;
                }
            }

            // 运行我们的子任务
            sub.tick(parentChain);
        } else {
            // 我们没有子任务（为null）
            if (sub != null && canBeInterrupted(sub, null)) {
                // 我们之前的子任务必须被中断
                sub.stop();
                sub = null;
            }
        }
    }

    /**
     * 重置任务状态
     */
    public void reset() {
        first = true;
        active = false;
        stopped = false;
    }

    /**
     * 停止任务（无中断任务参数）
     */
    public void stop() {
        stop(null);
    }

    /**
     * 停止任务。下次运行时将执行`onStart`
     * @param interruptTask 中断此任务的任务
     */
    public void stop(Task interruptTask) {
        if (!active) return;
        Debug.logInternal("任务停止: " + this + ", 被 " + interruptTask + " 中断");
        if (!first) {
            onStop(interruptTask);
        }

        if (sub != null && !sub.stopped()) {
            sub.stop(interruptTask);
        }

        first = true;
        active = false;
        stopped = true;
    }

    /**
     * 通知任务其执行已被"挂起"
     * <p>
     * 仍然会执行`onStop`
     * <p>
     * 不会完全停止任务（意味着`isActive`仍然返回true）
     * @param interruptTask 中断此任务的任务
     */
    public void interrupt(Task interruptTask) {
        if (!active) return;
        if (!first) {
            onStop(interruptTask);
        }

        if (sub != null && !sub.stopped()) {
            sub.interrupt(interruptTask);
        }

        first = true;
    }

    /**
     * 设置调试状态
     * @param state 调试状态字符串
     */
    protected void setDebugState(String state) {
        if (state == null) {
            state = "";
        }
        debugState = state;
    }

    // 虚方法
    /**
     * 检查任务是否已完成
     * @return 如果任务已完成则返回true，否则返回false
     */
    public boolean isFinished() {
        return false;
    }

    /**
     * 检查任务是否处于活动状态
     * @return 如果任务处于活动状态则返回true，否则返回false
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 检查任务是否已停止
     * @return 如果任务已停止则返回true，否则返回false
     */
    public boolean stopped() {
        return stopped;
    }

    /**
     * 任务开始时调用的抽象方法
     */
    protected abstract void onStart();

    /**
     * 任务执行时调用的抽象方法，返回下一个子任务
     * @return 下一个要执行的子任务，如果不需要子任务则返回null
     */
    protected abstract Task onTick();

    /**
     * 任务停止时调用的抽象方法
     * @param interruptTask 中断此任务的任务（如果任务正常停止则为null）
     */
    protected abstract void onStop(Task interruptTask);

    /**
     * 比较两个任务是否相等的抽象方法
     * @param other 要比较的其他任务
     * @return 如果两个任务相等则返回true，否则返回false
     */
    protected abstract boolean isEqual(Task other);

    /**
     * 获取任务的调试字符串表示的抽象方法
     * @return 任务的调试字符串
     */
    protected abstract String toDebugString();

    @Override
    public String toString() {
        return "<" + toDebugString() + "> " + debugState;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task task) {
            return isEqual(task);
        }
        return false;
    }

    /**
     * 检查当前任务或其任何子任务是否满足给定条件
     * @param pred 条件谓词
     * @return 如果当前任务或其任何子任务满足条件则返回true，否则返回false
     */
    public boolean thisOrChildSatisfies(Predicate<Task> pred) {
        Task t = this;
        while (t != null) {
            if (pred.test(t)) return true;
            t = t.sub;
        }
        return false;
    }

    /**
     * 检查当前任务或其任何子任务是否已超时
     * @return 如果当前任务或其任何子任务已超时则返回true，否则返回false
     */
    public boolean thisOrChildAreTimedOut() {
        return thisOrChildSatisfies(task -> task instanceof TimeoutWanderTask);
    }

    /**
     * 检查子任务是否可以被中断
     * 有时任务现在不能被打扰中断。
     * 例如，如果我们在空中并且必须完成跑酷动作。
     * @param subTask 子任务
     * @param toInterruptWith 要中断的任務
     * @return 如果可以中断则返回true，否则返回false
     */
    private boolean canBeInterrupted(Task subTask, Task toInterruptWith) {
        if (subTask == null) return true;
        // 我们的任务可以声明它现在强制自己保持活动状态
        return (subTask.thisOrChildSatisfies(task -> {
            if (task instanceof ITaskCanForce canForce) {
                return !canForce.shouldForce(toInterruptWith);
            }
            return true;
        }));
    }
}
