package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;

/**
 * 单任务链 - 管理单个任务执行的抽象基类
 * 每个单任务链只能同时运行一个任务
 */
public abstract class SingleTaskChain extends TaskChain {

    protected Task mainTask = null; // 主任务
    private boolean interrupted = false; // 标记是否被中断

    private final AltoClef mod; // AltoClef实例

    public SingleTaskChain(TaskRunner runner) {
        super(runner);
        mod = runner.getMod();
    }

    @Override
    protected void onTick() {
        if (!isActive()) return;

        if (interrupted) {
            interrupted = false;
            if (mainTask != null) {
                mainTask.reset();
            }
        }

        if (mainTask != null) {
            if ((mainTask.isFinished()) || mainTask.stopped()) {
                onTaskFinish(mod);
            } else {
                mainTask.tick(this);
            }
        }
    }

    /**
     * 停止时的处理
     */
    protected void onStop() {
        if (isActive() && mainTask != null) {
            mainTask.stop();
            mainTask = null;
        }
    }

    /**
     * 设置主任务
     * @param task 要设置的任务
     */
    public void setTask(Task task) {
        if (mainTask == null || !mainTask.equals(task)) {
            if (mainTask != null) {
                mainTask.stop(task);
            }
            mainTask = task;
            if (task != null) task.reset();
        }
    }


    @Override
    public boolean isActive() {
        return mainTask != null;
    }

    /**
     * 当任务完成时调用
     * @param mod AltoClef实例
     */
    protected abstract void onTaskFinish(AltoClef mod);

    @Override
    public void onInterrupt(TaskChain other) {
        if (other != null) {
            Debug.logInternal("链被中断: " + this + " 由 " + other);
        }
        // 停止我们的任务。当我们再次启动时，让我们的任务知道我们需要运行。
        interrupted = true;
        if (mainTask != null && mainTask.isActive()) {
            mainTask.interrupt(null);
        }
    }

    /**
     * 检查是否当前正在运行
     * @param mod AltoClef实例
     * @return 如果当前正在运行则返回true
     */
    protected boolean isCurrentlyRunning(AltoClef mod) {
        return !interrupted && mainTask.isActive() && !mainTask.isFinished();
    }

    /**
     * 获取当前任务
     * @return 当前任务
     */
    public Task getCurrentTask() {
        return mainTask;
    }
}
