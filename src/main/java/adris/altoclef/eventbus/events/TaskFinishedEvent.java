package adris.altoclef.eventbus.events;

import adris.altoclef.tasksystem.Task;

/**
 * 任务完成事件
 * 当一个任务执行完成时触发此事件
 */
public class TaskFinishedEvent {
    /** 任务执行持续时间（秒） */
    public double durationSeconds;
    /** 最后执行的任务 */
    public Task lastTaskRan;

    /**
     * 构造任务完成事件
     * 
     * @param durationSeconds 任务执行持续时间（秒）
     * @param lastTaskRan 最后执行的任务
     */
    public TaskFinishedEvent(double durationSeconds, Task lastTaskRan) {
        this.durationSeconds = durationSeconds;
        this.lastTaskRan = lastTaskRan;
    }
}
