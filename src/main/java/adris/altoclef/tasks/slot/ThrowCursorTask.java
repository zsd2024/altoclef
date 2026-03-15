package adris.altoclef.tasks.slot;

import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.slots.Slot;

/**
 * 丢弃光标中物品的任务
 */
public class ThrowCursorTask extends Task {

    // 丢弃光标物品的任务
    private final Task throwTask = new ClickSlotTask(Slot.UNDEFINED);

    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        // 执行丢弃光标物品的任务
        return throwTask;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    protected boolean isEqual(Task obj) {
        // 所有ThrowCursorTask实例都被认为是相等的
        return obj instanceof ThrowCursorTask;
    }

    @Override
    protected String toDebugString() {
        return "丢弃光标物品";
    }

    @Override
    public boolean isFinished() {
        // 当丢弃任务完成时，此任务也完成
        return throwTask.isFinished();
    }
}
