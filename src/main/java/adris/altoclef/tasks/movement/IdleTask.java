package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Playground;
import adris.altoclef.tasksystem.Task;

/**
 * 空闲任务 - 不执行任何操作
 */
public class IdleTask extends Task {
    @Override
    protected void onStart() {

    }

    @Override
    protected Task onTick() {
        // 什么都不做，除了可能测试代码
        Playground.IDLE_TEST_TICK_FUNCTION(AltoClef.getInstance());
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    public boolean isFinished() {
        // 永不完結
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof IdleTask;
    }

    @Override
    protected String toDebugString() {
        return "空闲";
    }
}
